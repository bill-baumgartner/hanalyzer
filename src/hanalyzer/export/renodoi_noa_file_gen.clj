;; RenoDOI uses two file types that are redundant in regards to
;; information content with the NodeIDs.txt and Id2TermMappings.txt
;; files. Instead of creating the files via queries to the KB, this
;; script creates them from the already generated NodeIDs.txt and
;; Id2TermMappings.txt files.
(ns hanalyzer.export.renodoi-noa-file-gen)

(defn slash-delimited-string [s id-to-label-map]
  "creates a string representation of the input collection that uses
  '///' as its delimiter"
  (apply str (drop-last ;; drop trailing "///"
              (interleave (map id-to-label-map
                               (clojure.string/split (first s) #","))
                          (repeat "///")))))

(defn- load-id-to-label-map [options source-string]
  "The id2term mapping files contain a mapping from term ID to label
  on each line, tab-delimited.This function creates a map from id to
  label."
  (let [id2term-mapping-file (str (:output-directory options)
                                  "/commonattributes-plugin-files/network."
                                  source-string ".Id2TermMappings.txt")]
    (into {} (map #(clojure.string/split % #"\t")
                  (clojure.string/split (slurp id2term-mapping-file) #"\n")))))


(defn build-noa-files-for-source [options source-string]
    (prn (str "Building noa files [" source-string "]..."))
  (let [output-file-name-ids (str (:output-directory options)
                                  "/commonattributes-plugin-files/network."
                                  source-string ".ids.noa")
        output-file-name-names (str (:output-directory options)
                                    "/commonattributes-plugin-files/network."
                                    source-string ".names.noa")
        term-id-to-label-map (load-id-to-label-map options source-string)
        node-ids-txt-file (str (:output-directory options)
                               "/commonattributes-plugin-files/network."
                               source-string ".NodeIds.txt")]
    (clojure.java.io/make-parents output-file-name-ids)
    (with-open [w_ids (clojure.java.io/writer output-file-name-ids)
                w_names (clojure.java.io/writer output-file-name-names)]
      (.write w_names (str source-string "\n"))
      (.write w_ids (str source-string "\n"))
      (doall (map #(let [node-id (first (clojure.string/split % #" = "))
                         ids     (rest (clojure.string/split % #" = "))
                         id-list (slash-delimited-string ids identity)
                         name-list (slash-delimited-string ids term-id-to-label-map)]
                     (.write w_names (str node-id " = " name-list "\n"))
                     (.write w_ids (str node-id " = " id-list "\n")))
                  ;; skip the first two lines
                  (rest (rest (clojure.string/split
                               (slurp node-ids-txt-file)
                               #"\n"))))))))

(defn build-noa-files [options]
  (build-noa-files-for-source options "Kegg")
  (build-noa-files-for-source options "GeneOntology_BP")
  (build-noa-files-for-source options "GeneOntology_CC")
  (build-noa-files-for-source options "GeneOntology_MF"))
