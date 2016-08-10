
`{:name "jiang-compute-pms"
  :description "This rule computes 'probability of the minimum subsumer', that is
                the minimum probability of the parental concepts shared by two
                concepts. This value is reified into a ProbMinSub instance.
                [Jiang & Conrath 1998: http://arxiv.org/pdf/cmp-lg/9709008.pdf]"

  :dependency "info_content/resnik/resnik-concept-probability-go-bp-aael"
  
  :head ((?/pms rdf/type iaohan/GOBP_Pair_aael)
         (?/pms obo/RO_0000057 ?/c1) ;; RO:has_participant
         (?/pms obo/RO_0000057 ?/c2) ;; RO:has_participant
         (?/pms iaohan/prob-min-subsumer {:num_type :float
                                       :as ?/min_jiang_d
                                       :eqn ["MIN(" ?/parent_prob ")"]
                                       :group_by [?/c1 ?/c2]}))

  :body ((?/shared_parent oboInOwl/hasOBONamespace ["biological_process"])
         (?/c1 [rdfs/subClassOf 1 5] ?/shared_parent) ;; rdfs:subClassOf{1,5}
         (?/c1 oboInOwl/hasOBONamespace ["biological_process"])
         (?/c2 [rdfs/subClassOf 1 5] ?/shared_parent) ;; rdfs:subClassOf{1,5}
         (?/c2 oboInOwl/hasOBONamespace ["biological_process"])
         (!= ?/c1 ?/c2)
         (?/shared_parent iaohan/resnik-concept-prob-aael ?/parent_prob))
  
  :reify ([?/pms {:ln (:sha-1 ?/c1 ?/c2)
                 :ns "iaohan" :prefix "PMS_"}])
  
  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
