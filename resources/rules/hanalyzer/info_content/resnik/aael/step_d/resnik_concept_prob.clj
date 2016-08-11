`{:name "resnik-concept-probability-go-cc-aael"
  :description "This rule computes the Resnik concept probability for each
                GO cellular_component concept using information from ggp 
                annotations to GO CC concepts as described in Resnik 1995, 
                http://arxiv.org/pdf/cmp-lg/9511007.pdf."
  :dependency ("annotation-counts-go-bp-aael")
  :head ((?/concept iaohan/resnik-concept-prob-aael {:num_type :float
                                                :as ?/prob
                                                :eqn [?/count "/" ?/total]})) 

  :body
  ((obo/GO_0005575 iaohan/annotation-count-aael ?/total)
   (?/concept oboInOwl/hasOBONamespace ["cellular_component"])
   (?/concept iaohan/annotation-count-aael ?/count))

  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }

