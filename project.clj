(defproject rsvp-backend "0.1.0-SNAPSHOT"
  :description "RSVP Management System"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[bidi "2.1.2"]
                 [cheshire "5.4.0"]
                 [clj-http "2.0.0"]
                 [com.taoensso/faraday "1.9.0"]
                 [com.taoensso/timbre "2.7.1"]
                 [levand/immuconf "0.1.0"]
                 [medley "0.7.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.nrepl "0.2.6"]
                 [ring-middleware-format "0.3.2" :exclusions [ring]]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [ring/ring-json "0.4.0"]]
  :plugins       [[refactor-nrepl "2.3.0-SNAPSHOT"]
                 [cider/cider-nrepl "0.12.0"]]
  :main ^:skip-aot rsvp-backend.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
