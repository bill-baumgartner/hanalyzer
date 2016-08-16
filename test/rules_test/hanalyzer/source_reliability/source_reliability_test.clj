(ns rules-test.hanalyzer.source-reliability.source-reliability-test
  (use clojure.test
        edu.ucdenver.ccp.kr.sesame.kb
        edu.ucdenver.ccp.kr.sesame.sparql
        edu.ucdenver.ccp.kr.sesame.rdf
        )
  (:require  [edu.ucdenver.ccp.kabob.build.run-rules :refer [query-variables run-forward-rule-sparql-string]]
             [edu.ucdenver.ccp.kr.forward-rule :refer [add-reify-fns]]
             [edu.ucdenver.ccp.kr.sparql :refer [sparql-select-query query]]
             [edu.ucdenver.ccp.kr.rdf :refer [register-namespaces synch-ns-mappings add!]]
             [edu.ucdenver.ccp.kr.kb :refer [kb open close]]
             [edu.ucdenver.ccp.kabob.namespace :refer [*namespaces*]]
             [edu.ucdenver.ccp.kabob.rule :refer [kabob-load-rules-from-classpath]]
             [edu.ucdenver.ccp.kabob.build.output-kb :refer [output-kb]]
             [kabob]
             [clojure.pprint :refer [pprint]]))


(def sample-kb-triples '((ex/h1 rdf/type iaohan/Node)
                         (ex/h2 rdf/type iaohan/Node)
                         (ex/h3 rdf/type iaohan/Node)
                         (ex/h4 rdf/type iaohan/Node)
                         (ex/h5 rdf/type iaohan/Node)

                         (ex/shared_bp_edge_1 rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_1 iaohan/linksNode ex/h1)
                         (ex/shared_bp_edge_1 iaohan/linksNode ex/h2)

                         ;; edge 6 simulates multiple go bp assertions
                         ;; between nodes h1 and h2. 
                         (ex/shared_bp_edge_6 rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_6 iaohan/linksNode ex/h1)
                         (ex/shared_bp_edge_6 iaohan/linksNode ex/h2)
                         
                         (ex/shared_bp_edge_2 rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_2 iaohan/linksNode ex/h2)
                         (ex/shared_bp_edge_2 iaohan/linksNode ex/h3)

                         (ex/shared_bp_edge_3 rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_3 iaohan/linksNode ex/h3)
                         (ex/shared_bp_edge_3 iaohan/linksNode ex/h5)

                         (ex/shared_bp_edge_4 rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_4 iaohan/linksNode ex/h2)
                         (ex/shared_bp_edge_4 iaohan/linksNode ex/h4)

                         (ex/shared_bp_edge_5 rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_5 iaohan/linksNode ex/h3)
                         (ex/shared_bp_edge_5 iaohan/linksNode ex/h4)

                         (ex/consensus_edge_1 rdf/type iaohan/HAN_0000010) ;; HAN:high_confidence_ppi_asserted_edge
                         (ex/consensus_edge_1 rdf/type iaohan/HAN_0000016) ;; HAN:consensus_set_edge
                         (ex/consensus_edge_1 iaohan/linksNode ex/h1)
                         (ex/consensus_edge_1 iaohan/linksNode ex/h2)

                         (ex/consensus_edge_4 rdf/type iaohan/HAN_0000010) ;; HAN:high_confidence_ppi_asserted_edge
                         (ex/consensus_edge_4 rdf/type iaohan/HAN_0000016) ;; HAN:consensus_set_edge
                         (ex/consensus_edge_4 iaohan/linksNode ex/h3)
                         (ex/consensus_edge_4 iaohan/linksNode ex/h4)
                         
                         (ex/consensus_edge_2 rdf/type iaohan/HAN_0000010) ;; HAN:high_confidence_ppi_asserted_edge
                         (ex/consensus_edge_2 rdf/type iaohan/HAN_0000016) ;; HAN:consensus_set_edge
                         (ex/consensus_edge_2 iaohan/linksNode ex/h3)
                         (ex/consensus_edge_2 iaohan/linksNode ex/h5)

                         (ex/consensus_edge_3 rdf/type iaohan/HAN_0000010) ;; HAN:high_confidence_ppi_asserted_edge
                         (ex/consensus_edge_3 rdf/type iaohan/HAN_0000016) ;; HAN:consensus_set_edge
                         (ex/consensus_edge_3 iaohan/linksNode ex/h3)
                         (ex/consensus_edge_3 iaohan/linksNode ex/h4)))
                                  
(def new-triples '())

(def result-triples '())

(defn test-kb [triples]
  "initializes an empty kb"
  (let [kb (register-namespaces (synch-ns-mappings (open (kb :sesame-mem)))
                                *namespaces*)]
    (dorun (map (partial add! kb) triples))
    kb))

(defn get-consensus-overlap-count [subject output-kb]
  "queries the kb for the iaohan/consensus_overlap_count_aael for the given subject"
  ('?/count (first (apply list (query output-kb
                                      `((~subject iaohan/consensus_overlap_count_aael ?/count)))))))

(deftest test-count-consensus-overlap
  (let [rule (first (filter #(= (:name %) "consensus-overlap-count-go-bp-aael") (kabob-load-rules-from-classpath "rules/hanalyzer/reliability/aael")))
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    
    (run-forward-rule-sparql-string source-kb source-kb rule)

    ;; there should be 1 instances of iaohan/annotation-count-aael
    (is (= 1 (count (query source-kb '((iaohan/HAN_0000007 iaohan/consensus_overlap_count_aael ?/score)))))) ;; HAN:shared_go_bp_asserted_edge

    (is (= 4 (get-consensus-overlap-count 'iaohan/HAN_0000007 source-kb))) ;; HAN:shared_go_bp_asserted_edge
                                      
    ;; The code fragment below is useful for debugging as it writes
    ;; triples to a local file.
   ;; (let [log-kb (output-kb "/tmp/triples.nt")]
      ;; add sample triples to the log kb
   ;;   (dorun (map (partial add! log-kb) sample-kb-triples))
      
   ;;   (run-forward-rule source-kb log-kb rule)
   ;;   (close log-kb))
    ))



(defn get-asserted-node-interaction-count [subject output-kb]
  "queries the kb for the iaohan/asserted_node_interaction_count_aael for the given subject"
  ('?/count (first (apply list (query output-kb
                                      `((~subject iaohan/asserted_node_interaction_count_aael ?/count)))))))


(deftest test-count-asserted-node-interactions
 (let [rule (first (filter #(= (:name %) "asserted-node-interaction-count-go-bp-aael") (kabob-load-rules-from-classpath "rules/hanalyzer/reliability/aael")))
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    
    (run-forward-rule-sparql-string source-kb source-kb rule)

    ;; there should be 1 instances of iaohan/annotation-count-aael
    (is (= 1 (count (query source-kb '((iaohan/HAN_0000007 iaohan/asserted_node_interaction_count_aael ?/score)))))) ;; HAN:shared_go_bp_asserted_edge

    (is (= 6 (get-asserted-node-interaction-count 'iaohan/HAN_0000007 source-kb))))) ;; HAN:shared_go_bp_asserted_edge
    



    
    

  




