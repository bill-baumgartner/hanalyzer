;; Given a list of gene/gene-product identifier URIs, this script
;; generates the collection of files required by the RenoDOI Cytoscape
;; plugin.
(ns hanalyzer.export.renodoi
  (:use [edu.ucdenver.ccp.kr.sesame.kb]
        [edu.ucdenver.ccp.kr.sesame.sparql]
        [edu.ucdenver.ccp.kr.sesame.rdf]
        [edu.ucdenver.ccp.kr.kb]
        [edu.ucdenver.ccp.kr.rdf]
        [edu.ucdenver.ccp.kr.sparql])
  (:require [clojure.tools.cli
             :refer [parse-opts]]
            [clojure.string :as string]
            [edu.ucdenver.ccp.kr.kb
             :refer [*kb*]]
            [edu.ucdenver.ccp.kr.rdf
             :refer [sym-to-long-name
                     *ns-map-to-long*]]
            [edu.ucdenver.ccp.kabob.build.input-kb
             :refer [open-kb]]
            [hanalyzer.export.renodoi-node-ids-file-gen
             :refer [build-node-ids-files]]
            [hanalyzer.export.renodoi-id2termmappings-file-gen
             :refer [build-id2termmappings-files]]
            [hanalyzer.export.renodoi-noa-file-gen
             :refer [build-noa-files]]
            [hanalyzer.export.renodoi-doi-gen-nodes-v-neighbors
             :refer [build-nodes-v-neighbors-doi-file]]
            [hanalyzer.export.renodoi-doi-gen-ppi-edges
             :refer [build-ppi-edge-doi-files]]
            [hanalyzer.export.renodoi-edge-evidence-file-gen
             :refer [build-edge-evidence-file]]
            [hanalyzer.export.renodoi-doi-gen-custom-edge-groupings
             :refer [build-custom-edge-doi-files]]
            [hanalyzer.export.renodoi-doi-gen-only-ppi-edge
             :refer [build-only-ppi-support-doi-file]]
            [hanalyzer.export.renodoi-doi-gen-edge-weight
             :refer [build-edge-weight-doi-files]])
  (:gen-class))


(def cli-options
  [["-s" "--server-url SERVER_URL" "Triple store server URL, e.g. http://url/to/server:port"]
   ["-r" "--repo-name REPOSITORY_NAME" "Name of the repository to open."]
   ["-u" "--username USERNAME" "User credential to access repository."]
   ["-p" "--password PASSWORD" "Password credential to access repository."]
   ["-i" "--id_file ID_FILE" "File containing identifier URIs to represent as nodes in the network"]
   ["-o" "--output-directory OUTPUT_DIRECTORY" "Path to folder where generated files will be stored."]
   ["-n" "--seed-nodes-file SEED_NODES" "File containing seed nodes for the network."]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["This program queries a KaBOB instance with Hanalyzer layer to
  construct the files required by the RenoDOI Cytoscape plugin."
        ""
        "Usage: program-name [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  nodes      Extract the hanalyzer node URIs for the input set of identifiers"
        "  neighbors  Expand the list of input identifiers by including neighbors of their respective hanalyzer nodes"
        "  sif        Create a .sif file containing the Hanalyzer layer network"
        "  id2sym     Create a .csv file containing a mapping from node IDs in the .sif file to a human-readable label."
        "  ee         Create the edge-experts file that details which sources contributed to each hanalyzer edge."
        "  node.id.files "
        "  id2term.mapping.files "
        "  noa "
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

;;; ==============
;;; BUILD SIF FILE
;;; ==============

(defn sif-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node1 ?node2 {
        VALUES ?node1 {"
       (slurp (:id_file options))
       "}
        VALUES ?node2 {"
       (slurp (:id_file options))
       "}
       ?master_edge iaohan:linksNode ?node1 .
       ?master_edge rdf:type iaohan:HAN_0000001 . # HAN:master_edge
       ?master_edge iaohan:linksNode ?node2 .
       FILTER (?node1 != ?node2 && STR(IRI(?node1)) < STR(IRI(?node2)))
       }"))


(defn build-sif-file [options]
  (prn (str "Building sif file..."))
  (let [source-connection (open-kb options)
        sparql-string (sif-file-query options)]
    (with-open [w (clojure.java.io/writer
                   (str (:output-directory options) "/network.sif"))]
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str (string/upper-case (str ('?/node1 bindings))) "\tKnowledge\t" (string/upper-case (str ('?/node2 bindings))) "\n")))
                        sparql-string))
        (finally (close source-connection))))))

