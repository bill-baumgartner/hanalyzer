`{:name "jiang-distance-go-cc-aael"
  :description "This rule computes the Jiang distance using previously
                calculated Resnik concept probabilities for each
                GO biological_process concept, as well as the previously
                computed probability of minimum subsumer (pms) in jiang/step-a. 
                [Jiang & Conrath 1998: http://arxiv.org/pdf/cmp-lg/9709008.pdf]"

  :dependency ["resnik-concept-probability-go-bp-aael" "jiang-compute-pms"]
  
  :head ((?/pair iaohan/jiang_distance {:num_type :float
                                        :as ?/jiang_d
                                        :eqn ["-2*ccp_sparql_ext:ln(" ?/pms_prob
                                              ") - (ccp_sparql_ext:ln(" ?/p1
                                              ") + ccp_sparql_ext:ln(" ?/p2 "))"]}))

  :body ((?/pair rdf/type iaohan/GOCC_Pair_aael)
         (?/pair iaohan/prob-min-subsumer ?/pms_prob)
         (?/pair obo/RO_0000057 ?/c1)
         (?/pair obo/RO_0000057 ?/c2)
         (!= ?/c1 ?/c2)
         (?/c1 iaohan/resnik-concept-prob-aael ?/p1)
         (?/c2 iaohan/resnik-concept-prob-aael ?/p2))
  
  :reify ()
  
  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
