`{:name "protein-protein-interactions"
  :description "This rule creates PPIs in BioWorld for protein-protein
  interactions asserted from various sources."
  
  :head ((?/interaction rdfs/subClassOf obo/GO_0005488) ;;GO:binding
         (?/interaction rdfs/label ["binding"]) ; transfer label to the subclass
         (?/record obo/IAO_0000219 ?/interaction) ; the ICE record denotes the interaction
                   
         ;; create subclasses of the bioentities (genes in this case)
         (?/gene1_sc rdfs/subClassOf ?/gene1)
         ;; TODO: UNCOMMENT below once kabob genes have labels
         ;;(?/gene1_sc rdfs/label ?/gene1_label) ; transfer label to the subclass
         (?/gene2_sc rdfs/subClassOf ?/gene2)
         ;; TODO: UNCOMMENT below once kabob genes have labels
         ;;(?/gene2_sc rdfs/label ?/gene2_label) ; transfer label to the subclass

         (?/r1 rdf/type owl/Restriction)
         (?/r1 owl/onProperty obo/RO_0000057) ; has_participant
         (?/r1 owl/someValuesFrom ?/gene1_sc)

         (?/r2 rdf/type owl/Restriction)
         (?/r2 owl/onProperty obo/RO_0000057) ; has_participant
         (?/r2 owl/someValuesFrom ?/gene2_sc)

         ;; create a cardinality restriction of 2 on the has_participant property
         ;;(?/rcard rdf/type owl/Restriction)
         ;;(?/rcard owl/onProperty obo/RO_0000057) ; has_participant
         ;;(?/rcard owl/cardinality 2) ;; binary interactions must have 2 participants.

         (?/interaction rdfs/subClassOf ?/r1)
         (?/interaction rdfs/subClassOf ?/r2))
         ;;(?/interaction rdfs/subClassOf ?/rcard))

  :body
  ;; get binary interaction records
  ((?/genefield kiao/hasTemplate
                iaounknown/InteractionWithScoreFileRecord_interactingGene1DataField1)
   (?/genefield obo/IAO_0000219 ?/geneIce)
   (?/geneIce obo/IAO_0000219 ?/gene1)
   (?/record obo/BFO_0000051 ?/genefield)
   (?/record obo/BFO_0000051 ?/otherGeneField)
   (?/otherGeneField kiao/hasTemplate
                     iaounknown/InteractionWithScoreFileRecord_interactingGene2DataField1)
   (?/otherGeneField obo/IAO_0000219 ?/otherGeneIce)
   (?/otherGeneIce obo/IAO_0000219 ?/gene2))

  :reify ([?/interaction {:ln (:sha-1 ?/gene1 ?/gene2 obo/GO_0005488)
                          :ns "kbio" :prefix "I_"}]
          [?/r1 {:ln (:restriction)
                 :ns "kbio" :prefix "RESTR_"}]
          [?/r2 {:ln (:restriction)
                 :ns "kbio" :prefix "RESTR_"}]
          ;;[?/rcard {:ln (:restriction)
          ;;          :ns "kbio" :prefix "R_"}]
          [?/gene1_sc {:ln (:sha-1 ?/interaction ?/gene1)
                          :ns "kbio" :prefix "G_"}]
          [?/gene2_sc {:ln (:sha-1 ?/interaction ?/gene2)
                          :ns "kbio" :prefix "G_"}])

  :options {:magic-prefixes [["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }
