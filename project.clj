(defproject traduki "0.1.0-SNAPSHOT"
  :description "Inject static translations into Enlive elements using HTML data tags"
  :url "https://github.com/elrob/traduki"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [midje "1.6.3"]
                 [enlive "1.1.5"]]
  :plugins [[lein-midje "3.1.3"]])
