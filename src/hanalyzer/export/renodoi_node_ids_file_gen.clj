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
        select ?node (group_concat(distinct ?pw_id ; separator = ',') as ?ice_ids) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_pathway_edge iaohan:linksNode ?node .
       ?shared_pathway_edge rdf:type iaohan:HAN_0000008 . # HAN:shared_pathway_asserted_edge
       ?shared_pathway_edge iaohan:denotes ?pathway .
       ?pathway rdfs:subClassOf ?canonical_pathway .
       ?ice_id obo:IAO_0000219 ?canonical_pathway .
       bind(replace(replace(str(?ice_id),
            'http://kabob.ucdenver.edu/iao/kegg/KEGG_PATHWAY_',''),'_ICE','') as ?pw_id)  
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
        select ?node (group_concat(distinct ?go_id ; separator = ',') as ?ice_ids) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_go_edge iaohan:linksNode ?node .
       ?shared_go_edge rdf:type iaohan:HAN_0000007 . # HAN:shared_go_bp_edge
       ?shared_go_edge iaohan:commonConcept ?go .
       ?ice_id obo:IAO_0000219 ?go .
       bind(replace(replace(str(?ice_id),
            'http://kabob.ucdenver.edu/iao/go/GO_','GO:'),'_ICE','') as ?go_id)       
       }
       group by ?node"))


;;; =======================
;;; Cellular Component query
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
        select ?node (group_concat(distinct ?go_id ; separator = ',') as ?ice_ids) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_go_edge iaohan:linksNode ?node .
       ?shared_go_edge rdf:type iaohan:HAN_0000006 . # HAN:shared_go_cc_edge
       ?shared_go_edge iaohan:commonConcept ?go .
       ?ice_id obo:IAO_0000219 ?go .       
       bind(replace(replace(str(?ice_id),
            'http://kabob.ucdenver.edu/iao/go/GO_','GO:'),'_ICE','') as ?go_id)
       }
       group by ?node"))

;;; =======================
;;; Molecular Function query
;;; =======================

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
        select ?node (group_concat(distinct ?go_id ; separator = ',') as ?ice_ids) {
        VALUES ?node {"
       (slurp (:id_file options))
       "}
       ?shared_go_edge iaohan:linksNode ?node .
       ?shared_go_edge rdf:type iaohan:HAN_0000005 . # HAN:shared_go_mf_edge
       ?shared_go_edge iaohan:commonConcept ?go .
       ?ice_id obo:IAO_0000219 ?go .
       bind(replace(replace(str(?ice_id),
            'http://kabob.ucdenver.edu/iao/go/GO_','GO:'),'_ICE','') as ?go_id)
       } group by ?node"))

(defn- build-node-ids-file [options source-string query-string-fn urlbase urlend]
    (prn (str "Building NodeIds file [" source-string "]..."))
  (let [source-connection (open-kb options)
        sparql-string (query-string-fn options)
        output-file-name (str (:output-directory options)
                              "/commonattributes-plugin-files/network."
                              source-string ".NodeIds.txt")]
    (clojure.java.io/make-parents output-file-name)
    (with-open [w (clojure.java.io/writer output-file-name)]
      (.write w (str source-string " URLBASE:" urlbase " URLEND:" urlend "\n"))
      (.write w (str "Mapping File: network." source-string ".Id2TermMappings.txt\n"))
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (.write w (str (string/upper-case (str ('?/node bindings)))
                                         " = "
                                         ('?/ice_ids bindings)
                                         "\n")))
                          sparql-string))
        (finally (close source-connection))))))


(defn build-node-ids-files [options]
  (build-node-ids-file options "Kegg" pathway-nodeIds-files-query
                       "http://www.genome.ad.jp/dbget-bin/www_bget?pathway+aag" "")
  (build-node-ids-file options "GeneOntology_BP" go-bp-nodeIds-files-query
                       "http://amigo.geneontology.org/amigo/term/" "")
  (build-node-ids-file options "GeneOntology_CC" go-cc-nodeIds-files-query
                       "http://amigo.geneontology.org/amigo/term/" "")
  (build-node-ids-file options "GeneOntology_MF" go-mf-nodeIds-files-query
                       "http://amigo.geneontology.org/amigo/term/" ""))
