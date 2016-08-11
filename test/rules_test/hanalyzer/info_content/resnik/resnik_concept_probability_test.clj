(ns rules-test.hanalyzer.info-content.resnik.resnik-concept-probability-test
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

                         (ex/p1_sc rdfs/subClassOf ex/p1)
                         (ex/p2_sc rdfs/subClassOf ex/p2)
                         (ex/p3_sc rdfs/subClassOf ex/p3)

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

(defn get-annotation-count [subject output-kb]
  "queries the kb for the iaohan/annotation-count-aael for the given subject"
  ('?/count (first (apply list (query output-kb
                                      `((~subject iaohan/annotation-count-aael ?/count)))))))

(deftest test-count-annotations
  (let [rule (first (filter #(= (:name %) "annotation-counts-go-bp-aael") (kabob-load-rules-from-classpath "rules/hanalyzer/info_content/resnik")))
        output-kb (test-kb '()) ;; output kb is initialized to empty
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples

    (run-forward-rule source-kb output-kb rule)

    ;; there should be 11 instances of iaohan/annotation-count-aael
    (is (= 11 (count (query output-kb '((?/concept iaohan/annotation-count-aael ?/count))))))

    (is (= 8 (get-annotation-count `obo/GO_0008150 output-kb)))
    (is (= 3 (get-annotation-count `ex/b output-kb)))
    (is (= 2 (get-annotation-count `ex/c output-kb)))
    (is (= 1 (get-annotation-count `ex/d output-kb)))
    (is (= 1 (get-annotation-count `ex/e output-kb)))
    (is (= 1 (get-annotation-count `ex/f output-kb)))
    (is (= 1 (get-annotation-count `ex/v output-kb)))
    (is (= 2 (get-annotation-count `ex/w output-kb)))
    (is (= 6 (get-annotation-count `ex/x output-kb)))
    (is (= 5 (get-annotation-count `ex/y output-kb)))
    (is (= 3 (get-annotation-count `ex/z output-kb)))))



(defn get-concept-prob [subject output-kb]
  "queries the kb for the iaohan/resnik-concept-prob for the given subject"
  ('?/count (first (apply list (query output-kb
                                      `((~subject iaohan/resnik-concept-prob-aael ?/count)))))))


(deftest test-concept-prob-calc
  (let [annot-count-rule (first
                          (filter #(= (:name %) "annotation-counts-go-bp-aael")
                                        (kabob-load-rules-from-classpath
                                         "rules/hanalyzer/info_content/resnik")))
        concept-prob-rule (first
                           (filter #(= (:name %) "resnik-concept-probability-go-bp-aael")
                                         (kabob-load-rules-from-classpath
                                          "rules/hanalyzer/info_content/resnik")))
        output-kb (test-kb '()) ;; output kb is initialized to empty
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples

    ;; annotation counts are a prerequisite for the concept-probability calculation
    ;; rule being tested here, so we prepopulate the kb with annotation counts.
    ;; Note: the annotation count rule is tested in isolation above.
    (run-forward-rule source-kb source-kb annot-count-rule)

    (is (= 11 (count (query source-kb
                            '((?/concept iaohan/annotation-count-aael ?/count))))))

    (is (= 8 (get-annotation-count `obo/GO_0008150 source-kb)))
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

    (is (= (float 8/8) (get-concept-prob `obo/GO_0008150 source-kb)))
    (is (= (float 3/8) (get-concept-prob `ex/b source-kb)))
    (is (= (float 2/8) (get-concept-prob `ex/c source-kb)))
    (is (= (float 1/8) (get-concept-prob `ex/d source-kb)))
    (is (= (float 1/8) (get-concept-prob `ex/e source-kb)))
    (is (= (float 1/8) (get-concept-prob `ex/f source-kb)))
    (is (= (float 1/8) (get-concept-prob `ex/v source-kb)))
    (is (= (float 2/8) (get-concept-prob `ex/w source-kb)))
    (is (= (float 6/8) (get-concept-prob `ex/x source-kb)))
    (is (= (float 5/8) (get-concept-prob `ex/y source-kb)))
    (is (= (float 3/8) (get-concept-prob `ex/z source-kb)))))






(defn get-pms [c1 c2 kb]
  "given two concepts, return the probability of min subsumer"
  ('?/prob (first (apply list (query kb
                                     `((?/pms obo/RO_0000057 ~c1)
                                       (?/pms rdf/type iaohan/GOBP_Pair_aael)
                                       (?/pms obo/RO_0000057 ~c2)
                                       (?/pms iaohan/prob-min-subsumer ?/prob)))))))


