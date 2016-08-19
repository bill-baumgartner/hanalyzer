`{:name "aael-shared-pathway-edges"
  :description "This rule produces reified edges between two hanalyzer nodes
  that have GGPs that appear in the same KEGG pathway."
  :head (;; creates a reified edge of type iaohan/HAN_0000008 (HAN:shared_pathway_asserted_edge) that links
         ;; the two hanalyzer nodes that denote GGPs that participate in a shared pathway
         (?/edge rdf/type iaohan/HAN_0000008) ;; HAN:shared_pathway_asserted_edge 
         (?/edge iaohan/linksNode ?/node1)
         (?/edge iaohan/linksNode ?/node2)
         (?/edge iaohan/denotes ?/pathway)
         (?/edge rdfs/label ?/edgeLabel))

  :reify ([?/edge {:ln (:sha-1 iaohan/HAN_0000008 ?/node1 ?/node2 ?/pathway)
                   :ns "iaohan" :prefix "HANEDGE_PW_"}])

  :body (;; this rule is yellow fever mosquito-specific so we restrict to
         ;; NCBITaxon 7159
         (?/taxon_r owl/onProperty obo/RO_0002162)
         (?/taxon_r rdf/type owl/Restriction)
         (?/taxon_r owl/someValuesFrom obo/NCBITaxon_7159)
         (?/pathway rdfs/subClassOf ?/taxon_r) ;; the pathway must belong to the specified taxon
         ;;(?/pathway [rdfs/subClassOf *] obo/INO_0000003) ;; INO_0000003 = pathway
         (?/pathway rdfs/subClassOf ?/canonical_pathway)
         (?/ice obo/IAO_0000219 ?/canonical_pathway)

         ;; Exclude the KEGG Global/Overview maps as they are too general
         (:optional
          ((?/ice rdf/type ?/ice_category)))
         (:or (:not (:bound ?/ice_category))
              (:not (== ?/qualifier iaokegg/GlobalOverviewMap)))
         
         (?/pathway rdfs/label ?/edgeLabel)

         ;; the pathway must have two different participants that
         ;; are each denoted by a Hanalyzer node
         (?/pathway rdfs/subClassOf ?/participant_r)
         (?/participant_r rdf/type owl/Restriction)
         (?/participant_r owl/onProperty obo/RO_0000057) ;; RO_0000057 = has_participant
         (?/participant_r owl/someValuesFrom ?/ggp_sc_1)
         (?/pathway rdfs/subClassOf ?/participant_r_2)
         (?/participant_r_2 rdf/type owl/Restriction)
         (?/participant_r_2 owl/onProperty obo/RO_0000057) ;; RO_0000057 = has_participant
         (?/participant_r_2 owl/someValuesFrom ?/ggp_sc_2)
         (!= ?/ggp_sc_1 ?/ggp_sc_2)

         (?/ggp_sc_1 rdfs/subClassOf ?/ggp_1)
         (?/node1 iaohan/denotes ?/ggp_1)

         (?/ggp_sc_2 rdfs/subClassOf ?/ggp_2)
         (?/node2 iaohan/denotes ?/ggp_2))

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
