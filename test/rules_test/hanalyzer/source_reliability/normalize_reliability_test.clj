(ns rules-test.hanalyzer.source-reliability.normalize-reliability-test
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


(def sample-kb-triples '((iaohan/HAN_0000005 rdfs/subClassOf iaohan/HAN_0000002)
                         (iaohan/HAN_0000006 rdfs/subClassOf iaohan/HAN_0000002)
                         (iaohan/HAN_0000007 rdfs/subClassOf iaohan/HAN_0000002)
                         (iaohan/HAN_0000008 rdfs/subClassOf iaohan/HAN_0000002)
                         (iaohan/HAN_0000012 rdfs/subClassOf iaohan/HAN_0000002)
                         (iaohan/HAN_0000013 rdfs/subClassOf iaohan/HAN_0000002)
                         (iaohan/HAN_0000014 rdfs/subClassOf iaohan/HAN_0000002)
                         (iaohan/HAN_0000015 rdfs/subClassOf iaohan/HAN_0000002)
                         
                         (iaohan/HAN_0000005 iaohan/reliability_aael 0.003165)
                         (iaohan/HAN_0000006 iaohan/reliability_aael 0.0029346)
                         (iaohan/HAN_0000007 iaohan/reliability_aael 0.0)
                         (iaohan/HAN_0000008 iaohan/reliability_aael 1.000581)
                         (iaohan/HAN_0000012 iaohan/reliability_aael 0.007044)
                         (iaohan/HAN_0000013 iaohan/reliability_aael 0.0183835)
                         (iaohan/HAN_0000014 iaohan/reliability_aael 1.0002906)
                         (iaohan/HAN_0000015 iaohan/reliability_aael 0.0002034)))
                                  
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

(deftest test-reliability-normalization
  (let [rule (first (filter #(= (:name %) "normalize-reliability") (kabob-load-rules-from-classpath "rules/hanalyzer/reliability/aael")))
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    
    (run-forward-rule-sparql-string source-kb source-kb rule)

    ;; there should be 8 instances of iaohan/normalized_reliability
    (is (= 8 (count (query source-kb '((?/edge_type iaohan/normalized_reliability_aael ?/score)))))) 

    ;;(is (= 4 (get-consensus-overlap-count 'iaohan/HAN_0000007 source-kb))) ;; HAN:shared_go_bp_asserted_edge
                                      
    ;; The code fragment below is useful for debugging as it writes
    ;; triples to a local file.
   ;; (let [log-kb (output-kb "/tmp/triples.nt")]
      ;; add sample triples to the log kb
   ;;   (dorun (map (partial add! log-kb) sample-kb-triples))
      
   ;;   (run-forward-rule source-kb log-kb rule)
   ;;   (close log-kb))
    ))
    



    
    

  




