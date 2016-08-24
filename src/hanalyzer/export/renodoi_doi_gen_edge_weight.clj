;; build an doi function that associates edges with their knowledge network score
(ns hanalyzer.export.renodoi-doi-gen-edge-weight
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

(defn- knowledge-score-edge-weight-doi-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?node1 ?node2 ?score {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        ?edge rdf:type iaohan:HAN_0000001 . # HAN:master_edge
        ?edge iaohan:linksNode ?node1 .
        ?edge iaohan:linksNode ?node2 .
        ?edge iaohan:reliability_aael ?score .

        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2)))
                
        }"))

(defn- build-edge-weight-doi-file [options file-label query-fn]
  (prn (str "Building edge weight DOI file [" file-label "]..."))
  (let [source-connection (open-kb options)
        sparql-string (query-fn options)
        output-file-name (str (:output-directory options) "/doi/" file-label ".doi.csv")]
    (clojure.java.io/make-parents output-file-name)
    (with-open [w (clojure.java.io/writer output-file-name)]
      (.write w (str "NodeId, NodeId, score\n"))
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str
                                     (string/upper-case (str ('?/node1 bindings)))
                                     ","
                                     (string/upper-case (str ('?/node2 bindings)))
                                     ","
                                     ('?/score bindings)
                                     "\n")))
                        sparql-string))
        (finally (close source-connection))))))


(defn build-edge-weight-doi-files [options]
  (build-edge-weight-doi-file options "kn-score" knowledge-score-edge-weight-doi-file-query))


