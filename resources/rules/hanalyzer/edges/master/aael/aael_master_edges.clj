

`{:name "master-edges-aael"
  
  :description "This rule produces a 'master' reified edge between two
  hanalyzer nodes if an 'interaction' between the two nodes is
  asserted by some resource. The collection of 'master' edges
  represents what was often referred to as the hanalyzer 'knowledge
  network'. The score of a 'master' edge is computed using the
  Noisy-OR function over the reliability scores of the edges that
  assert an interaction between the nodes."

  :head ((?/master_edge rdf/type iaohan/HAN_0000001) ;; HAN:master_edge 
         (?/master_edge iaohan/linksNode ?/node1)
         (?/master_edge iaohan/linksNode ?/node2)
         (?/master_edge iaohan/asserted_by ?/source_edge_types)
         (?/master_edge iaohan/reliability_aael ?/reliability_scores))
         
 
  :reify ([?/master_edge {:ln (:sha-1 iaohan/HAN_0000001 ?/node1 ?/node2)
                   :ns "iaohan" :prefix "HANEDGE_MASTER_"}])

  :sparql-string "PREFIX franzOption_clauseReorderer: <franz:identity> 
                  PREFIX franzOption_chunkProcessingAllowed: <franz:yes> 
                  PREFIX iaohan: <http://kabob.ucdenver.edu/iao/hanalyzer/> 

                  select ?node1 ?node2 
                  (group_concat(?norm_reliability ; separator = ',') AS ?reliability_scores)
                  (group_concat(?edge_type ; separator = ',') AS ?source_edge_types)
                  {
  	             ?edge iaohan:linksNode ?node1 .
                     ?edge iaohan:linksNode ?node2 .
                     FILTER (?node1 != ?node2 && STR(IRI(?node1)) < STR(IRI(?node2)))
                     ?edge rdf:type ?edge_type .
                     ?edge_type iaohan:normalized_reliability_aael ?norm_reliability .
                  
}
                  group by ?node1 ?node2"

  ;; this binding modification function combines the extracted
  ;; group_concat normalized reliability scores for each edge using
  ;; the NOISY-OR function, and replaces the binding value with the
  ;; NOISY-OR output.
  ;;
  ;; WORK-AROUND: Note that this function is just a place holder at
  ;; the moment. A work-around has been implemented in the
  ;; kabob/run_rules.clj script that invokes the noisy or function if
  ;; a rule has a non-nil :binding-mod-fn. The work-around is
  ;; necessary as I've so far been unable to define a function here
  ;; and have it successfully process the bindings in run_rules. 
  :binding-mod-fn (fn [] )
  
  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }




