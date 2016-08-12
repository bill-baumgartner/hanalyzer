`{:name "resnik-concept-probability-go-bp-aael"
  :description "This rule computes the Resnik concept probability for each
                GO biological_process concept using information from ggp 
                annotations to GO BP concepts as described in Resnik 1995, 
                http://arxiv.org/pdf/cmp-lg/9511007.pdf."
  :dependency ("annotation-counts-go-bp-aael")
  :head ((?/concept iaohan/resnik-concept-prob-aael {:num_type :float
                                                :as ?/prob
                                                :eqn [?/count "/" ?/total]})) 

  :body
  ((obo/GO_0008150 iaohan/annotation-count-aael ?/total)
   (?/concept oboInOwl/hasOBONamespace ["biological_process"])
   (?/concept iaohan/annotation-count-aael ?/count))

  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }

