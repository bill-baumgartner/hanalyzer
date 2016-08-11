`{:name "annotation-counts-go-cc-aael"
  :description "This rule represents the first step when computing Resnik 
                concept probabilities for each GO cellular_component concept. 
                This rule records the number of gene/protein annotations 
                associated with each term (and all of the terms children). 
                For details, see Resnik 1995: 
                http://arxiv.org/pdf/cmp-lg/9511007.pdf."
  :head ((?/super iaohan/annotation-count-aael
                  [:count :distinct ?/loc])) 

  :body ((?/loc rdfs/subClassOf obo/GO_0051179)
         (?/loc rdfs/subClassOf ?/of_r)
         (?/of_r rdf/type owl/Restriction)
         (?/of_r owl/onProperty obo/RO_0002313) ;; transports or maintains localization of
         (?/of_r owl/someValuesFrom ?/protein_sc)
         (?/protein_sc rdfs/subClassOf ?/protein)
         (?/protein rdfs/subClassOf ?/taxon_r)
         (?/taxon_r owl/onProperty obo/RO_0002162) ;; in_taxon
         (?/taxon_r rdf/type owl/Restriction)
         (?/taxon_r owl/someValuesFrom ?/taxon_sc)
         (?/taxon_sc [rdfs/subClassOf *] obo/NCBITaxon_7159) ;; yellow fever mosquito
         (?/loc rdfs/subClassOf ?/to_r)
         (?/to_r rdf/type owl/Restriction)
         (?/to_r owl/onProperty obo/RO_0002339) ;; has target end location
         (?/to_r owl/someValuesFrom ?/sc)
         (?/sc rdfs/subClassOf ?/sub)
         (?/sub oboInOwl/hasOBONamespace ["cellular_component"])
         (?/sub [rdfs/subClassOf *] ?/mid)
         (?/mid oboInOwl/hasOBONamespace ["cellular_component"])
         (?/mid [rdfs/subClassOf *] ?/super)
         (?/super oboInOwl/hasOBONamespace ["cellular_component"]))

  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }


