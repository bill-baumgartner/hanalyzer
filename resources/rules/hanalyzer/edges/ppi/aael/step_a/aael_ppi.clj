
`{:name "aael-ppi-edges"
  :description "This rule produces reified edges between two hanalyzer nodes
  that have were asserted to interact by a PPI resource."

  :head (;; creates a reified edge of type iaohan/PpiEdge that links
         ;; the two hanalyzer nodes that denote GGPs that participate
         ;; in a protein-protein interaction
         (?/edge rdf/type iaohan/PpiEdge) 
         (?/edge iaohan/linksNode ?/node1)
         (?/edge iaohan/linksNode ?/node2)
         (?/edge iaohan/denotes ?/interaction)
         (?/edge iaohan/interaction_source ?/source)
         (?/edge iaohan/interaction_score ?/score)
         (?/edge rdfs/label ?/edgeLabel))

  :reify ([?/edge {:ln (:sha-1 iaohan/PpiEdge ?/node1 ?/node2 ?/go)
                   :ns "iaohan" :prefix "HANEDGE_PPI_"}])

  :body ((?/interaction rdfs/subClassOf obo/GO_0005488) ;; GO:binding
         (?/interaction rdfs/label ?/interaction_label) 
         (?/interaction rdfs/subClassOf ?/participant_r1)
         (?/participant_r1 owl/onProperty obo/RO_0000057)
         (?/participant_r1 owl/someValuesFrom ?/gene1_sc)
         (?/interaction rdfs/subClassOf ?/participant_r1)
         (!= ?/participant_r1 ?/participant_r2)
         (?/participant_r2 owl/onProperty obo/RO_0000057)
         (?/participant_r2 owl/someValuesFrom ?/gene2_sc)
         (?/gene1_sc rdfs/subClassOf ?/gene1)
         (?/gene2_sc rdfs/subClassOf ?/gene2)
         (?/gene1 rdfs/subClassOf ?/taxon_r)
         (?/taxon_r owl/onProperty obo/RO_0002162) ;; RO:in_taxon
         (?/taxon_r rdf/type owl/Restriction)
         (?/taxon_r owl/someValuesFrom obo/NCBITaxon_7159) ;; yellow fever mosquito
         (?/gene2 rdfs/subClassOf ?/taxon_r)
         (?/record obo/IAO_0000219 ?/interaction) ; the ICE record denotes the interaction
         (?/record kiao/hasTemplate iaounknown/InteractionWithScoreFileRecordSchema1)
         (?/record obo/BFO_0000051 ?/sourceField)
         (?/sourceField kiao/hasTemplate iaounknown/InteractionWithScoreFileRecord_sourceDataField1)
         (?/sourceField obo/IAO_0000219 ?/source)
         (?/record obo/BFO_0000051 ?/scoreField)
         (?/scoreField kiao/hasTemplate iao/unknown/InteractionWithScoreFileRecord_scoreDataField1)
         (?/scoreField obo/IAO_00000219 ?/score)
         
         (?/node1 iaohan/denotes ?/gene1)         
         (?/node2 iaohan/denotes ?/gene2)
         )

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }



