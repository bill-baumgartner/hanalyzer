(ns rules-test.hanalyzer.edges.shared-go-cc-edge-hierarchical-test
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


;;; --------------------------------------------------------
;;; Set up a test KB below and run the resnik-concept-probability
;;;  rules on a sample ontology as defined below.
;;; --------------------------------------------------------

;;;            obo:GO_0005575
;;;                 / \
;;;                b   x           above, prob > 0.01
;;;     - - - - - - - - - - - - - - - - - - - - - - - -
;;;               / \ / \          below, prob < 0.01
;;;              c   e   y
;;;             /   /   / \
;;;            d   f   w   z
;;;           /         \ /
;;;          g           v
;;;         /
;;;        h
;;;
;;; All nodes will have obo-namespace=cellular_component
;;;
;;; Annotation of these concepts to three proteins (p1, p2, & p3)
;;; will also be modeled.
;;;
;;; c=p1
;;; d=p2
;;; f=p3
;;; y=p1
;;; w=p2
;;; v=p1
;;; z=p2,p3

(def sample-kb-triples '((ex/b rdfs/subClassOf obo/GO_0005575)
                         (ex/x rdfs/subClassOf obo/GO_0005575)
                         (ex/c rdfs/subClassOf ex/b)
                         (ex/e rdfs/subClassOf ex/b)
                         (ex/d rdfs/subClassOf ex/c)
                         (ex/f rdfs/subClassOf ex/e)
                         (ex/g rdfs/subClassOf ex/d)
                         (ex/h rdfs/subClassOf ex/g)
                         (ex/e rdfs/subClassOf ex/x)
                         (ex/y rdfs/subClassOf ex/x)
                         (ex/w rdfs/subClassOf ex/y)
                         (ex/z rdfs/subClassOf ex/y)
                         (ex/v rdfs/subClassOf ex/w)
                         (ex/v rdfs/subClassOf ex/z)
                         (obo/GO_0005575 oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/b oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/c oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/d oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/e oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/f oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/g oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/h oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/x oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/y oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/w oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/z oboInOwl/hasOBONamespace ["cellular_component"])
                         (ex/v oboInOwl/hasOBONamespace ["cellular_component"])

                         (obo/GO_0008150 rdfs/label ["biological_process"])
                         (ex/b rdfs/label ["b"])
                         (ex/c rdfs/label ["c"])
                         (ex/d rdfs/label ["d"])
                         (ex/e rdfs/label ["e"])
                         (ex/f rdfs/label ["f"])
                         (ex/g rdfs/label ["g"])
                         (ex/h rdfs/label ["g"])
                         (ex/x rdfs/label ["x"])
                         (ex/y rdfs/label ["y"])
                         (ex/w rdfs/label ["w"])
                         (ex/z rdfs/label ["z"])
                         (ex/v rdfs/label ["v"])

                          ;; the rule that creates shared go edges uses
                         ;; the resnik concept probability as a
                         ;; threshold to keep from creating edges
                         ;; based on very general concepts, e.g. the
                         ;; root biological_process concept. Here we
                         ;; set the resnik concept probability for
                         ;; each concept to be 0.001
                         (obo/GO_0008150 iaohan/resnik-concept-prob-aael 1.0)
                         (ex/b iaohan/resnik-concept-prob-aael 0.100)
                         (ex/c iaohan/resnik-concept-prob-aael 0.001)
                         (ex/d iaohan/resnik-concept-prob-aael 0.001)
                         (ex/e iaohan/resnik-concept-prob-aael 0.001)
                         (ex/f iaohan/resnik-concept-prob-aael 0.001)
                         (ex/g iaohan/resnik-concept-prob-aael 0.001)
                         (ex/h iaohan/resnik-concept-prob-aael 0.001)
                         (ex/v iaohan/resnik-concept-prob-aael 0.001)
                         (ex/w iaohan/resnik-concept-prob-aael 0.001)
                         (ex/x iaohan/resnik-concept-prob-aael 0.100)
                         (ex/y iaohan/resnik-concept-prob-aael 0.001)
                         (ex/z iaohan/resnik-concept-prob-aael 0.001)
                                                 
                         (ex/p1_sc rdfs/subClassOf ex/p1)
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


                         (ex/loc1 rdfs/subClassOf obo/GO_0051179) ;localization
                         (ex/protein1 rdfs/subClassOf ex/p1)
                         (ex/gocc1 rdfs/subClassOf ex/h)
                         (ex/of1 rdf/type owl/Restriction)
                         (ex/of1 owl/onProperty obo/RO_0002313) ;transports or maintains localization of
                         (ex/of1 owl/someValuesFrom ex/protein1)
                         (ex/to1 rdf/type owl/Restriction)
                         (ex/to1 owl/onProperty obo/RO_0002339) ; has target end location
                         (ex/to1 owl/someValuesFrom ex/gocc1)
                         (ex/loc1 rdfs/subClassOf ex/of1)
                         (ex/loc1 rdfs/subClassOf ex/to1)
                         
                         (ex/loc2 rdfs/subClassOf obo/GO_0051179) ;localization
                         (ex/protein2 rdfs/subClassOf ex/p2)
                         (ex/gocc2 rdfs/subClassOf ex/d)
                         (ex/of2 rdf/type owl/Restriction)
                         (ex/of2 owl/onProperty obo/RO_0002313) ;transports or maintains localization of
                         (ex/of2 owl/someValuesFrom ex/protein2)
                         (ex/to2 rdf/type owl/Restriction)
                         (ex/to2 owl/onProperty obo/RO_0002339) ; has target end location
                         (ex/to2 owl/someValuesFrom ex/gocc2)
                         (ex/loc2 rdfs/subClassOf ex/of2)
                         (ex/loc2 rdfs/subClassOf ex/to2)
                         
                         (ex/loc3 rdfs/subClassOf obo/GO_0051179) ;localization
                         (ex/protein3 rdfs/subClassOf ex/p3)
                         (ex/gocc3 rdfs/subClassOf ex/f)
                         (ex/of3 rdf/type owl/Restriction)
                         (ex/of3 owl/onProperty obo/RO_0002313) ;transports or maintains localization of
                         (ex/of3 owl/someValuesFrom ex/protein3)
                         (ex/to3 rdf/type owl/Restriction)
                         (ex/to3 owl/onProperty obo/RO_0002339) ; has target end location
                         (ex/to3 owl/someValuesFrom ex/gocc3)
                         (ex/loc3 rdfs/subClassOf ex/of3)
                         (ex/loc3 rdfs/subClassOf ex/to3)
                         
                         (ex/loc4 rdfs/subClassOf obo/GO_0051179) ;localization
                         (ex/protein4 rdfs/subClassOf ex/p1)
                         (ex/gocc4 rdfs/subClassOf ex/y)
                         (ex/of4 rdf/type owl/Restriction)
                         (ex/of4 owl/onProperty obo/RO_0002313) ;transports or maintains localization of
                         (ex/of4 owl/someValuesFrom ex/protein4)
                         (ex/to4 rdf/type owl/Restriction)
                         (ex/to4 owl/onProperty obo/RO_0002339) ; has target end location
                         (ex/to4 owl/someValuesFrom ex/gocc4)
                         (ex/loc4 rdfs/subClassOf ex/of4)
                         (ex/loc4 rdfs/subClassOf ex/to4)
                         
                         (ex/loc5 rdfs/subClassOf obo/GO_0051179) ;localization
                         (ex/protein5 rdfs/subClassOf ex/p2)
                         (ex/gocc5 rdfs/subClassOf ex/w)
                         (ex/of5 rdf/type owl/Restriction)
                         (ex/of5 owl/onProperty obo/RO_0002313) ;transports or maintains localization of
                         (ex/of5 owl/someValuesFrom ex/protein5)
                         (ex/to5 rdf/type owl/Restriction)
                         (ex/to5 owl/onProperty obo/RO_0002339) ; has target end location
                         (ex/to5 owl/someValuesFrom ex/gocc5)
                         (ex/loc5 rdfs/subClassOf ex/of5)
                         (ex/loc5 rdfs/subClassOf ex/to5)
                         
                         (ex/loc6 rdfs/subClassOf obo/GO_0051179) ;localization
                         (ex/protein6 rdfs/subClassOf ex/p1)
                         (ex/gocc6 rdfs/subClassOf ex/v)
                         (ex/of6 rdf/type owl/Restriction)
                         (ex/of6 owl/onProperty obo/RO_0002313) ;transports or maintains localization of
                         (ex/of6 owl/someValuesFrom ex/protein6)
                         (ex/to6 rdf/type owl/Restriction)
                         (ex/to6 owl/onProperty obo/RO_0002339) ; has target end location
                         (ex/to6 owl/someValuesFrom ex/gocc6)
                         (ex/loc6 rdfs/subClassOf ex/of6)
                         (ex/loc6 rdfs/subClassOf ex/to6)
                         
                         (ex/loc7 rdfs/subClassOf obo/GO_0051179) ;localization
                         (ex/protein7 rdfs/subClassOf ex/p2)
                         (ex/gocc7 rdfs/subClassOf ex/z)
                         (ex/of7 rdf/type owl/Restriction)
                         (ex/of7 owl/onProperty obo/RO_0002313) ;transports or maintains localization of
                         (ex/of7 owl/someValuesFrom ex/protein7)
                         (ex/to7 rdf/type owl/Restriction)
                         (ex/to7 owl/onProperty obo/RO_0002339) ; has target end location
                         (ex/to7 owl/someValuesFrom ex/gocc7)
                         (ex/loc7 rdfs/subClassOf ex/of7)
                         (ex/loc7 rdfs/subClassOf ex/to7)
                         
                         (ex/loc8 rdfs/subClassOf obo/GO_0051179) ;localization
                         (ex/protein8 rdfs/subClassOf ex/p3)
                         (ex/gocc8 rdfs/subClassOf ex/z)
                         (ex/of8 rdf/type owl/Restriction)
                         (ex/of8 owl/onProperty obo/RO_0002313) ;transports or maintains localization of
                         (ex/of8 owl/someValuesFrom ex/protein8)
                         (ex/to8 rdf/type owl/Restriction)
                         (ex/to8 owl/onProperty obo/RO_0002339) ; has target end location
                         (ex/to8 owl/someValuesFrom ex/gocc8)
                         (ex/loc8 rdfs/subClassOf ex/of8)
                         (ex/loc8 rdfs/subClassOf ex/to8)))

