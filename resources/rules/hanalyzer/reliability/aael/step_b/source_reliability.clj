`{:name "source-reliability-aael"
  :description "This rule computes the source reliabilities using the counts from reliability/aael/step_a. Reliability is computed as described in Leach et al 2007 (http://www.ncbi.nlm.nih.gov/pubmed/17990508)."

  :dependency "rules/hanalyzer/reliability/aael/step_a"
  
  :head ((?/edge_type iaohan/reliability_aael {:num_type :float
                                               :as ?/reliability_score
                                               :eqn [?/consensus_overlap_count "/"
                                                     ?/asserted_interaction_count]}))
  
  :reify ()

  :body ((?/edge_type [rdfs/subClassOf *] iaohan/HAN_0000002) ;; HAN:source-asserted_edge
         (?/edge_type iaohan/consensus_overlap_count_aael ?/consensus_overlap_count)
         (?/edge_type iaohan/asserted_node_interaction_count_aael ?/asserted_interaction_count))

  
  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
