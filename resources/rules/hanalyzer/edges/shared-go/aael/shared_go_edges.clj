
`{:name "aael-shared-go-bp-edges"
  :description "This rule produces reified edges between two hanalyzer nodes
  that have GGPs that are annotated with the same GO BP concept. We limit 
shared edges between nodes to only those GO concepts that have a Resnik concept probability of < 0.01. This prevents links being asserted by very general nodes, e.g. the root biological_process node."

  :head (;; creates a reified edge of type iaohan/SharedPathwayEdge that links
         ;; the two hanalyzer nodes that denote GGPs that participate in a shared pathway
         (?/edge rdf/type iaohan/SharedGoBpEdge) 
         (?/edge iaohan/linksNode ?/node1)
         (?/edge iaohan/linksNode ?/node2)
         (?/edge iaohan/denotes ?/go_sc)
         (?/edge iaohan/denotes ?/go_sc2)
         (?/edge rdfs/label ?/edgeLabel))

  :reify ([?/edge {:ln (:sha-1 iaohan/SharedGoBpEdge ?/node1 ?/node2 ?/go)
                   :ns "iaohan" :prefix "HANEDGE_GOBP_"}])

  :body ((?/go oboInOwl/hasOBONamespace ["biological_process"])
         (?/go iaohan/resnik-concept-prob-aael ?prob)
         (< ?prob 0.01)
         (?/go rdfs/label ?/edgeLabel)
         (?/go_sc rdfs/subClassOf ?/go)
         (?/go_sc rdfs/subClassOf ?/participant_r)
         (?/participant_r rdf/type owl/Restriction)
         (?/participant_r owl/onProperty obo/RO_0000057)
         (?/participant_r owl/someValuesFrom ?/protein_sc)
         (?/protein_sc rdfs/subClassOf ?/protein)
         (?/protein rdfs/subClassOf ?/taxon_r)
         (?/taxon_r owl/onProperty obo/RO_0002162) ;; in_taxon
         (?/taxon_r rdf/type owl/Restriction)
         (?/taxon_r owl/someValuesFrom ?/taxon_sc)
         (?/taxon_sc [rdfs/subClassOf *] obo/NCBITaxon_7159) ;; yellow fever mosquito
         (?/node1 iaohan/denotes ?/protein)
         (?/go_sc2 rdfs/subClassOf ?/go)
         (!= ?/go_sc ?/go_sc2)
         (?/go_sc2 rdfs/subClassOf ?/participant_r2)
         (?/participant_r2 rdf/type owl/Restriction)
         (?/participant_r2 owl/onProperty obo/RO_0000057)
         (?/participant_r2 owl/someValuesFrom ?/protein_sc2)
         (?/protein_sc2 rdfs/subClassOf ?/protein2)
         (!= ?/protein ?/protein2)
         (?/protein2 rdfs/subClassOf ?/taxon_r)
        
         (?/node2 iaohan/denotes ?/protein2))

  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }



`{:name "aael-shared-go-cc-edges"
  :description "This rule produces reified edges between two hanalyzer nodes
  that have GGPs that are annotated with the same GO CC concept. We limit 
shared edges between nodes to only those GO concepts that have a Resnik concept probability of < 0.01. This prevents links being asserted by very general nodes, e.g. the root cellular_component node."

  :head (;; creates a reified edge of type iaohan/SharedPathwayEdge that links
         ;; the two hanalyzer nodes that denote GGPs that participate in a shared pathway
         (?/edge rdf/type iaohan/SharedGoCcEdge) 
         (?/edge iaohan/linksNode ?/node1)
         (?/edge iaohan/linksNode ?/node2)
         (?/edge iaohan/denotes ?/go_sc)
         (?/edge iaohan/denotes ?/go_sc2)
         (?/edge rdfs/label ?/edgeLabel))

  :reify ([?/edge {:ln (:sha-1 iaohan/SharedGoCcEdge ?/node1 ?/node2 ?/go)
                   :ns "iaohan" :prefix "HANEDGE_GOCC_"}])

  :body ((?/loc rdfs/subClassOf obo/GO_0051179) ;; GO:localization
         (?/loc rdfs/subClassOf ?/of_r)
         (?/of_r owl/onProperty obo/RO_0002313) ;; RO:transports_or_maintains_localization_of
         (?/of_r owl/someValuesFrom ?/protein_sc)
         (?/protein_sc rdfs/subClassOf ?/protein)
         (?/protein rdfs/subClassOf ?/taxon_r)
         (?/taxon_r owl/onProperty obo/RO_0002162) ;; in_taxon
         (?/taxon_r rdf/type owl/Restriction)
         (?/taxon_r owl/someValuesFrom ?/taxon_sc)
         (?/taxon_sc [rdfs/subClassOf *] obo/NCBITaxon_7159) ;; yellow fever mosquito
         (?/node1 iaohan/denotes ?/protein)
         (?/loc rdfs/subClassOf ?/to_r)
         (?/to_r owl/onProperty obo/RO_0002339) ;; RO:has_target_end_location
         (?/to_r owl/someValuesFrom ?/go_sc)
         (?/go_sc rdfs/subClassOf ?/go)
         (?/go rdfs/label ?/edgeLabel)
         (?/go iaohan/resnik-concept-prob-aael ?prob)
         (< ?prob 0.01)

         (?/loc2 rdfs/subClassOf obo/GO_0051179) ;; GO:localization
         (!= ?/loc ?/loc2)
         (?/loc2 rdfs/subClassOf ?/of_r2)
         (?/of_r2 owl/onProperty obo/RO_0002313) ;; RO:transports_or_maintains_localization_of
         (?/of_r2 owl/someValuesFrom ?/protein_sc2)
         (?/protein_sc2 rdfs/subClassOf ?/protein2)
         (?/protein2 rdfs/subClassOf ?/taxon_r)
         (!= ?/protein ?/protein2)
         (?/node2 iaohan/denotes ?/protein2)
         (?/loc2 rdfs/subClassOf ?/to_r2)
         (?/to_r2 owl/onProperty obo/RO_0002339) ;; RO:has_target_end_location
         (?/to_r2 owl/someValuesFrom ?/go_sc2)
         (?/go_sc2 rdfs/subClassOf ?/go)
         )

  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
