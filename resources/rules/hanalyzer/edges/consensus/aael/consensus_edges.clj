
`{:name "consensus-edges-aael"
  :description "This rule defines the edges that will form the consensus set from which the different edge reliability scores will be computed. For the AAEL network, we use the HAN_0000010 (HAN:high_confidence_ppi_asserted_edge) as the consensus set."

  :dependency "rules/hanalyzer/edges/ppi/aael/step_b/aael_ppi_edges_by_score"
  
  :head ((?/edge rdf/type iaohan/HAN_0000016)) ;; HAN:consensus_set_edge

  :reify ()

  :body ((?/edge rdf/type iaohan/HAN_0000010) ;; HAN:high_confidence_ppi_asserted_edge
         (?/edge iaohan/interaction_source ["Guo" "en"])) 

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }



