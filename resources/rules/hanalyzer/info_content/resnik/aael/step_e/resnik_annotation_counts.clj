`{:name "annotation-counts-go-mf-aael"
  :description "This rule represents the first step when computing
                Resnik concept probabilities for each GO
                molecular_function concept.  This rule records the
                number of gene/protein annotations associated with
                each term (and all of the terms children).  For
                details, see Resnik 1995:
                http://arxiv.org/pdf/cmp-lg/9511007.pdf. Note that
                because molecular functions are not currently
                represented in the BIO side of KaBOB, this rule
                reaches into the ICE side to grab the requisite
                information."
  :head ((?/super iaohan/annotation-count-aael
                  [:count :distinct ?/record])) 

  :body ((?/sub oboInOwl/hasOBONamespace ["molecular_function"])
         (?/sub [rdfs/subClassOf *] ?/mid)
         (?/mid oboInOwl/hasOBONamespace ["molecular_function"])
         (?/mid [rdfs/subClassOf *] ?/super)
         (?/super oboInOwl/hasOBONamespace ["molecular_function"])
         (?/go_ice obo/IAO_0000219 ?/sub) ;; IAO:denotes
         (?/go_id_field obo/IAO_0000219 ?/go_ice) ;; IAO:denotes
         (?/go_id_field kiao/hasTemplate iaogoa/GpAssociationGoaUniprotFileData_goIDDataField1)
         (?/record obo/BFO_0000051 ?/go_id_field) ;; BFO:has_part 
         (?/record obo/BFO_0000051 ?/protein_id_field) ;; BFO:has_part
         
         ;; filter out the negations
         (:optional
           ((?/record obo/BFO_0000051 ?/qualfv) ; has_part
            (?/qualfv kiao/hasTemplate iaogoa/GpAssociationGoaUniprotFileData_qualifierDataField1)
            (?/qualfv obo/IAO_0000219 ?/qualifier)))
         (:or (:not (:bound ?/qualifier))
               (:not (:regex ?/qualifier "^NOT" "i")))
         
         (?/protein_id_field kiao/hasTemplate iaogoa/GpAssociationGoaUniprotFileData_databaseObjectIDDataField1)
         (?/protein_id_field obo/IAO_0000219 ?/protein_ice) ;; IAO:denotes
         (?/protein_ice  obo/IAO_0000219 ?/protein) ;; IAO:denotes
         (?/protein rdfs/subClassOf ?/taxon_r)
         (?/taxon_r owl/onProperty obo/RO_0002162) ;; in_taxon
         (?/taxon_r rdf/type owl/Restriction)
         (?/taxon_r owl/someValuesFrom obo/NCBITaxon_7159)) ;; yellow fever mosquito
         
  :options {:magic-prefixes [;;["franzOption_clauseReorderer" "franz:identity"]
                             ["franzOption_chunkProcessingAllowed" "franz:yes"]]}
  }


