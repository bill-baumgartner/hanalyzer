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
;;; BUILD NodeIds.txt FILES
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
                          (.write w (str ('?/ice_id bindings)
                                         "\t"
                                         ('?/labels bindings)
                                         "\n")))
                          sparql-string))
        (finally (close source-connection))))))


(defn build-id2termmappings-files [options]
  (build-id2termmappings-file options "PW" pathway-id2termmappings-files-query)  
  )
