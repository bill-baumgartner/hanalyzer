(ns rules-test.hanalyzer.edges.master-edge-test
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


(def r_mf 0.002)
(def r_cc 0.003)
(def r_bp 0.004)
(def r_pw 0.02)
(def r_ppi 0.502)

(def sample-kb-triples `((ex/h1 rdf/type iaohan/Node)
                         (ex/h2 rdf/type iaohan/Node)
                         (ex/h3 rdf/type iaohan/Node)
                         (ex/h4 rdf/type iaohan/Node)

                         (iaohan/HAN_0000005 iaohan/normalized_reliability_aael ~r_mf) ;; HAN:shared_go_mf_asserted_edge
                         (iaohan/HAN_0000006 iaohan/normalized_reliability_aael ~r_cc);; HAN:shared_go_cc_asserted_edge
                         (iaohan/HAN_0000007 iaohan/normalized_reliability_aael ~r_bp);; HAN:shared_go_bp_asserted_edge
                         (iaohan/HAN_0000008 iaohan/normalized_reliability_aael ~r_pw);; HAN:shared_pathway_asserted_edge
                         (iaohan/HAN_0000010 iaohan/normalized_reliability_aael ~r_ppi);; HAN:ppi_high_confidence_asserted_edge

                         
                         ;; =========================
                         ;; BP links 1-2, 2-3 x2, 3-4
                         ;; =========================
                         
                         (ex/shared_bp_edge_12 rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_12 iaohan/linksNode ex/h1)
                         (ex/shared_bp_edge_12 iaohan/linksNode ex/h2)

                         (ex/shared_bp_edge_23_a rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_23_a iaohan/linksNode ex/h2)
                         (ex/shared_bp_edge_23_a iaohan/linksNode ex/h3)
                         
                         (ex/shared_bp_edge_23_b rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_23_b iaohan/linksNode ex/h2)
                         (ex/shared_bp_edge_23_b iaohan/linksNode ex/h3)

                         (ex/shared_bp_edge_34 rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
                         (ex/shared_bp_edge_34 iaohan/linksNode ex/h3)
                         (ex/shared_bp_edge_34 iaohan/linksNode ex/h4)

                         ;; =================
                         ;; CC links 2-4, 3-4
                         ;; =================
                         
                         (ex/shared_cc_edge_24 rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
                         (ex/shared_cc_edge_24 iaohan/linksNode ex/h2)
                         (ex/shared_cc_edge_24 iaohan/linksNode ex/h4)

                         (ex/shared_cc_edge_34 rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
                         (ex/shared_cc_edge_34 iaohan/linksNode ex/h3)
                         (ex/shared_cc_edge_34 iaohan/linksNode ex/h4)

                         ;; =================
                         ;; MF links 2-3, 3-4
                         ;; =================

                         (ex/shared_mf_edge_23 rdf/type iaohan/HAN_0000005) ;; HAN:shared_go_mf_asserted_edge
                         (ex/shared_mf_edge_23 iaohan/linksNode ex/h2)
                         (ex/shared_mf_edge_23 iaohan/linksNode ex/h3)

                         (ex/shared_mf_edge_34 rdf/type iaohan/HAN_0000005) ;; HAN:shared_go_mf_asserted_edge
                         (ex/shared_mf_edge_34 iaohan/linksNode ex/h3)
                         (ex/shared_mf_edge_34 iaohan/linksNode ex/h4)

                         ;; =================
                         ;; PW links 2-3, 3-4
                         ;; =================

                         (ex/shared_pw_edge_23 rdf/type iaohan/HAN_0000008) ;; HAN:shared_pathway_asserted_edge
                         (ex/shared_pw_edge_23 iaohan/linksNode ex/h2)
                         (ex/shared_pw_edge_23 iaohan/linksNode ex/h3)
                         
                         (ex/shared_pw_edge_34 rdf/type iaohan/HAN_0000008) ;; HAN:shared_pathway_asserted_edge
                         (ex/shared_pw_edge_34 iaohan/linksNode ex/h3)
                         (ex/shared_pw_edge_34 iaohan/linksNode ex/h4)

                         ;; ===========================
                         ;; PPI_HighConf links 2-4, 3-4
                         ;; ===========================

                         (ex/shared_ppi_edge_24 rdf/type iaohan/HAN_0000010) ;; HAN:ppi_high_confidence_asserted_edge
                         (ex/shared_ppi_edge_24 iaohan/linksNode ex/h2)
                         (ex/shared_ppi_edge_24 iaohan/linksNode ex/h4)

                         (ex/shared_ppi_edge_34 rdf/type iaohan/HAN_0000010) ;; HAN:ppi_high_confidence_asserted_edge
                         (ex/shared_ppi_edge_34 iaohan/linksNode ex/h3)
                         (ex/shared_ppi_edge_34 iaohan/linksNode ex/h4)))
                         
                                  
(def new-triples '())

(def result-triples '())

(defn test-kb [triples]
  "initializes an empty kb"
  (let [kb (register-namespaces (synch-ns-mappings (open (kb :sesame-mem)))
                                *namespaces*)]
    (dorun (map (partial add! kb) triples))
    kb))

(defn get-master-edge-score [node1 node2 kb]
  "queries the kb for the reliability score for the master edge for the given nodes"
  ('?/score (first (apply list (query kb
                                      `((?/edge rdf/type iaohan/HAN_0000001)
                                        (?/edge iaohan/linksNode ~node1)
                                        (?/edge iaohan/linksNode ~node2)
                                        (?/edge iaohan/reliability_aael ?/score)))))))

(defn noisy-or [nums]
  "compute the noisy-or function over the input numbers"
  (- 1 (reduce * (map #(- 1 %) nums))))

(deftest test-count-asserted-node-interactions
 (let [rule (first (filter #(= (:name %) "master-edges-aael") (kabob-load-rules-from-classpath "rules/hanalyzer/edges/master/aael")))
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    
    (run-forward-rule-sparql-string source-kb source-kb rule)

    ;; there should be 1 instances of iaohan/annotation-count-aael
    (is (= 4 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000001)))))) ;; HAN:master_edge

    (is (= (noisy-or `(~r_bp)) (get-master-edge-score 'ex/h1 'ex/h2 source-kb)))
    (is (= (noisy-or `(~r_bp ~r_bp ~r_mf ~r_pw)) (get-master-edge-score 'ex/h2 'ex/h3 source-kb)))
    (is (= (noisy-or `(~r_cc ~r_ppi)) (get-master-edge-score 'ex/h2 'ex/h4 source-kb)))
    (is (= (noisy-or `(~r_bp ~r_cc ~r_mf ~r_pw ~r_ppi)) (get-master-edge-score 'ex/h3 'ex/h4 source-kb)))))
    



    
    

  




