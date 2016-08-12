(ns rules-test.hanalyzer.info-content.resnik.resnik-go-mf-test
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

                         (ex/p1_sc rdfs/subClassOf ex/p1)
                         (ex/p2_sc rdfs/subClassOf ex/p2)
                         (ex/p3_sc rdfs/subClassOf ex/p3)

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

(defn get-annotation-count [subject output-kb]
  "queries the kb for the iaohan/annotation-count-aael for the given subject"
  ('?/count (first (apply list (query output-kb
                                      `((~subject iaohan/annotation-count-aael ?/count)))))))

(deftest test-count-annotations
  (let [rule (first (filter #(= (:name %) "annotation-counts-go-mf-aael") (kabob-load-rules-from-classpath "rules/hanalyzer/info_content/resnik")))
        ;;output-kb (test-kb '()) ;; output kb is initialized to empty
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples

    (run-forward-rule source-kb source-kb rule)

    ;; there should be 11 instances of iaohan/annotation-count-aael
    (is (= 11 (count (query source-kb '((?/concept iaohan/annotation-count-aael ?/count))))))

    (is (= 8 (get-annotation-count `obo/GO_0003674 source-kb)))
    (is (= 3 (get-annotation-count `ex/b source-kb)))
    (is (= 2 (get-annotation-count `ex/c source-kb)))
    (is (= 1 (get-annotation-count `ex/d source-kb)))
    (is (= 1 (get-annotation-count `ex/e source-kb)))
    (is (= 1 (get-annotation-count `ex/f source-kb)))
    (is (= 1 (get-annotation-count `ex/v source-kb)))
    (is (= 2 (get-annotation-count `ex/w source-kb)))
    (is (= 6 (get-annotation-count `ex/x source-kb)))
    (is (= 5 (get-annotation-count `ex/y source-kb)))
    (is (= 3 (get-annotation-count `ex/z source-kb)))))

(defn get-concept-prob [subject output-kb]
  "queries the kb for the iaohan/resnik-concept-prob for the given subject"
  ('?/count (first (apply list (query output-kb
                                      `((~subject iaohan/resnik-concept-prob-aael ?/count)))))))


(deftest test-concept-prob-calc
  (let [annot-count-rule (first
                          (filter #(= (:name %) "annotation-counts-go-mf-aael")
                                        (kabob-load-rules-from-classpath
                                         "rules/hanalyzer/info_content/resnik")))
        concept-prob-rule (first
                           (filter #(= (:name %) "resnik-concept-probability-go-mf-aael")
                                         (kabob-load-rules-from-classpath
                                          "rules/hanalyzer/info_content/resnik")))
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples

    ;; annotation counts are a prerequisite for the
    ;; concept-probability calculation rule being tested here, so we
    ;; prepopulate the kb with annotation counts.  Note: the
    ;; annotation count rule is tested in isolation above.
    (run-forward-rule source-kb source-kb annot-count-rule)

    (is (= 11 (count (query source-kb
                            '((?/concept iaohan/annotation-count-aael ?/count))))))

    (is (= 8 (get-annotation-count `obo/GO_0003674 source-kb)))
    (is (= 3 (get-annotation-count `ex/b source-kb)))
    (is (= 2 (get-annotation-count `ex/c source-kb)))
    (is (= 1 (get-annotation-count `ex/d source-kb)))
    (is (= 1 (get-annotation-count `ex/e source-kb)))
    (is (= 1 (get-annotation-count `ex/f source-kb)))
    (is (= 1 (get-annotation-count `ex/v source-kb)))
    (is (= 2 (get-annotation-count `ex/w source-kb)))
    (is (= 6 (get-annotation-count `ex/x source-kb)))
    (is (= 5 (get-annotation-count `ex/y source-kb)))
    (is (= 3 (get-annotation-count `ex/z source-kb)))

    (run-forward-rule source-kb source-kb concept-prob-rule)

    ;; there should be 11 instances of iaohan/annotation-count-aael
    (is (= 11 (count (query source-kb '((?/concept iaohan/resnik-concept-prob-aael ?/count))))))

    (is (= (float 8/8) (get-concept-prob `obo/GO_0003674 source-kb)))
    (is (= (float 3/8) (get-concept-prob `ex/b source-kb)))
    (is (= (float 2/8) (get-concept-prob `ex/c source-kb)))
    (is (= (float 1/8) (get-concept-prob `ex/d source-kb)))
    (is (= (float 1/8) (get-concept-prob `ex/e source-kb)))
    (is (= (float 1/8) (get-concept-prob `ex/f source-kb)))
    (is (= (float 1/8) (get-concept-prob `ex/v source-kb)))
    (is (= (float 2/8) (get-concept-prob `ex/w source-kb)))
    (is (= (float 6/8) (get-concept-prob `ex/x source-kb)))
    (is (= (float 5/8) (get-concept-prob `ex/y source-kb)))
    (is (= (float 3/8) (get-concept-prob `ex/z source-kb)))

    

    ;; triples to a local file.
    ;; (let [log-kb (output-kb "/tmp/triples.nt")]
    ;;   ;; add sample triples to the log kb
    ;;   (dorun (map (partial add! log-kb) sample-kb-triples))
      
    ;;   (run-forward-rule source-kb log-kb annot-count-rule)
    ;;   (run-forward-rule source-kb log-kb concept-prob-rule)
    ;;   (run-forward-rule source-kb log-kb compute-pms-rule)
    ;;   (run-forward-rule source-kb log-kb jiang-dist-rule)
    ;;   (close log-kb))
    ))



  




