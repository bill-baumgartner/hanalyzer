`{:name "aael-hanalyzer-nodes"
  :description "This rule produces hanalyzer nodes for yellow fever mosquito. One node
  is created for each mosquito GGPV. Each node may end up with multiple labels as the
  labels are extracted from the GGPs themselves. "
  :head ((?/node rdf/type iaohan/Node) ;interaction
         (?/node rdfs/label ?/label)
         (?/node iaohan/denotes ?/ggpv_sc))

  :body
  ((?/taxon_r owl/onProperty obo/RO_0002162) ;; in_taxon
  (?/taxon_r rdf/type owl/Restriction)
  (?/taxon_r owl/someValuesFrom ?/taxon_sc)
  (?/taxon_sc [rdfs/subClassOf *] obo/NCBITaxon_7159) ;; yellow fever mosquito

  (?/ggpv_sc rdfs/subClassOf ?/taxon_r)
  (?/ggpv_sc [rdfs/subClassOf *] ?/ggpv)
  (?/ggpv rdf/type kbio/GeneSpecificGorGPorVClass)
  (?/ggpv_sc rdfs/label ?/label))

  :reify ([?/node {:ln (:localname ?/ggpv)
                          :ns "iaohan" :prefix "HANODE_"}])

  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
