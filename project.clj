(defproject edu.ucdenver.ccp/hanalyzer-kabob-layer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [edu.ucdenver.ccp/kabob-build "1.3.0-SNAPSHOT"
                  :exclusions [org.slf4j/slf4j-log4j12 potemkin]]]
  :profiles {:dev {:dependencies [[midje "1.8.3"]]
                   :plugins [[lein-midje "3.2"]]}
             :uberjar {:aot :all}}
  )
