
`{:name "aael-shared-go-bp-edges-hierarchical"
  :description "This rule produces reified edges between two hanalyzer
  nodes that have GGPs that are annotated with the same GO BP
  concept. We limit shared edges between nodes to only those GO
  concepts that have a Resnik concept probability of < 0.01. This
  prevents links being asserted by very general nodes, e.g. the root
  biological_process node."

  :head (;; creates a reified edge of type
         ;; iaohan/HAN_0000007 (HAN:shared_go_bp_asserted_edge)
         ;; that links the two hanalyzer nodes that denote GGPs that
         ;; participate in the same biological process
         (?/edge rdf/type iaohan/HAN_0000007) ;; HAN:shared_go_bp_asserted_edge
         (?/edge iaohan/linksNode ?/node1)
         (?/edge iaohan/linksNode ?/node2)
         (?/edge iaohan/commonConcept ?/go)
         (?/edge iaohan/denotes ?/go_sc)
         (?/edge iaohan/denotes ?/go_sc2)
         (?/edge rdfs/label ?/edgeLabel))

  :reify ([?/edge {:ln (:sha-1 iaohan/HAN_0000007 ?/node1 ?/node2 ?/go)
                   :ns "iaohan" :prefix "HANEDGE_GOBP_"}])

  :body ((?/go iaohan/resnik-concept-prob-aael ?/prob)
         (< ?/prob 0.01)
         (?/go oboInOwl/hasOBONamespace ["biological_process"])
         (?/go rdfs/label ?/edgeLabel)
         (?/go_sc rdfs/subClassOf ?/go)

         ;; get an instance of the go concept that has a has_participant restriction
         (?/go_sc rdfs/subClassOf ?/participant_r)
         (?/participant_r rdf/type owl/Restriction)
         (?/participant_r owl/onProperty obo/RO_0000057)
         (?/participant_r owl/someValuesFrom ?/protein_sc)
         (?/protein_sc rdfs/subClassOf ?/protein)
         (?/protein rdfs/subClassOf ?/taxon_r)
         (?/taxon_r owl/onProperty obo/RO_0002162) ;; in_taxon
         (?/taxon_r rdf/type owl/Restriction)
         (?/taxon_r owl/someValuesFrom  obo/NCBITaxon_7159) ;; yellow fever mosquito
         ;; get the hanalyzer node that denotes the protein
         (?/node1 iaohan/denotes ?/protein)

         ;; get another participant in the process
         ;; The * below is what makes this rule hierarchical.
         ;; Tt looks for participants of any subclass of the go
         ;; concept and creates a shared go term edge based on the
         ;; superclass concept. Since we are looking at subclasses we
         ;; are guaranteed that their concept probability will be <
         ;; than the current concept.
         (?/go_sc2 [rdfs/subClassOf *] ?/go)
         (!= ?/go_sc ?/go_sc2)
         (?/go_sc2 rdfs/subClassOf ?/participant_r2)
         (?/participant_r2 rdf/type owl/Restriction)
         (?/participant_r2 owl/onProperty obo/RO_0000057)
         (?/participant_r2 owl/someValuesFrom ?/protein_sc2)
         (?/protein_sc2 rdfs/subClassOf ?/protein2)
         (!= ?/protein ?/protein2)
         (?/protein2 rdfs/subClassOf ?/taxon_r)
         ;; get the hanalyzer node that denotes the other participant
         (?/node2 iaohan/denotes ?/protein2))

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }



`{:name "aael-shared-go-cc-edges-hierarchical"
  :description "This rule produces reified edges between two hanalyzer
  nodes that have GGPs that are annotated with the same GO CC
  concept. We limit shared edges between nodes to only those GO
  concepts that have a Resnik concept probability of < 0.01. This
  prevents links being asserted by very general nodes, e.g. the root
  cellular_component node."

  :head (;; creates a reified edge of type
         ;; iaohan/HAN_0000006 (HAN:shared_go_cc_asserted_edge)
         ;; that links the two hanalyzer nodes that denote GGPs that
         ;; are known to be colocalized in the same cellular component
         (?/edge rdf/type iaohan/HAN_0000006) ;; HAN:shared_go_cc_asserted_edge
         (?/edge iaohan/linksNode ?/node1)
         (?/edge iaohan/linksNode ?/node2)
         (?/edge iaohan/commonConcept ?/go)
         (?/edge iaohan/denotes ?/go_sc)
         (?/edge iaohan/denotes ?/go_sc2)
         (?/edge rdfs/label ?/edgeLabel))

  :reify ([?/edge {:ln (:sha-1 iaohan/HAN_0000006 ?/node1 ?/node2 ?/go)
                   :ns "iaohan" :prefix "HANEDGE_GOCC_"}])

  :body ((?/go iaohan/resnik-concept-prob-aael ?/prob)
         (< ?/prob 0.01)
         (?/go oboInOwl/hasOBONamespace ["cellular_component"])
         (?/go_sc rdfs/subClassOf ?/go)
         (?/to_r owl/someValuesFrom ?/go_sc)
         (?/to_r owl/onProperty obo/RO_0002339) ;; RO:has_target_end_location
         (?/loc rdfs/subClassOf ?/to_r) 
         (?/loc rdfs/subClassOf obo/GO_0051179) ;; GO:localization
         (?/loc rdfs/subClassOf ?/of_r)
         (?/of_r owl/onProperty obo/RO_0002313) ;; RO:transports_or_maintains_localization_of
         (?/of_r owl/someValuesFrom ?/protein_sc)
         (?/protein_sc rdfs/subClassOf ?/protein)
         (?/protein rdfs/subClassOf ?/taxon_r)
         (?/taxon_r owl/onProperty obo/RO_0002162) ;; in_taxon
         (?/taxon_r rdf/type owl/Restriction)
         (?/taxon_r owl/someValuesFrom obo/NCBITaxon_7159) ;; yellow fever mosquito
         (?/node1 iaohan/denotes ?/protein)
         (?/go rdfs/label ?/edgeLabel)

         (?/go_sc2 [rdfs/subClassOf *] ?/go)
         (!= ?/go_sc2 ?/go_sc)
         (?/to_r2 owl/someValuesFrom ?/go_sc2)
         (?/to_r2 owl/onProperty obo/RO_0002339) ;; RO:has_target_end_location
         (?/loc2 rdfs/subClassOf ?/to_r2)
         (?/loc2 rdfs/subClassOf obo/GO_0051179) ;; GO:localization
         (?/loc2 rdfs/subClassOf ?/of_r2)
         (?/of_r2 owl/onProperty obo/RO_0002313) ;; RO:transports_or_maintains_localization_of
         (?/of_r2 owl/someValuesFrom ?/protein_sc2)
         (?/protein_sc2 rdfs/subClassOf ?/protein2)
         (?/protein2 rdfs/subClassOf ?/taxon_r)
         (!= ?/protein ?/protein2)
         (?/node2 iaohan/denotes ?/protein2))

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }



