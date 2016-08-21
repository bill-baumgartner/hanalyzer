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
       
# need to run the shared-go-hierarchical rules to get the commonConcept relation
#?shared_go_edge iaohan:commonConcept ?go .
       
       ?shared_go_edge iaohan:denotes ?go_sc1 .
       ?shared_go_edge iaohan:denotes ?go_sc2 .
       FILTER (?go_sc1 != ?go_sc2)
       ?go_sc1 rdfs:subClassOf ?go1 .
       ?go_sc2 rdfs:subClassOf ?go2 .
       ?go1 rdfs:subClassOf* ?go2 .
       ?go2 rdfs:label ?label .
       ?ice_id obo:IAO_0000219 ?go2 .
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
       
# need to run the shared-go-hierarchical rules to get the commonConcept relation
#?shared_go_edge iaohan:commonConcept ?go .
       
       ?shared_go_edge iaohan:denotes ?go_sc1 .
       ?shared_go_edge iaohan:denotes ?go_sc2 .
       FILTER (?go_sc1 != ?go_sc2)
       ?go_sc1 rdfs:subClassOf ?go1 .
       ?go_sc2 rdfs:subClassOf ?go2 .
       ?go1 rdfs:subClassOf* ?go2 .
       ?go2 rdfs:label ?label .
       ?ice_id obo:IAO_0000219 ?go2 .
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
       
# need to run the shared-go-hierarchical rules to get the commonConcept relation
#?shared_go_edge iaohan:commonConcept ?go .
       
       ?shared_go_edge iaohan:denotes ?record1 .
       ?record1 obo:BFO_0000051 ?go_id_field .
       ?go_id_field kiao:hasTemplate iaogoa:GpAssociationGoaUniprotFileData_goIDDataField1 .
       ?go_id_field obo:IAO_0000219 ?go_ice_1 .
       ?go_ice_1 obo:IAO_0000219 ?go1 .
       
       ?shared_go_edge iaohan:denotes ?record2 .
       ?record2 obo:BFO_0000051 ?go_id_field2 .
       ?go_id_field2 kiao:hasTemplate iaogoa:GpAssociationGoaUniprotFileData_goIDDataField1 .
       ?go_id_field2 obo:IAO_0000219 ?ice_id .
       ?ice_id obo:IAO_0000219 ?go2 .
       
       FILTER (?go1 != ?go2)
       ?go1 rdfs:subClassOf* ?go2 .
       ?go2 rdfs:label ?label .
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
       ?ice_id obo:IAO_0000219 ?canonical_pathway .
       ?canonical_pathway rdfs:label ?label .
       } group by ?ice_id"))

(defn- build-id2termmappings-file [options source-string query-string-fn]
  (prn options)
  (let [source-connection (open-kb options)
        sparql-string (query-string-fn options)]
    (prn (str "Id2TermMappings.txt query for: " source-string "\n" (query-string-fn options)))
    (with-open [w (clojure.java.io/writer
                   (str (:output-directory options) "/commonattributes-plugin-files/network." source-string ".Id2TermMappings.txt"))]
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str
                                     (sym-to-long-name ('?/ice_id bindings))
                                     "\t"
                                     ('?/labels bindings)
                                     "\n")))
                          sparql-string))
        (finally (close source-connection))))))


(defn build-id2termmappings-files [options]
  (build-id2termmappings-file options "PW" pathway-id2termmappings-files-query)
  (build-id2termmappings-file options "BP" go-bp-id2termmappings-files-query)
  (build-id2termmappings-file options "CC" go-cc-id2termmappings-files-query)
  (build-id2termmappings-file options "MF" go-mf-id2termmappings-files-query)
  
  )
