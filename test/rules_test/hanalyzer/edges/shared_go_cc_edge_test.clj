(ns rules-test.hanalyzer.edges.shared-go-cc-edge-test
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
;;;                b   x
;;;               / \ / \
;;;              c   e   y
;;;             /   /   / \
;;;            d   f   w   z
;;;                     \ /
;;;                      v
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
                         (ex/x rdfs/label ["x"])
                         (ex/y rdfs/label ["y"])
                         (ex/w rdfs/label ["w"])
                         (ex/z rdfs/label ["z"])
                         (ex/v rdfs/label ["v"])
                         
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
                         (ex/gocc1 rdfs/subClassOf ex/c)
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
  (let [rule (first (filter #(= (:name %) "aael-shared-go-cc-edges") (kabob-load-rules-from-classpath "rules/hanalyzer/edges/shared-go/aael")))
        ;;output-kb (test-kb '()) ;; output kb is initialized to empty
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    
    (run-forward-rule source-kb source-kb rule)

    (prn (str "RESULTS: " (pr-str (query source-kb '((?/edge rdf/type iaohan/SharedGoCcEdge)
                                                     (?/edge iaohan/linksNode ?/node))))))
    
    ;; there should be 1 instances of iaohan/annotation-count-aael
    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/SharedGoCcEdge))))))
    
    ;;    (is (= 8 (get-annotation-count `obo/GO_0008150 output-kb)))

    ;; The code fragment below is useful for debugging as it writes
    ;; triples to a local file.
    (let [log-kb (output-kb "/tmp/triples.nt")]
      ;; add sample triples to the log kb
      (dorun (map (partial add! log-kb) sample-kb-triples))
      
    ;; ;;(run-forward-rule source-kb log-kb rule)
     (close log-kb))
    ))



  




