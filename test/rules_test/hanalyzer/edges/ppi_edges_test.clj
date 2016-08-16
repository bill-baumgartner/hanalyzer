(ns rules-test.hanalyzer.edges.ppi-edges-test
  (use clojure.test
        edu.ucdenver.ccp.kr.sesame.kb
        edu.ucdenver.ccp.kr.sesame.sparql
        edu.ucdenver.ccp.kr.sesame.rdf
        )
  (:require  [edu.ucdenver.ccp.kabob.build.run-rules :refer [query-variables run-forward-rule]]
             [edu.ucdenver.ccp.kr.forward-rule :refer [add-reify-fns]]
             [edu.ucdenver.ccp.kr.sparql :refer [sparql-select-query query]]
             [edu.ucdenver.ccp.kr.rdf :refer [register-namespaces synch-ns-mappings add!]]
             [edu.ucdenver.ccp.kr.kb :refer [kb open close]]
             [edu.ucdenver.ccp.kabob.namespace :refer [*namespaces*]]
             [edu.ucdenver.ccp.kabob.rule :refer [kabob-load-rules-from-classpath]]
             [edu.ucdenver.ccp.kabob.build.output-kb :refer [output-kb]]
             [kabob]
             [clojure.pprint :refer [pprint]]))


(def sample-kb-triples '((ex/p1_sc rdfs/subClassOf ex/p1)
                         (ex/p2_sc rdfs/subClassOf ex/p2)
                         (ex/p3_sc rdfs/subClassOf ex/p3)

                         (ex/han_node_1 rdfs/type iaohan/Node)
                         (ex/han_node_1 rdfs/label "p1 node")
                         (ex/han_node_1 iaohan/denotes ex/p1)

                         (ex/han_node_2 rdfs/type iaohan/Node)
                         (ex/han_node_2 rdfs/label "p2 node")
                         (ex/han_node_2 iaohan/denotes ex/p2)

                         (ex/han_node_3 rdfs/type iaohan/Node)
                         (ex/han_node_3 rdfs/label "p3 node")
                         (ex/han_node_3 iaohan/denotes ex/p3)
                         
                         (ex/taxon_r rdf/type owl/Restriction)
                         (ex/taxon_r owl/onProperty obo/RO_0002162)
                         (ex/taxon_r owl/someValuesFrom obo/NCBITaxon_7159)
                         (ex/p1 rdfs/subClassOf ex/taxon_r)
                         (ex/p2 rdfs/subClassOf ex/taxon_r)
                         (ex/p3 rdfs/subClassOf ex/taxon_r)

                         (ex/interaction rdfs/subClassOf obo/GO_0005488) ;; GO:binding
                         (ex/interaction rdfs/label ["binding"]) 
                         (ex/interaction rdfs/subClassOf ex/participant_r1)
                         (ex/participant_r1 owl/onProperty obo/RO_0000057)
                         (ex/participant_r1 owl/someValuesFrom ex/gene1_sc)
                         (ex/interaction rdfs/subClassOf ex/participant_r1)
                         (ex/interaction rdfs/subClassOf ex/participant_r2)
                         (ex/participant_r2 owl/onProperty obo/RO_0000057)
                         (ex/participant_r2 owl/someValuesFrom ex/gene2_sc)
                         (ex/gene1_sc rdfs/subClassOf ex/p1)
                         (ex/gene2_sc rdfs/subClassOf ex/p2)
                         (ex/record obo/IAO_0000219 ex/interaction) ; the ICE record denotes the interaction
                         (ex/record kiao/hasTemplate iaounknown/InteractionWithScoreFileRecordSchema1)
                         (ex/record obo/BFO_0000051 ex/sourceField)
                         (ex/sourceField kiao/hasTemplate iaounknown/InteractionWithScoreFileRecord_sourceDataField1)
                         (ex/sourceField obo/IAO_0000219 ["Guo"])
                         (ex/record obo/BFO_0000051 ex/scoreField)
                         (ex/scoreField kiao/hasTemplate iaounknown/InteractionWithScoreFileRecord_scoreDataField1)
                         (ex/scoreField obo/IAO_0000219 0.78)))
         

(def new-triples '())

(def result-triples '())

(defn test-kb [triples]
  "initializes an empty kb"
  (let [kb (register-namespaces (synch-ns-mappings (open (kb :sesame-mem)))
                                *namespaces*)]
    (dorun (map (partial add! kb) triples))
    kb))

(deftest test-create-ppi-edges
  (let [rule (first (filter #(= (:name %) "aael-ppi-edges") (kabob-load-rules-from-classpath "rules/hanalyzer/edges/ppi/aael")))
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    
    (run-forward-rule source-kb source-kb rule)

    ;; there are two instances b/c there are two possible combinations of ?/han_node_1 and ?/han_node_2. You could fix this using FILTER( STR(IRI(?node1)) < STR(IRI(?node2)) but I'm not sure if those have been implemented in this branch of the code. They are in Elizabeth's branch of kabob somewhereI believe.
    (is (= 2 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000009) ;; HAN:ppi_asserted_edge
                                       (?/edge iaohan/linksNode ?/han_node_1)
                                       (?/edge iaohan/linksNode ?/han_node_2)
                                       (!= ?/han_node_1 ?/han_node_2)
                                       (?/edge iaohan/interaction_source "Guo")
                                       (?/edge iaohan/interaction_score ?/score)
                                       (= ?/score 0.78)
                                       )))))
    
    ;; The code fragment below is useful for debugging as it writes
    ;; triples to a local file.
    (let [log-kb (output-kb "/tmp/triples.nt")]
      ;; add sample triples to the log kb
      (dorun (map (partial add! log-kb) sample-kb-triples))
      
      (run-forward-rule source-kb log-kb rule)
      (close log-kb))
    ))



    
    

  




