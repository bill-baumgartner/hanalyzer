(ns hanalyzer.export.renodoi-doi-gen-nodes-v-neighbors)

(defn- shorten-namespace [s]
  "converts
  '<http://kabob.ucdenver.edu/iao/hanalyzer/HANODE_GorGPorV_BIO_ea674551f8c4f5bd2dada90cb397c2cb>'
  to 'iaohan/HANODE_GorGPorV_BIO_ea674551f8c4f5bd2dada90cb397c2cb'"
  (clojure.string/upper-case
   (clojure.string/replace
    (clojure.string/replace s "<http://kabob.ucdenver.edu/iao/hanalyzer/" "iaohan/")
    #">$" ""))) ;; remove the trailing '>'

(defn build-nodes-v-neighbors-doi-file [options]
  "Builds a file in the RenoDoI DoI function format to distinguish
  between seed nodes and nodes that were added as neighbors."
  (let [seed-nodes (into #{} (clojure.string/split (slurp (:seed-nodes-file options)) #"\n"))
        all-nodes (into #{} (clojure.string/split (slurp (:id_file options)) #"\n"))]
    (with-open [w (clojure.java.io/writer
                   (str (:output-directory options) "/doi/nodes_v_neighbors.doi.csv"))]
      (.write w (str "NodeID,sig_fatBody\n"))
      (doall (map #(if (contains? seed-nodes %)
                     (.write w (str (shorten-namespace %) ",1.0\n"))
                     (.write w (str (shorten-namespace %) ",-1.0\n")))
                  all-nodes)))))
