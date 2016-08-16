`{:name "consensus-overlap-count-pw-aael"
  :description "This rule counts the number of unique node pairings
  that are asserted to 'interact' because they are members of the same
  pathway and that also overlap with the node pairings in the
  consensus set of 'trusted' interactions."

  :dependency ""
  
  :head ((iaohan/HAN_0000008 iaohan/consensus_overlap_count_aael ?/count)) ;; HAN:shared_pathway_asserted_edge

  :reify ()

  :sparql-string "PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
                  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
                  SELECT (count (*) as ?count) {
                    SELECT DISTINCT ?consensus_edge
                    WHERE {  
                      ?edge rdf:type iaohan:HAN_0000008 . # HAN:shared_pathway_asserted_edge
   		      ?edge iaohan:linksNode ?node1 .
   		      ?edge iaohan:linksNode ?node2 .
                      ?consensus_edge iaohan:linksNode ?node1 .
                      ?consensus_edge iaohan:linksNode ?node2 .
                      ?consensus_edge rdf:type iaohan::HAN_0000016 . # HAN:consensus_set_edge
   		      FILTER (?node1 != ?node2 && STR(IRI(?node1)) < STR(IRI(?node2)))
                     }
                   }"

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }

`{:name "asserted-node-interaction-count-pw-aael"
  :description "This rule counts the number of unique node pairings
  that are asserted to 'interact' because they are members of the same
  pathway."

  :dependency ""
  
  :head ((iaohan/HAN_0000008 iaohan/asserted_node_interaction_count_aael ?/count)) ;; HAN:shared_pathway_asserted_edge

  :reify ()

  :sparql-string "PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
                  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
                  SELECT (count (*) as ?count) {
                    SELECT ?node1 ?node2
                    WHERE {  
                      ?edge rdf:type iaohan:HAN_0000008 . # HAN:shared_pathway_asserted_edge
   		      ?edge iaohan:linksNode ?node1 .
   		      ?edge iaohan:linksNode ?node2 .
                      FILTER (?node1 != ?node2 && STR(IRI(?node1)) < STR(IRI(?node2)))
                     }
                   }"

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }







