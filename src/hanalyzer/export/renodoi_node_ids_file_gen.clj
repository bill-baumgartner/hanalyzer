(ns hanalyzer.export.renodoi-node-ids-file-gen
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
;;; Pathway query
;;; =======================

(defn- pathway-nodeIds-files-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node (group_concat(distinct ?ice_id ; separator = ',') as ?ice_ids) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_pathway_edge iaohan:linksNode ?node .
       ?shared_pathway_edge rdf:type iaohan:HAN_0000008 . # HAN:shared_pathway_asserted_edge
       ?shared_pathway_edge iaohan:denotes ?pathway .
       ?pathway rdfs:subClassOf ?canonical_pathway .
       ?ice_id obo:IAO_0000219 ?canonical_pathway .
       }
       group by ?node"))

;;; =======================
;;; Biological Process query
;;; =======================

(defn- go-bp-nodeIds-files-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node (group_concat(distinct ?ice_id ; separator = ',') as ?ice_ids) {
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
       ?ice_id obo:IAO_0000219 ?go2 .
       }
       group by ?node"))


;;; =======================
;;; Biological Process query
;;; =======================

(defn- go-cc-nodeIds-files-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node (group_concat(distinct ?ice_id ; separator = ',') as ?ice_ids) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_go_edge iaohan:linksNode ?node .
       ?shared_go_edge rdf:type iaohan:HAN_0000006 . # HAN:shared_go_cc_edge

# need to run the shared-go-hierarchical rules to get the commonConcept relation
#?shared_go_edge iaohan:commonConcept ?go .
       
       ?shared_go_edge iaohan:denotes ?go_sc1 .
       ?shared_go_edge iaohan:denotes ?go_sc2 .
       FILTER (?go_sc1 != ?go_sc2)
       ?go_sc1 rdfs:subClassOf ?go1 .
       ?go_sc2 rdfs:subClassOf ?go2 .
       ?go1 rdfs:subClassOf* ?go2 .
       ?ice_id obo:IAO_0000219 ?go2 .
       }
       group by ?node"))


(defn- go-mf-nodeIds-files-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX iaogoa: <http://kabob.ucdenver.edu/iao/goa/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node (group_concat(distinct ?ice_id ; separator = ',') as ?ice_ids) {
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
       } group by ?node"))

(defn- build-node-ids-file [options source-string query-string-fn]
  (prn options)
  (let [source-connection (open-kb options)
        sparql-string (query-string-fn options)]
    (prn (str "NodeIDs.txt query for: " source-string "\n" (query-string-fn options)))
    (with-open [w (clojure.java.io/writer
                   (str (:output-directory options) "/commonattributes-plugin-files/network." source-string ".NodeIds.txt"))]
      (.write w (str source-string " URLBASE:http://not/specified URLEND:\n"))
      (.write w (str "Mapping File: network." source-string ".Id2TermMappings.txt\n"))
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str ('?/node bindings)
                                         " = "
                                         ('?/ice_ids bindings)
                                         "\n")))
                          sparql-string))
        (finally (close source-connection))))))


(defn build-node-ids-files [options]
  (build-node-ids-file options "PW" pathway-nodeIds-files-query)
  (build-node-ids-file options "BP" go-bp-nodeIds-files-query)
  (build-node-ids-file options "CC" go-cc-nodeIds-files-query)
  (build-node-ids-file options "MF" go-cc-nodeIds-files-query)
  )
