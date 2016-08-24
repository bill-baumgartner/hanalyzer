(ns hanalyzer.export.renodoi-doi-gen-ppi-edges
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

(defn- ppi-doi-files-query [options ppi-type1-str ppi-type2-str]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node1 ?node2 ?score {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        ?edge rdf:type ?edge_type .
        ?edge iaohan:linksNode ?node1 .
        ?edge iaohan:linksNode ?node2 .
        ?edge iaohan:interaction_score ?score .       
        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2))
                && (?edge_type = iaohan:" ppi-type1-str " 
                    || ?edge_type = iaohan:" ppi-type2-str "))
        }"))

(def ppi-type-to-label-map
  {"HAN_0000012" "String"
   "HAN_0000013" "Guo"
   "HAN_0000014" "String"
   "HAN_0000015" "Guo"})

(defn- build-ppi-edge-doi-file [options ppi-type1-str ppi-type2-str norm-fn]
  (prn (str "Building PPI edge DOI file [" (ppi-type-to-label-map ppi-type1-str) "]..."))
  (let [source-connection (open-kb options)
        sparql-string (ppi-doi-files-query options ppi-type1-str ppi-type2-str)
        output-file-name (str (:output-directory options)
                              "/doi/" (ppi-type-to-label-map ppi-type1-str) ".doi.csv")]
    (clojure.java.io/make-parents output-file-name)
    (with-open [w (clojure.java.io/writer output-file-name)]
      (.write w (str "NodeId, NodeId, " (ppi-type-to-label-map ppi-type1-str) "_score\n"))
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
                                     (norm-fn ('?/score bindings))
                                     "\n")))
                          sparql-string))
        (finally (close source-connection))))))


(defn build-ppi-edge-doi-files [options]
  ;; The raw String scores range from 150-999, so we normalize to 0-1
  ;; b/c that is required by renodoi. The Guo values are already
  ;; between 0-1.
  (build-ppi-edge-doi-file options "HAN_0000012" "HAN_0000014" #(/ (float %) 999.0)) ;;String
  (build-ppi-edge-doi-file options "HAN_0000013" "HAN_0000015" identity)) ;;Guo
