;; creates a DOI file that distinguishes edges that are supported only
;; by PPI data vs edges that are supported by at least one other kind
;; of data, e.g. shared biological process
;;
;; This script processes the edgeExperts file 
(ns hanalyzer.export.renodoi-doi-gen-only-ppi-edge)

(defn- shorten-namespace [s]
  "converts
  '<http://kabob.ucdenver.edu/iao/hanalyzer/HANODE_GorGPorV_BIO_ea674551f8c4f5bd2dada90cb397c2cb>'
  to 'iaohan/HANODE_GorGPorV_BIO_ea674551f8c4f5bd2dada90cb397c2cb'"
  (clojure.string/upper-case
   (clojure.string/replace
    (clojure.string/replace s "<http://kabob.ucdenver.edu/iao/hanalyzer/" "iaohan/")
    #">$" ""))) ;; remove the trailing '>'

(defn build-only-ppi-support-doi-file [options]
  "Builds a file in the RenoDoI DoI function format to distinguish
  between edges that are supported only by PPI data vs edges that are
  supported by at least one other kind of data, e.g. shared biological
  processseed nodes and nodes that were added as neighbors."
  (let [edge-experts-file  (str (:output-directory options)
                                "/commonattributes-plugin-files/network.edgeExperts.eda")
        edges (into #{} (clojure.string/split (slurp edge-experts-file) #"\n"))
        output-file-name (str (:output-directory options) "/doi/only-ppi.doi.csv")]
    (clojure.java.io/make-parents output-file-name)
    (with-open [w (clojure.java.io/writer output-file-name)]
      (.write w (str "NodeID,NodeID,1=only ppi, -1=at least one other source\n"))
      (doall (map #(let [experts-str (last (clojure.string/split % #" = "))
                         experts-str-no-ppi (-> (clojure.string/replace experts-str "HC_PPI_String" "")
                                                (clojure.string/replace "LC_PPI_String" "")
                                                (clojure.string/replace "HC_PPI_Guo" "")
                                                (clojure.string/replace "LC_PPI_Guo" "")
                                                  (clojure.string/replace "/" ""))
                         nodes-str (first (clojure.string/split % #" = "))
                         node1-uri (first (clojure.string/split nodes-str #" "))
                         node2-uri (last (clojure.string/split nodes-str #" "))]
                     
                     ;; if experts-str-no-ppi is empty at this point,
                     ;; then the edge is supported entirely by PPI
                     ;; sources. If not, it is supported by something
                     ;; other than PPI.
                     (if (clojure.string/blank? experts-str-no-ppi)
                       (.write w (str (shorten-namespace node1-uri)
                                      ","
                                      (shorten-namespace node2-uri)
                                      ",1.0\n"))
                       (.write w (str (shorten-namespace node1-uri)
                                      ","
                                      (shorten-namespace node2-uri)
                                      ",-1.0\n"))))
                  edges)))))
