(ns rules-test.hanalyzer.edges.shared-go-mf-edge-test
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

;;;            obo:GO_0003674
;;;                 / \
;;;                b   x
;;;               / \ / \
;;;              c   e   y
;;;             /   /   / \
;;;            d   f   w   z
;;;                     \ /
;;;                      v
;;;
;;; All nodes will have obo-namespace=molecular_function
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

(def sample-kb-triples '((ex/b rdfs/subClassOf obo/GO_0003674)
                         (ex/x rdfs/subClassOf obo/GO_0003674)
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
                         (obo/GO_0003674 oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/b oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/c oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/d oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/e oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/f oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/x oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/y oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/w oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/z oboInOwl/hasOBONamespace ["molecular_function"])
                         (ex/v oboInOwl/hasOBONamespace ["molecular_function"])
                         (obo/GO_0003674 rdfs/label ["molecular_function"])
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

                         ;; c-p1
                         (ex/c_ice obo/IAO_0000219 ex/c) 
                         (ex/go_id_field1 obo/IAO_0000219 ex/c_ice) 
                         (ex/go_id_field1 kiao/hasTemplate
                                          iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
                         (ex/record1 obo/BFO_0000051 ex/go_id_field1) 
                         (ex/record1 obo/BFO_0000051 ex/protein_id_field1) 
                         (ex/protein_id_field1 kiao/hasTemplate
                                               iaogoa/GpAssociationGoaUniprotFileData_databaseObjectIDDataField1)
                         (ex/protein_id_field1 obo/IAO_0000219 ex/p1_ice) 
                         (ex/p1_ice obo/IAO_0000219 ex/p1)

                         ;; d-p2
                         (ex/d_ice obo/IAO_0000219 ex/d) 
                         (ex/go_id_field_d obo/IAO_0000219 ex/d_ice) 
                         (ex/go_id_field_d kiao/hasTemplate
                                          iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
                         (ex/record2 obo/BFO_0000051 ex/go_id_field_d) 
                         (ex/record2 obo/BFO_0000051 ex/protein_id_field2) 
                         (ex/protein_id_field2 kiao/hasTemplate
                                               iaogoa/GpAssociationGoaUniprotFileData_databaseObjectIDDataField1)
                         (ex/protein_id_field2 obo/IAO_0000219 ex/p2_ice) 
                         (ex/p2_ice obo/IAO_0000219 ex/p2)

                         ;; f-p3
                         (ex/f_ice obo/IAO_0000219 ex/f) 
                         (ex/go_id_field3 obo/IAO_0000219 ex/f_ice) 
                         (ex/go_id_field3 kiao/hasTemplate
                                          iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
                         (ex/record3 obo/BFO_0000051 ex/go_id_field3) 
                         (ex/record3 obo/BFO_0000051 ex/protein_id_field3) 
                         (ex/protein_id_field3 kiao/hasTemplate
                                               iaogoa/GpAssociationGoaUniprotFileData_databaseObjectIDDataField1)
                         (ex/protein_id_field3 obo/IAO_0000219 ex/p3_ice) 
                         (ex/p3_ice obo/IAO_0000219 ex/p3)

                         ;; y-p1
                         (ex/y_ice obo/IAO_0000219 ex/y) 
                         (ex/go_id_field4 obo/IAO_0000219 ex/y_ice) 
                         (ex/go_id_field4 kiao/hasTemplate
                                          iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
                         (ex/record4 obo/BFO_0000051 ex/go_id_field4) 
                         (ex/record4 obo/BFO_0000051 ex/protein_id_field1) 

                         ;; w-p2
                         (ex/w_ice obo/IAO_0000219 ex/w) 
                         (ex/go_id_field5 obo/IAO_0000219 ex/w_ice) 
                         (ex/go_id_field5 kiao/hasTemplate
                                          iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
                         (ex/record5 obo/BFO_0000051 ex/go_id_field5) 
                         (ex/record5 obo/BFO_0000051 ex/protein_id_field2) 

                         ;; v-p1
                         (ex/v_ice obo/IAO_0000219 ex/v) 
                         (ex/go_id_field6 obo/IAO_0000219 ex/v_ice) 
                         (ex/go_id_field6 kiao/hasTemplate
                                          iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
                         (ex/record6 obo/BFO_0000051 ex/go_id_field6) 
                         (ex/record6 obo/BFO_0000051 ex/protein_id_field1) 

                         ;; z-p2
                         (ex/z_ice obo/IAO_0000219 ex/z) 
                         (ex/go_id_field_z obo/IAO_0000219 ex/z_ice) 
                         (ex/go_id_field_z kiao/hasTemplate
                                          iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
                         (ex/record7 obo/BFO_0000051 ex/go_id_field_z) 
                         (ex/record7 obo/BFO_0000051 ex/protein_id_field2) 

                         ;; z-p3
                         (ex/record8 obo/BFO_0000051 ex/go_id_field_z) 
                         (ex/record8 obo/BFO_0000051 ex/protein_id_field3)

                         ;; NOT d-p3
                         (ex/record9 obo/BFO_0000051 ex/go_id_field_d) 
                         (ex/record9 obo/BFO_0000051 ex/protein_id_field3)
                         (ex/record9 obo/BFO_0000051 ex/qualifier_field)
                         (ex/qualifier_field kiao/hasTemplate iaogoa/GpAssociationGoaUniprotFileData_qualifierDataField1)
                         (ex/qualifier_field obo/IAO_0000219 ["NOT involved_in"])

                         )) 


(def new-triples '())

(def result-triples '())

(defn test-kb [triples]
  "initializes an empty kb"
  (let [kb (register-namespaces (synch-ns-mappings (open (kb :sesame-mem)))
                                *namespaces*)]
    (dorun (map (partial add! kb) triples))
    kb))

(deftest test-create-shared-go-edges
  (let [rule (first (filter #(= (:name %) "aael-shared-go-mf-edges")
                            (kabob-load-rules-from-classpath
                             "rules/hanalyzer/edges/shared-go/aael")))
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    
    (run-forward-rule source-kb source-kb rule)

    ;; there should be 1 instances of iaohan/annotation-count-aael
    (is (= 1 (count (query source-kb '((?/edge rdf/type iaohan/SharedGoMfEdge)
                                       (?/edge iaohan/linksNode ex/han_node_2)
                                       (?/edge iaohan/linksNode ex/han_node_3)
                                       (?/edge iaohan/denotes ex/record7)
                                       (?/edge iaohan/denotes ex/record8))))))
    
    ;; The code fragment below is useful for debugging as it writes
    ;; triples to a local file.
    (let [log-kb (output-kb "/tmp/triples.nt")]
      ;; add sample triples to the log kb
      (dorun (map (partial add! log-kb) sample-kb-triples))
      
    ;; ;;(run-forward-rule source-kb log-kb rule)
     (close log-kb))
    ))



    
    

  




