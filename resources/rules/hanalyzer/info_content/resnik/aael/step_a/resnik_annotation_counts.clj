`{:name "annotation-counts-go-bp-aael"
  :description "This rule represents the first step when computing Resnik 
                concept probabilities for each GO biological_process concept. 
                This rule records the number of gene/protein annotations 
                associated with each term (and all of the terms children). 
                For details, see Resnik 1995: 
                http://arxiv.org/pdf/cmp-lg/9511007.pdf."
  :head ((?/super iaohan/annotation-count-aael
                  [:count :distinct ?/participant_r])) 

  :body ((?/sub oboInOwl/hasOBONamespace ["biological_process"])
   (?/sub [rdfs/subClassOf *] ?/mid)
   (?/mid oboInOwl/hasOBONamespace ["biological_process"])
   (?/mid [rdfs/subClassOf *] ?/super)
   (?/super oboInOwl/hasOBONamespace ["biological_process"])
   (?/sc rdfs/subClassOf ?/sub)
   (?/sc rdfs/subClassOf ?/participant_r)
   (?/participant_r rdf/type owl/Restriction)
   (?/participant_r owl/onProperty obo/RO_0000057)
   (?/participant_r owl/someValuesFrom ?/protein_sc)
   (?/protein_sc rdfs/subClassOf ?/protein)
   (?/protein rdfs/subClassOf ?/taxon_r)
   (?/taxon_r owl/onProperty obo/RO_0002162) ;; in_taxon
   (?/taxon_r rdf/type owl/Restriction)
   (?/taxon_r owl/someValuesFrom ?/taxon_sc)
   (?/taxon_sc [rdfs/subClassOf *] obo/NCBITaxon_7159)) ;; yellow fever mosquito

  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }


