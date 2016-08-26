(ns hanalyzer.export.renodoi-id2termmappings-file-gen
  (:use [edu.ucdenver.ccp.kr.sesame.kb]
        [edu.ucdenver.ccp.kr.sesame.sparql]
        [edu.ucdenver.ccp.kr.sesame.rdf]
        [edu.ucdenver.ccp.kr.kb]
        [edu.ucdenver.ccp.kr.rdf]
        [edu.ucdenver.ccp.kr.sparql])
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [edu.ucdenver.ccp.kr.kb :refer [*kb*]]
            [edu.ucdenver.ccp.kr.rdf :refer [sym-to-long-name *ns-map-to-long*]]
            [edu.ucdenver.ccp.kabob.build.input-kb :refer [open-kb]]))


;;; =======================
;;; Biological Process query
;;; =======================

(defn- go-bp-id2termmappings-files-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?ice_id (group_concat(distinct ?label ; separator = ',') as ?labels) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_go_edge iaohan:linksNode ?node .
       ?shared_go_edge rdf:type iaohan:HAN_0000007 . # HAN:shared_go_bp_edge
       ?shared_go_edge iaohan:commonConcept ?go .
       ?go rdfs:label ?label.
       ?ice obo:IAO_0000219 ?go .
       bind(replace(replace(str(?ice),'http://kabob.ucdenver.edu/iao/go/GO_','GO:'),'_ICE','') as ?ice_id)
       } group by ?ice_id"))

;;; =======================
;;; Cellular Component query
;;; =======================

(defn- go-cc-id2termmappings-files-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?ice_id (group_concat(distinct ?label ; separator = ',') as ?labels) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_go_edge iaohan:linksNode ?node .
       ?shared_go_edge rdf:type iaohan:HAN_0000006 . # HAN:shared_go_bp_edge
       ?shared_go_edge iaohan:commonConcept ?go .
       ?go rdfs:label ?label.
       ?ice obo:IAO_0000219 ?go .
       bind(replace(replace(str(?ice),'http://kabob.ucdenver.edu/iao/go/GO_','GO:'),'_ICE','') as ?ice_id)
       } group by ?ice_id"))

;;; =======================
;;; Molecular Function query
;;; =======================

(defn- go-mf-id2termmappings-files-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX iaogoa: <http://kabob.ucdenver.edu/iao/goa/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?ice_id (group_concat(distinct ?label ; separator = ',') as ?labels) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_go_edge iaohan:linksNode ?node .
       ?shared_go_edge rdf:type iaohan:HAN_0000005 . # HAN:shared_go_bp_edge
       ?shared_go_edge iaohan:commonConcept ?go .
       ?go rdfs:label ?label.
       ?ice obo:IAO_0000219 ?go .
       bind(replace(replace(str(?ice),'http://kabob.ucdenver.edu/iao/go/GO_','GO:'),'_ICE','') as ?ice_id)
       } group by ?ice_id"))

;;; =======================
;;; Pathway query
;;; =======================

(defn- pathway-id2termmappings-files-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?ice_id (group_concat(distinct ?label ; separator = ',') as ?labels) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_pathway_edge iaohan:linksNode ?node .
       ?shared_pathway_edge rdf:type iaohan:HAN_0000008 . # HAN:shared_pathway_asserted_edge
       ?shared_pathway_edge iaohan:denotes ?pathway .
       ?pathway rdfs:subClassOf ?canonical_pathway .
       ?ice obo:IAO_0000219 ?canonical_pathway .
       bind(replace(replace(str(?ice),
            'http://kabob.ucdenver.edu/iao/kegg/KEGG_PATHWAY_',''),'_ICE','') as ?ice_id)
       ?canonical_pathway rdfs:label ?label .
       } group by ?ice_id"))

(defn- build-id2termmappings-file [options source-string query-string-fn]
  (prn (str "Building Id2TermMappings file [" source-string "]..."))
  (let [source-connection (open-kb options)
        sparql-string (query-string-fn options)
        output-file-name (str (:output-directory options)
                              "/commonattributes-plugin-files/network."
                              source-string ".Id2TermMappings.txt")]
    (clojure.java.io/make-parents output-file-name)
    (with-open [w (clojure.java.io/writer output-file-name)]
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str
                                     ;;(sym-to-long-name ('?/ice_id bindings))
                                     ('?/ice_id bindings)
                                     "\t"
                                     ('?/labels bindings)
                                     "\n")))
                          sparql-string))
        (finally (close source-connection))))))


(defn build-id2termmappings-files [options]
  (build-id2termmappings-file options "Kegg" pathway-id2termmappings-files-query)
  (build-id2termmappings-file options "GeneOntology_BP" go-bp-id2termmappings-files-query)
  (build-id2termmappings-file options "GeneOntology_CC" go-cc-id2termmappings-files-query)
  (build-id2termmappings-file options "GeneOntology_MF" go-mf-id2termmappings-files-query))