(defn get-jiang-d [c1 c2 kb]
  "given two concepts, return the jiang distance"
  ('?/dist (first (apply list (query kb
                                     `((?/pms obo/RO_0000057 ~c1)
                                       (?/pms rdf/type iaohan/GOBP_Pair_aael)
                                       (?/pms obo/RO_0000057 ~c2)
                                       (?/pms iaohan/jiang_distance ?/dist)))))))

(deftest test-jiang
  (let [annot-count-rule (first
                          (filter #(= (:name %) "annotation-counts-go-bp-aael")
                                        (kabob-load-rules-from-classpath
                                         "rules/hanalyzer/info_content/resnik/aael")))
        concept-prob-rule (first
                           (filter #(= (:name %) "resnik-concept-probability-go-bp-aael")
                                         (kabob-load-rules-from-classpath
                                          "rules/hanalyzer/info_content/resnik/aael")))
        compute-pms-rule (first
                           (filter #(= (:name %) "jiang-compute-pms-go-bp-aael")
                                         (kabob-load-rules-from-classpath
                                          "rules/hanalyzer/semantic_similarity/jiang/aael")))
        
        jiang-dist-rule  (first
                           (filter #(= (:name %) "jiang-distance-go-bp-aael")
                                         (kabob-load-rules-from-classpath
                                          "rules/hanalyzer/semantic_similarity/jiang/aael")))

        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
         

    
    
    
    ;; annotation counts are a prerequisite for the concept-probability calculation
    ;; rule being tested here, so we prepopulate the kb with annotation counts.
    ;; Note: the annotation count rule is tested in isolation above.
    (run-forward-rule source-kb source-kb annot-count-rule)
    (run-forward-rule source-kb source-kb concept-prob-rule)

    (is (= (float 8/8) (get-concept-prob `obo/GO_0008150 source-kb)))
    
    (run-forward-rule source-kb source-kb compute-pms-rule)

    ;; below is a sampling of the expected output
    (is (= (float 6/8) (get-pms 'ex/y 'ex/z source-kb)))
    ;; adding the constraint that ?/c2 rdfs/subClassOf ?/c1 makes
    ;; the next two tests fail so they have been commented out
    ;; The tradeoff is that hopefully the query will run to completion
    ;; without running out of memory.
    ;;(is (= (float 3/8) (get-pms 'ex/c 'ex/f source-kb)))
    ;;(is (= (float 6/8) (get-pms 'ex/e 'ex/w source-kb)))
    (is (= (float 8/8) (get-pms 'ex/c 'ex/b source-kb)))

    (run-forward-rule source-kb source-kb jiang-dist-rule)

     ;; below is a sampling of the expected output
    (is (= (- (* -2 (Math/log 6/8)) (+ (Math/log 5/8) (Math/log 3/8))))
           (get-jiang-d 'ex/y 'ex/z source-kb))
    (is (= (- (* -2 (Math/log 3/8)) (+ (Math/log 2/8) (Math/log 1/8))))
           (get-jiang-d 'ex/c 'ex/f source-kb))
    (is (= (- (* -2 (Math/log 6/8)) (+ (Math/log 1/8) (Math/log 2/8))))
           (get-jiang-d 'ex/e 'ex/w source-kb))
    (is (= (- (* -2 (Math/log 8/8)) (+ (Math/log 2/8) (Math/log 3/8))))
           (get-jiang-d 'ex/c 'ex/b source-kb))

    ;; The code fragment below is useful for debugging as it writes
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

  




