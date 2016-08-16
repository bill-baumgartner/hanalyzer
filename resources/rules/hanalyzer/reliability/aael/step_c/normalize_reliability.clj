`{:name "normalize-reliability"
  :description "This rule normalizes the source reliability scores
  such that they can be treated as probability values. See footnote
  'f' in Leach et al
  2007 (http://www.ncbi.nlm.nih.gov/pubmed/17990508). The normalized
  value for a source = r/(max(r)+1)."

  :dependency ""
  
  :head ((?/edge_type iaohan/normalized_reliability {:num_type :float
                                                     :as ?/norm_reliability
                                                     :eqn [?/reliability_score "/"
                                                          "(" ?/max_r "+ 1 )"]})) 

  :reify ()

  :sparql-string "PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 
                  select ?edge_type ?reliability_score ?max_r
                  WHERE {
  	             ?edge_type iaohan:reliability_aael ?reliability_score
	             {
  		        select (max(?reliability_score) as ?max_r) {
     	                     ?edge_type iaohan:reliability_aael ?reliability_score
    	                }
  	             }
                  }"

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