;;; ===============
;;; BUILD NODE FILE
;;; ===============

(defn node-query [options]
  "Queries for the hanalyzer node for the input identifiers"
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?node {
           VALUES ?id_ice {"
              (slurp (:id_file options))
           "}
            ?id_ice obo:IAO_0000219 ?bioentity .
            ?node iaohan:denotes ?bioentity .
          }"))


(defn build-nodes-file [options]
    (prn (str "Building node uris file..."))
  (let [source-connection (open-kb options)
        sparql-string (node-query options)]
    (with-open [w (clojure.java.io/writer
                   (str (:output-directory options) "/nodes.uri"))]
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str "<" (sym-to-long-name ('?/node bindings)) ">\n")))
                          sparql-string))
          (finally (close source-connection))))))


;;; =========================
;;; BUILD NODE NEIGHBORS FILE
;;; =========================

(defn node-neighbors-query [options]
  "Queries for the immediate neighbors of the input nodes in the hanalyzer layer. Nodes are not considered neighbors if the only source asserting an interaction is String."
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select distinct ?neighbor_node {
           VALUES ?id_ice {"
              (slurp (:id_file options))
           "}
            ?id_ice obo:IAO_0000219 ?bioentity .
            ?node iaohan:denotes ?bioentity .
            ?master_edge iaohan:linksNode ?node .
            ?master_edge rdf:type iaohan:HAN_0000001 . # HAN:master_edge
            ?master_edge iaohan:asserted_by ?asserting_sources .

            ?master_edge iaohan:linksNode ?neighbor_node .
           
            # exclude edges that are asserted only by String or by the low-confidence Guo data
            FILTER (?node != ?neighbor_node 
                    && ?asserting_sources != 'http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000012'@en   # LC_PPI_String
                    && ?asserting_sources != 'http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000014'@en   # HC_PPI_String
                    && ?asserting_sources != 'http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000013'@en)  # LC_PPI_Guo
          }"))


(defn build-nodes-plus-neighbors-file [options]
    (prn (str "Building nodes+neighbors file..."))
  (let [source-connection (open-kb options)
        node-sparql-string (node-query options)
        neighbor-sparql-string (node-neighbors-query options)]
    (with-open [w (clojure.java.io/writer
                   (str (:output-directory options) "/nodes+neighbors.uri"))]
      (try
        ;; get the seed nodes and write to output file
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str "<" (sym-to-long-name ('?/node bindings)) ">\n")))
                        node-sparql-string)
          ;; then get the neighbors of the seed nodes and write to the output file
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str "<" (sym-to-long-name ('?/neighbor_node bindings)) ">\n")))
                        neighbor-sparql-string))
        (finally (close source-connection))))))

;;; ===========================
;;; BUILD NODE ID TO LABEL FILE
;;; ===========================

(defn id2sym-file-query [options]
 (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  
        select ?node (group_concat(?node_label ; separator = ';') as ?labels) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
        ?node iaohan:denotes ?bioentity .
        ?bioentity rdfs:label ?node_label .       
       }
       group by ?node"))

(defn build-node-id-to-symbol-file [options]
    (prn (str "Building node-id-to-symbol file..."))
  (let [source-connection (open-kb options)
        sparql-string (id2sym-file-query options)]
    (with-open [w (clojure.java.io/writer
                   (str (:output-directory options) "/network.geneID2Symbol.csv"))]
      (.write w (str "NodeID,Label\n"))
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str (string/upper-case (str ('?/node bindings))) "," ('?/labels bindings) "\n")))
                          sparql-string))
          (finally (close source-connection))))))


;;; =======================
;;; BUILD EDGE EXPERTS FILE
;;; =======================

(defn ee-file-query [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node1 ?node2 ?source_edge_types {
        VALUES ?node1 {"
       (slurp (:id_file options))
       "}
        VALUES ?node2 {"
       (slurp (:id_file options))
       "}
       ?master_edge iaohan:linksNode ?node1 .
       ?master_edge rdf:type iaohan:HAN_0000001 . # HAN:master_edge
       ?master_edge iaohan:linksNode ?node2 .
          ?master_edge iaohan:asserted_by ?source_edge_types .
       FILTER (?node1 != ?node2 && STR(IRI(?node1)) < STR(IRI(?node2)))
       }"))

(def edge-type-label-map
  {"http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000005" "GeneOntology_MF"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000006" "GeneOntology_CC"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000007" "GeneOntology_BP"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000008" "Kegg"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000009" "PPI"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000010" "HC_PPI"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000011" "LC_PPI"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000012" "LC_PPI_String"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000013" "LC_PPI_Guo"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000014" "HC_PPI_String"
   "http://kabob.ucdenver.edu/iao/hanalyzer/HAN_0000015" "HC_PPI_Guo"
   })
   

(defn ee-string [s]
  (apply str (drop-last ;; drop trailing "///"
              (interleave (map edge-type-label-map
                               (clojure.string/split s #","))
                          (repeat "///")))))

(defn build-edge-experts-file [options]
  (prn (str "Building edge-experts file..."))
  (let [source-connection (open-kb options)
        sparql-string (ee-file-query options)]
    (with-open [w (clojure.java.io/writer
                   (str (:output-directory options)
                        "/commonattributes-plugin-files/network.edgeExperts.eda"))]
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str (string/upper-case (str ('?/node1 bindings)))
                                         " (Knowledge) "
                                         (string/upper-case (str ('?/node2 bindings)))
                                         " = "
                                         (ee-string ('?/source_edge_types bindings))
                                         "\n")))
                          sparql-string))
          (finally (close source-connection))))))






(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 1) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    ;; Execute program with options
    (case (first arguments)
      "nodes" (build-nodes-file options)
      "neigh" (build-nodes-plus-neighbors-file options)
      "sif" (build-sif-file options)
      "id2sym" (build-node-id-to-symbol-file options)
      "ee" (build-edge-experts-file options)
      "node.id.files" (build-node-ids-files options)
      "id2term.mapping.files" (build-id2termmappings-files options)
      "ev" (build-edge-evidence-file options)
      "noa" (build-noa-files options)
      "doi.nodes" (build-nodes-v-neighbors-doi-file options)
      "doi.edges" (do (build-only-ppi-support-doi-file options)
                      (build-custom-edge-doi-files options)
                      (build-ppi-edge-doi-files options)
                      (build-edge-weight-doi-files options))
      "all" (time (do (time (build-sif-file options))
                      (time (build-node-id-to-symbol-file options))
                      (time (build-edge-experts-file options))
                      (time (build-node-ids-files options))
                      (time (build-id2termmappings-files options))
                      (time (build-noa-files options))
                      (time (build-edge-evidence-file options))
                      (time (build-nodes-v-neighbors-doi-file options))
                      (time (build-only-ppi-support-doi-fil options))
                      (time (build-custom-edge-doi-files options))
                      (time (build-ppi-edge-doi-files options))
                      (time (build-edge-weight-doi-files options))))
      (exit 1 (usage summary)))))