(def new-triples '())

(def result-triples '())

(defn test-kb [triples]
  "initializes an empty kb"
  (let [kb (register-namespaces (synch-ns-mappings (open (kb :sesame-mem)))
                                *namespaces*)]
    (dorun (map (partial add! kb) triples))
    kb))


(deftest test-create-shared-go-edges
  (let [rule (first (filter #(= (:name %) "aael-shared-go-cc-edges-hierarchical") (kabob-load-rules-from-classpath "rules/hanalyzer/edges/shared-go/aael")))
        ;;output-kb (test-kb '()) ;; output kb is initialized to empty
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    
    (run-forward-rule source-kb source-kb rule)

    ;; there should be 7 instances of iaohan/HAN_0000006 created
    ;; p1-p2 y (y,w)
    ;; p1-p3 y (y,z)
    ;; p1-p2 d (d,h)
    ;; p1-p2 w (w,v)
    ;; p1-p2 z (z,v)
    ;; p2-p3 z (z,z)
    ;; p1-p3 z (z,v)
    (is (= 7 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000006)))))) ;; HAN:shared_go_cc_asserted_edge

    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
                                       (?/edge iaohan/linksNode ex/han_node_1)
                                       (?/edge iaohan/linksNode ex/han_node_2)
                                       (?/edge iaohan/commonConcept ex/y)))))) 
    
    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
                                       (?/edge iaohan/linksNode ex/han_node_1)
                                       (?/edge iaohan/linksNode ex/han_node_3)
                                       (?/edge iaohan/commonConcept ex/y)))))) 
    
    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
                                       (?/edge iaohan/linksNode ex/han_node_1)
                                       (?/edge iaohan/linksNode ex/han_node_2)
                                       (?/edge iaohan/commonConcept ex/d)))))) 
    
    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
                                       (?/edge iaohan/linksNode ex/han_node_1)
                                       (?/edge iaohan/linksNode ex/han_node_2)
                                       (?/edge iaohan/commonConcept ex/w)))))) 
    
    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
                                       (?/edge iaohan/linksNode ex/han_node_1)
                                       (?/edge iaohan/linksNode ex/han_node_2)
                                       (?/edge iaohan/commonConcept ex/z)))))) 

    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
                                       (?/edge iaohan/linksNode ex/han_node_2)
                                       (?/edge iaohan/linksNode ex/han_node_3)
                                       (?/edge iaohan/commonConcept ex/z)))))) 
    
    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
                                       (?/edge iaohan/linksNode ex/han_node_1)
                                       (?/edge iaohan/linksNode ex/han_node_3)
                                       (?/edge iaohan/commonConcept ex/z)))))) 
    

    ;; The code fragment below is useful for debugging as it writes
    ;; triples to a local file.
    ;(let [log-kb (output-kb "/tmp/triples.nt")]
      ;; add sample triples to the log kb
    ;  (dorun (map (partial add! log-kb) sample-kb-triples))
      
    ;; ;;(run-forward-rule source-kb log-kb rule)
    ; (close log-kb))
    ))



  