`{:name "aael-shared-go-mf-edges-hierarchical"
  :description "This rule produces reified edges between two hanalyzer
  nodes that have GGPs that are annotated with the same GO MF
  concept. We limit shared edges between nodes to only those GO
  concepts that have a Resnik concept probability of < 0.01. This
  prevents links being asserted by very general nodes, e.g. the root
  molecular_function node. Note, because molecular functions are not
  currently represented on the BIO side of KaBOB, this rule reaches
  back into the ICE side to grab the requisite information."

  :head (;; creates a reified edge of type
         ;; iaohan/HAN_0000005 (HAN:shared_go_mf_asserted_edge)
         ;; that links the two hanalyzer nodes that denote GGPs that
         ;; participate in the same molecular function
         (?/edge rdf/type iaohan/HAN_0000005) ;; HAN:shared_go_mf_asserted_edge 
         (?/edge iaohan/linksNode ?/node1)
         (?/edge iaohan/linksNode ?/node2)
         (?/edge iaohan/commonConcept ?/go)
         (?/edge iaohan/denotes ?/record) ;; denotes --> ICE is
                                          ;; unconventional, but since
                                          ;; we don't represent GO MF
                                          ;; in BIO world yet this is
                                          ;; the only means of
                                          ;; pointing back to the
                                          ;; source
         (?/edge iaohan/denotes ?/record2)
         (?/edge rdfs/label ?/edgeLabel))

  :reify ([?/edge {:ln (:sha-1 iaohan/HAN_0000005 ?/node1 ?/node2 ?/go)
                   :ns "iaohan" :prefix "HANEDGE_GOMF_"}])

  :body ((?/go iaohan/resnik-concept-prob-aael ?/prob)
         (< ?/prob 0.005)
         (?/go oboInOwl/hasOBONamespace ["molecular_function"])

         (?/go_ice obo/IAO_0000219 ?/go) ;; IAO:denotes
         (?/go_id_field obo/IAO_0000219 ?/go_ice) ;; IAO:denotes
         (?/go_id_field kiao/hasTemplate iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
         (?/record obo/BFO_0000051 ?/go_id_field) ;; BFO:has_part 
         (?/record obo/BFO_0000051 ?/protein_id_field) ;; BFO:has_part
         
         ;; filter out the negations
         (:optional
           ((?/record obo/BFO_0000051 ?/qualfv) ; has_part
            (?/qualfv kiao/hasTemplate
                      iaogoa/GpAssociationGoaUniprotFileData_qualifierDataField1)
            (?/qualfv obo/IAO_0000219 ?/qualifier)))
         (:or (:not (:bound ?/qualifier))
               (:not (:regex ?/qualifier "^NOT" "i")))
         
         (?/protein_id_field kiao/hasTemplate
                             iaogoa/GpAssociationGoaUniprotFileData_databaseObjectIDDataField1)
         (?/protein_id_field obo/IAO_0000219 ?/protein_ice) ;; IAO:denotes
         (?/protein_ice  obo/IAO_0000219 ?/protein) ;; IAO:denotes
         (?/protein rdfs/subClassOf ?/taxon_r)
         (?/taxon_r owl/onProperty obo/RO_0002162) ;; in_taxon
         (?/taxon_r rdf/type owl/Restriction)
         (?/taxon_r owl/someValuesFrom obo/NCBITaxon_7159) ;; yellow fever mosquito
         (?/node1 iaohan/denotes ?/protein)
         (?/go rdfs/label ?/edgeLabel)

         ;; Use of the * below and the option of record2 having a
         ;; different go_id_field makes this rule hierarchical (in
         ;; comparison to its 'exact' version)
         (?/go_sc [rdfs/subClassOf *] ?/go)
         (?/go_ice2 obo/IAO_0000219 ?/go_sc) ;; IAO:denotes
         (?/go_id_field2 obo/IAO_0000219 ?/go_ice2) ;; IAO:denotes
         (?/go_id_field2 kiao/hasTemplate iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
         (?/record2 obo/BFO_0000051 ?/go_id_field2) ;; BFO:has_part 
         (!= ?/record ?/record2)
         (?/record2 obo/BFO_0000051 ?/protein_id_field2) ;; BFO:has_part
         
         ;; filter out the negations
         (:optional
          ((?/record2 obo/BFO_0000051 ?/qualfv2) ; has_part
           (?/qualfv2 kiao/hasTemplate
                      iaogoa/GpAssociationGoaUniprotFileData_qualifierDataField1)
            (?/qualfv2 obo/IAO_0000219 ?/qualifier2)))
         (:or (:not (:bound ?/qualifier2))
              (:not (:regex ?/qualifier2 "^NOT" "i")))
         
         (?/protein_id_field2 kiao/hasTemplate
                              iaogoa/GpAssociationGoaUniprotFileData_databaseObjectIDDataField1)
         (?/protein_id_field2 obo/IAO_0000219 ?/protein_ice2) ;; IAO:denotes
         (?/protein_ice2  obo/IAO_0000219 ?/protein2) ;; IAO:denotes
         (?/protein2 rdfs/subClassOf ?/taxon_r)
         (?/protein2 rdfs/subClassOf ?/taxon_r)
         (!= ?/protein ?/protein2)
         (?/node2 iaohan/denotes ?/protein2))

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
