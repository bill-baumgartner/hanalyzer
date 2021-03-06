
`{:name "aael-ppi-edges-high-confidence-guo"
  :description "This rule creates a simple heirarchy of edges based on
  the edge scores provided in their ICE records. Edges are divided
  into two groups: 1) a high-scoring group, i.e. interactions with
  higher confidence levels; and 2) a low-scoring group,
  i.e. interactions with lower confidence levels. For AAEL we have
  interactions from two sources: 1) the Guo paper, and 2) a String
  network. Each source uses a different range of scores, so there are
  rules to handle each source."

  :dependency "rules/hanalyzer/edges/ppi/aael/step_a/aael_ppi_edges"
  
  :head ((?/edge rdf/type iaohan/HAN_0000010) ;; HAN:high_confidence_ppi_asserted_edge
         (?/edge rdf/type iaohan/HAN_0000015)) ;; HAN:high_confidence_ppi_asserted_edge_guo
        
  :reify ()

  :body ((?/edge rdf/type iaohan/HAN_0000009) ;; HAN:ppi_asserted_edge
         (?/edge iaohan/interaction_source ["Guo" "en"])
         (?/edge iaohan/interaction_score ?/score)
         (> ?/score 0.60)) 

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }


`{:name "aael-ppi-edges-low-confidence-guo"
  :description "This rule creates a simple heirarchy of edges based on
  the edge scores provided in their ICE records. Edges are divided
  into two groups: 1) a high-scoring group, i.e. interactions with
  higher confidence levels; and 2) a low-scoring group,
  i.e. interactions with lower confidence levels. For AAEL we have
  interactions from two sources: 1) the Guo paper, and 2) a String
  network. Each source uses a different range of scores, so there are
  rules to handle each source."

  :dependency "rules/hanalyzer/edges/ppi/aael/step_a/aael_ppi_edges"
  
  :head ((?/edge rdf/type iaohan/HAN_0000011) ;; HAN:low_confidence_ppi_asserted_edge
         (?/edge rdf/type iaohan/HAN_0000013)) ;; HAN:low_confidence_ppi_asserted_edge_guo

  :reify ()

  :body ((?/edge rdf/type iaohan/HAN_0000009) ;; HAN:ppi_asserted_edge
         (?/edge iaohan/interaction_source ["Guo" "en"])
         (?/edge iaohan/interaction_score ?/score)
         (<= ?/score 0.60)) 

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }


`{:name "aael-ppi-edges-high-confidence-string"
  :description "This rule creates a simple heirarchy of edges based on
  the edge scores provided in their ICE records. Edges are divided
  into two groups: 1) a high-scoring group, i.e. interactions with
  higher confidence levels; and 2) a low-scoring group,
  i.e. interactions with lower confidence levels. For AAEL we have
  interactions from two sources: 1) the Guo paper, and 2) a String
  network. Each source uses a different range of scores, so there are
  rules to handle each source."

  :dependency "rules/hanalyzer/edges/ppi/aael/step_a/aael_ppi_edges"
  
  :head ((?/edge rdf/type iaohan/HAN_0000010) ;; HAN:high_confidence_ppi_asserted_edge
         (?/edge rdf/type iaohan/HAN_0000014)) ;; HAN:high_confidence_ppi_asserted_edge_string

  :reify ()

  :body ((?/edge rdf/type iaohan/HAN_0000009) ;; HAN:ppi_asserted_edge
         (?/edge iaohan/interaction_source ["String" "en"])
         (?/edge iaohan/interaction_score ?/score)
         (> ?/score 600)) 

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }


`{:name "aael-ppi-edges-low-confidence-string"
  :description "This rule creates a simple heirarchy of edges based on
  the edge scores provided in their ICE records. Edges are divided
  into two groups: 1) a high-scoring group, i.e. interactions with
  higher confidence levels; and 2) a low-scoring group,
  i.e. interactions with lower confidence levels. For AAEL we have
  interactions from two sources: 1) the Guo paper, and 2) a String
  network. Each source uses a different range of scores, so there are
  rules to handle each source."

  :dependency "rules/hanalyzer/edges/ppi/aael/step_a/aael_ppi_edges"
  
  :head ((?/edge rdf/type iaohan/HAN_0000011) ;; HAN:low_confidence_ppi_asserted_edge
         (?/edge rdf/type iaohan/HAN_0000012)) ;; HAN:low_confidence_ppi_asserted_edge_string


  :reify ()

  :body ((?/edge rdf/type iaohan/HAN_0000009) ;; HAN:ppi_asserted_edge
         (?/edge iaohan/interaction_source ["String" "en"])
         (?/edge iaohan/interaction_score ?/score)
         (<= ?/score 600)) 

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
