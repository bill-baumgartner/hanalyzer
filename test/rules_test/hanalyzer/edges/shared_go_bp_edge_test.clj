(ns rules-test.hanalyzer.edges.shared-go-bp-edge-test
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

;;;            obo:GO_0008150
;;;                 / \
;;;                b   x
;;;               / \ / \
;;;              c   e   y
;;;             /   /   / \
;;;            d   f   w   z
;;;                     \ /
;;;                      v
;;;
;;; All nodes will have obo-namespace=biological_process
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

(def sample-kb-triples '((ex/b rdfs/subClassOf obo/GO_0008150)
                         (ex/x rdfs/subClassOf obo/GO_0008150)
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
                         (obo/GO_0008150 oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/b oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/c oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/d oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/e oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/f oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/x oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/y oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/w oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/z oboInOwl/hasOBONamespace ["biological_process"])
                         (ex/v oboInOwl/hasOBONamespace ["biological_process"])
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

                         ;; the rule that creates shared go edges uses
                         ;; the resnik concept probability as a
                         ;; threshold to keep from creating edges
                         ;; based on very general concepts, e.g. the
                         ;; root biological_process concept. Here we
                         ;; set the resnik concept probability for
                         ;; each concept to be 0.001
                         (obo/GO_0008150 iaohan/resnik-concept-prob-aael 0.001)
                         (ex/b iaohan/resnik-concept-prob-aael 0.001)
                         (ex/c iaohan/resnik-concept-prob-aael 0.001)
                         (ex/d iaohan/resnik-concept-prob-aael 0.001)
                         (ex/e iaohan/resnik-concept-prob-aael 0.001)
                         (ex/f iaohan/resnik-concept-prob-aael 0.001)
                         (ex/v iaohan/resnik-concept-prob-aael 0.001)
                         (ex/w iaohan/resnik-concept-prob-aael 0.001)
                         (ex/x iaohan/resnik-concept-prob-aael 0.001)
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
                                                  
                         (ex/c_sc rdfs/subClassOf ex/c)
                         (ex/c_sc rdfs/subClassOf ex/r1)
                         (ex/r1 rdf/type owl/Restriction)
                         (ex/r1 owl/onProperty obo/RO_0000057)
                         (ex/r1 owl/someValuesFrom ex/p1_sc)
                         
                         (ex/d_sc rdfs/subClassOf ex/d)
                         (ex/d_sc rdfs/subClassOf ex/r2)
                         (ex/r2 rdf/type owl/Restriction)
                         (ex/r2 owl/onProperty obo/RO_0000057)
                         (ex/r2 owl/someValuesFrom ex/p2_sc)
                         
                         (ex/f_sc rdfs/subClassOf ex/f)
                         (ex/f_sc rdfs/subClassOf ex/r3)
                         (ex/r3 rdf/type owl/Restriction)
                         (ex/r3 owl/onProperty obo/RO_0000057)
                         (ex/r3 owl/someValuesFrom ex/p3_sc)
                         
                         (ex/y_sc rdfs/subClassOf ex/y)
                         (ex/y_sc rdfs/subClassOf ex/r4)
                         (ex/r4 rdf/type owl/Restriction)
                         (ex/r4 owl/onProperty obo/RO_0000057)
                         (ex/r4 owl/someValuesFrom ex/p1_sc)
                         
                         (ex/w_sc rdfs/subClassOf ex/w)
                         (ex/w_sc rdfs/subClassOf ex/r5)
                         (ex/r5 rdf/type owl/Restriction)
                         (ex/r5 owl/onProperty obo/RO_0000057)
                         (ex/r5 owl/someValuesFrom ex/p2_sc)
                         
                         (ex/v_sc rdfs/subClassOf ex/v)
                         (ex/v_sc rdfs/subClassOf ex/r6)
                         (ex/r6 rdf/type owl/Restriction)
                         (ex/r6 owl/onProperty obo/RO_0000057)
                         (ex/r6 owl/someValuesFrom ex/p1_sc)
                         
                         (ex/z_sc1 rdfs/subClassOf ex/z)
                         (ex/z_sc1 rdfs/subClassOf ex/r7)
                         (ex/r7 rdf/type owl/Restriction)
                         (ex/r7 owl/onProperty obo/RO_0000057)
                         (ex/r7 owl/someValuesFrom ex/p2_sc)
                         
                         (ex/z_sc2 rdfs/subClassOf ex/z)
                         (ex/z_sc2 rdfs/subClassOf ex/r8)
                         (ex/r8 rdf/type owl/Restriction)
                         (ex/r8 owl/onProperty obo/RO_0000057)
                         (ex/r8 owl/someValuesFrom ex/p3_sc)))


(def new-triples '())

(def result-triples '())

(defn test-kb [triples]
  "initializes an empty kb"
  (let [kb (register-namespaces (synch-ns-mappings (open (kb :sesame-mem)))
                                *namespaces*)]
    (dorun (map (partial add! kb) triples))
    kb))

(deftest test-create-shared-go-edges
  (let [rule (first (filter #(= (:name %) "aael-shared-go-bp-edges") (kabob-load-rules-from-classpath "rules/hanalyzer/edges/shared-go/aael")))
        ;;output-kb (test-kb '()) ;; output kb is initialized to empty
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    
    (run-forward-rule source-kb source-kb rule)

    ;; there should be 1 instances of iaohan/annotation-count-aael
    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/HAN_0000007)))))) ;; HAN:shared_go_bp_asserted_edge
    
    ;; The code fragment below is useful for debugging as it writes
    ;; triples to a local file.
    ;;(let [log-kb (output-kb "/tmp/triples.nt")]
      ;; add sample triples to the log kb
    ;;  (dorun (map (partial add! log-kb) sample-kb-triples))
      
    ;; ;;(run-forward-rule source-kb log-kb rule)
    ;; (close log-kb))
    ))



    
    

  




