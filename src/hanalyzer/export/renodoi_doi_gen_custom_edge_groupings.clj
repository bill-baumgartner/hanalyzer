;; build an doi function that groups edges by the following:
;; PW && (BP || CC || MF)
(ns hanalyzer.export.renodoi-doi-gen-custom-edge-groupings
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

(defn- pw-go-custom-edge-grouping-doi-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?node1 ?node2 {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        ?pw_edge rdf:type iaohan:HAN_0000008 . # HAN:shared_pw_edge
        ?pw_edge iaohan:linksNode ?node1 .
        ?pw_edge iaohan:linksNode ?node2 .

        ?other_edge iaohan:linksNode ?node1 .
        ?other_edge iaohan:linksNode ?node2 .
        ?other_edge rdf:type ?other_edge_type .
        FILTER (?other_edge != ?pw_edge 
                && ?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2))
                && (?other_edge_type = iaohan:HAN_0000007 
                    || ?other_edge_type = iaohan:HAN_0000006
                    || ?other_edge_type = iaohan:HAN_0000005))
        }"))

(defn- pw-custom-edge-grouping-doi-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?node1 ?node2 {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        ?pw_edge rdf:type iaohan:HAN_0000008 . # HAN:shared_pw_edge
        ?pw_edge iaohan:linksNode ?node1 .
        ?pw_edge iaohan:linksNode ?node2 .

        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2)))
        }"))

(defn- go-custom-edge-grouping-doi-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?node1 ?node2 {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        
        ?go_edge iaohan:linksNode ?node1 .
        ?go_edge iaohan:linksNode ?node2 .
        ?go_edge rdf:type ?type .
        
        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2))
                && (?type = iaohan:HAN_0000007 
                    || ?type = iaohan:HAN_0000006
                    || ?type = iaohan:HAN_0000005))
        }"))


(defn- go-bp-custom-edge-grouping-doi-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?node1 ?node2 {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        
        ?go_edge iaohan:linksNode ?node1 .
        ?go_edge iaohan:linksNode ?node2 .
        ?go_edge rdf:type ?type .
        
        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2))
                && ?type = iaohan:HAN_0000007)
        }"))


(defn- go-mf-custom-edge-grouping-doi-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?node1 ?node2 {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        
        ?go_edge iaohan:linksNode ?node1 .
        ?go_edge iaohan:linksNode ?node2 .
        ?go_edge rdf:type ?type .
        
        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2))
                && ?type = iaohan:HAN_0000005)
        }"))

(defn- go-cc-custom-edge-grouping-doi-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?node1 ?node2 {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        
        ?go_edge iaohan:linksNode ?node1 .
        ?go_edge iaohan:linksNode ?node2 .
        ?go_edge rdf:type ?type .
        
        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2))
                && ?type = iaohan:HAN_0000006)
        }"))

(defn- seed-edges-grouping-doi-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?node1 ?node2 {
        VALUES ?node1 {"
        (slurp (:seed-nodes-file options))
        "}
        VALUES ?node2 {"
        (slurp (:seed-nodes-file options))
        "}
        ?edge rdf:type ?edge_type .
        ?edge_type rdfs:subClassOf* iaohan:HAN_0000002 . 
        ?edge iaohan:linksNode ?node1 .
        ?edge iaohan:linksNode ?node2 .
        FILTER (?node1 != ?node2 && STR(IRI(?node1)) < STR(IRI(?node2)))
        }"))



(defn- build-custom-edge-doi-file [options file-label query-fn]
  (prn (str "Building Custom Edge DOI file [" file-label "]..."))
  (let [source-connection (open-kb options)
        sparql-string (query-fn options)
        output-file-name  (str (:output-directory options) "/doi/" file-label ".doi.csv")]
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
                                     ",1.0\n")))
                        sparql-string))
        (finally (close source-connection))))))


(defn build-custom-edge-doi-files [options]
  (build-custom-edge-doi-file options "pw+go" pw-go-custom-edge-grouping-doi-file-query)
  (build-custom-edge-doi-file options "go" pw-custom-edge-grouping-doi-file-query)
  (build-custom-edge-doi-file options "pw" go-custom-edge-grouping-doi-file-query)

  (build-custom-edge-doi-file options "go-bp" go-bp-custom-edge-grouping-doi-file-query)
  (build-custom-edge-doi-file options "go-cc" go-cc-custom-edge-grouping-doi-file-query)
  (build-custom-edge-doi-file options "go-mf" go-mf-custom-edge-grouping-doi-file-query)
  
 ;;designed to be run on the seed network to produce the set of edges
 ;;involving only the seed nodes (no neighbors), hence the file-label
 ;;of 'seeds'
  (build-custom-edge-doi-file options "seeds" seed-edges-grouping-doi-file-query))

