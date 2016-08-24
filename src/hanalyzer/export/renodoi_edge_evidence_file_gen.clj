(ns hanalyzer.export.renodoi-edge-evidence-file-gen
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


(defn- edge-evidence-file-query-bp [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX iaogoa: <http://kabob.ucdenver.edu/iao/goa/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node1 ?node2 ?db_ref {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        ?edge rdf:type iaohan:HAN_0000007 .
        ?edge iaohan:linksNode ?node1 .
        ?edge iaohan:linksNode ?node2 .
        ?edge iaohan:denotes ?go_sc .
        ?go_sc rdfs:subClassOf ?participant_r .
        ?participant_r owl:onProperty obo:RO_0000057 .
        ?participant_r owl:someValuesFrom ?protein_sc .
        ?protein_sc rdfs:subClassOf ?protein .
        ?protein_ice obo:IAO_0000219 ?protein .
        ?db_id_field obo:IAO_0000219 ?protein_ice .
        ?db_id_field kiao:hasTemplate iaogoa:GpAssociationGoaUniprotFileData_databaseObjectIDDataField1 .
        ?record obo:BFO_0000051 ?db_id_field .
        ?record obo:BFO_0000051 ?go_id_field .
        ?go_id_field kiao:hasTemplate iaogoa:GpAssociationGoaUniprotFileData_goIDDataField1 .
        ?go_id_field obo:IAO_0000219 ?go_ice .
        ?go_ice obo:IAO_0000219 ?go .
        ?go_sc rdfs:subClassOf ?go .
        ?record obo:BFO_0000051 ?db_ref_field .
        ?db_ref_field kiao:hasTemplate iaogoa:GpAssociationGoaUniprotFileData_dbReferenceDataField1 .
        ?db_ref_field obo:IAO_0000219 ?db_ref .
      

        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2)))
              
        }"))

(defn- edge-evidence-file-query-cc [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX iaogoa: <http://kabob.ucdenver.edu/iao/goa/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node1 ?node2 ?db_ref {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        ?edge rdf:type iaohan:HAN_0000006 .
        ?edge iaohan:linksNode ?node1 .
        ?edge iaohan:linksNode ?node2 .
        ?edge iaohan:denotes ?go_sc .
        ?to_r owl:someValuesFrom ?go_sc .
        ?to_r owl:onProperty obo:RO_0002339 .
        ?loc rdfs:subClassOf ?to_r .
        ?loc rdfs:subClassOf ?of_r .
        ?of_r owl:onProperty obo:RO_0002313 .
        ?of_r owl:someValuesFrom ?protein_sc .

        ?protein_sc rdfs:subClassOf ?protein .
        ?protein_ice obo:IAO_0000219 ?protein .
        ?db_id_field obo:IAO_0000219 ?protein_ice .
        ?db_id_field kiao:hasTemplate iaogoa:GpAssociationGoaUniprotFileData_databaseObjectIDDataField1 .
        ?record obo:BFO_0000051 ?db_id_field .
        ?record obo:BFO_0000051 ?go_id_field .
        ?go_id_field kiao:hasTemplate iaogoa:GpAssociationGoaUniprotFileData_goIDDataField1 .
        ?go_id_field obo:IAO_0000219 ?go_ice .
        ?go_ice obo:IAO_0000219 ?go .
        ?go_sc rdfs:subClassOf ?go .
        ?record obo:BFO_0000051 ?db_ref_field .
        ?db_ref_field kiao:hasTemplate iaogoa:GpAssociationGoaUniprotFileData_dbReferenceDataField1 .
        ?db_ref_field obo:IAO_0000219 ?db_ref .
      
        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2)))
              
        }"))


(defn- edge-evidence-file-query-mf [options]
  (str "PREFIX franzOption_memoryLimit: <franz:85g> 
        PREFIX franzOption_memoryExhaustionWarningPercentage: <franz:95> 
        PREFIX franzOption_clauseReorderer: <franz:identity> 
        PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
        PREFIX obo: <http://purl.obolibrary.org/obo/> 
        PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
        PREFIX iaogoa: <http://kabob.ucdenver.edu/iao/goa/> 
        PREFIX owl: <http://www.w3.org/2002/07/owl#> 
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        select ?node1 ?node2 ?db_ref {
        VALUES ?node1 {"
        (slurp (:id_file options))
        "}
        VALUES ?node2 {"
        (slurp (:id_file options))
        "}
        ?edge rdf:type iaohan:HAN_0000005 .
        ?edge iaohan:linksNode ?node1 .
        ?edge iaohan:linksNode ?node2 .
        ?edge iaohan:denotes ?record .
       
        ?record obo:BFO_0000051 ?db_ref_field .
        ?db_ref_field kiao:hasTemplate iaogoa:GpAssociationGoaUniprotFileData_dbReferenceDataField1 .
        ?db_ref_field obo:IAO_0000219 ?db_ref .
      
        FILTER (?node1 != ?node2 
                && STR(IRI(?node1)) < STR(IRI(?node2)))
              
        }"))


;; kiao:pm/PM_24718992_ICE
(defn- extract-pmid [s]
   (:pmid (zipmap [:pmid] (rest (re-find #"iaopm/PM_(\d+)_ICE" (str s))))))


(defn- build-edge-evidence-file-part [options source-str query-str-fn append-option]
  (prn (str "Building edge-evidence file [" source-str "]..."))
  (let [source-connection (open-kb options)
        sparql-string (query-str-fn options)]
    (prn (str "SPARQL: " sparql-string))
    (with-open [w (clojure.java.io/writer
                   (str (:output-directory options)
                        "/commonattributes-plugin-files/edgeEvidence.txt")
                   :append append-option)]
      (try
        (binding [*kb* source-connection
                  edu.ucdenver.ccp.kr.rdf/*use-inference* false]
          (visit-sparql source-connection
                        (fn [bindings]
                          (let [pmid (extract-pmid ('?/dbref bindings))]
                            (when pmid 
                              (.write w (str
                                         (string/upper-case (str ('?/node1 bindings)))
                                         "(" source-str ")"
                                         (string/upper-case (str ('?/node2 bindings)))
                                         " = PMID:" pmid "\n")))))
                          sparql-string))
          (finally (close source-connection))))))


(defn build-edge-evidence-file [options]
  (build-edge-evidence-file-part options "GeneOntology_BP" edge-evidence-file-query-bp false)
  (build-edge-evidence-file-part options "GeneOntology_CC" edge-evidence-file-query-cc true)
  (build-edge-evidence-file-part options "GeneOntology_MF" edge-evidence-file-query-mf true))
 
