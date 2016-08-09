
(ns rules-test.hanalyzer.node-construction-test
  (use clojure.test
        edu.ucdenver.ccp.kr.sesame.kb
        edu.ucdenver.ccp.kr.sesame.sparql
        edu.ucdenver.ccp.kr.sesame.rdf
        )
  (:require  [edu.ucdenver.ccp.kabob.build.run-rules :refer [query-variables run-forward-rule]]
             [edu.ucdenver.ccp.kr.forward-rule :refer [add-reify-fns]]
             [edu.ucdenver.ccp.kr.sparql :refer [sparql-select-query query]]
             [edu.ucdenver.ccp.kr.rdf :refer [register-namespaces synch-ns-mappings add!]]
             [edu.ucdenver.ccp.kr.kb :refer [kb open]]
             [edu.ucdenver.ccp.kabob.namespace :refer [*namespaces*]]
             [edu.ucdenver.ccp.kabob.rule :refer [kabob-load-rules-from-classpath]]
             [kabob]
             [clojure.pprint :refer [pprint]]))


;;; --------------------------------------------------------
;;; Set up a test KB below to hold two GGPV's, one with a
;;; gene and a protein, and one with only a gene. Using this
;;; small KB, two hanalyzer nodes should be created.
;;; --------------------------------------------------------

;;;                 kbio:GeneSpecificGorGPorVClass
;;;                  |rdf/type                 |rdf/type
;;;           kbio:GorGPorV_BIO_1       kbio:GorGPorV_BIO_2
;;;                  |                         |
;;;            kbio:GorGP_BIO_1          kbio:GorGP_BIO_2
;;;                 / \                       /
;;;                /   \                     /
;;;               /     \                   /
;;;       kbio:Gene_1  kbio:Protein_1   kbio:Gene_2
;;;               \     /                 /
;;;                \   /               __/
;;;                 \ /               /  
;;;                  R--in_taxon--AAEL
;;;                  


(def sample-kb-triples '((kbio/GorGPorV_BIO_1 rdf/type kbio/GeneSpecificGorGPorVClass)
                         (kbio/GorGPorV_BIO_2 rdf/type kbio/GeneSpecificGorGPorVClass)
                         (kbio/GorGP_BIO_1 rdfs/subClassOf kbio/GorGPorV_BIO_1)
                         (kbio/GorGP_BIO_2 rdfs/subClassOf kbio/GorGPorV_BIO_2)
                         (kbio/Gene_1 rdfs/subClassOf kbio/GorGP_BIO_1)
                         (kbio/Gene_1 rdfs/label "gene 1")
                         (kbio/Protein_1 rdfs/subClassOf kbio/GorGP_BIO_1)
                         (kbio/Protein_1 rdfs/label "protein 1")
                         (kbio/Gene_2  rdfs/subClassOf kbio/GorGP_BIO_2)
                         (kbio/Gene_2 rdfs/label "gene 2")
                         (kbio/R_taxon_7159 rdfs/subClassOf owl/Class)
                         (kbio/R_taxon_7159 rdf/type owl/Restriction)
                         (kbio/R_taxon_7159 owl/onProperty obo/RO_0002162) ;; obo:in_taxon
                         (kbio/R_taxon_7159 owl/someValuesFrom obo/NCBITaxon_7159)
                         (kbio/Gene_1 rdfs/subClassOf kbio/R_taxon_7159)
                         (kbio/Protein_1 rdfs/subClassOf kbio/R_taxon_7159)
                         (kbio/Gene_2 rdfs/subClassOf kbio/R_taxon_7159)))


(def new-triples '())

(def result-triples '())

(defn test-kb [triples]
  "initializes an empty kb"
  (let [kb (register-namespaces (synch-ns-mappings (open (kb :sesame-mem)))
                                *namespaces*)]
    (dorun (map (partial add! kb) triples))
    kb))


(deftest test-hanalyzer-node-construction
  "Using the sample triples above that define a KB with 2 GGPs, this test
   exercises the nodes/aael rule to check that 2 separate hanalyzer nodes
   are created."
  (let [rule (first (kabob-load-rules-from-classpath "rules/hanalyzer/nodes/aael"))
        output-kb (test-kb '()) ;; output kb is initialized to empty
        source-kb (test-kb sample-kb-triples)] ;; source kb contains sample triples
    (run-forward-rule source-kb output-kb rule)
    (is (= 2 (count (query output-kb '((?/n rdf/type iaohan/Node))))))
    ))


