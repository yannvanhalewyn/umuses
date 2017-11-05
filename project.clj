(defproject sheet-bucket "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.6.1"

  :dependencies [[org.clojure/clojure "1.9.0-beta2"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/core.async "0.2.374"
                  :exclusions [org.clojure/tools.reader]]
                 [com.stuartsierra/component "0.3.2"]
                 ;; CLJS
                 [reagent "0.7.0"]]

  :profiles {:dev {:dependencies [[devcards "0.2.4" :exclusions [cljsjs/react]]
                                  [binaryage/devtools "0.8.3"]
                                  [reloaded.repl "0.2.4"]
                                  [figwheel-sidecar "0.5.14"]
                                  [org.clojure/test.check "0.9.0"] ;; For cljs.spec
                                  [com.cemerick/piggieback "0.2.2"]]
                   :source-paths ["src" "env/dev"]
                   :repl-options {:init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :plugins [[lein-figwheel "0.5.14"]
            [lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :cljsbuild {:builds [{:id "cards"
                        :source-paths ["src"]
                        :figwheel {:devcards true}
                        :compiler {:main cards.core
                                   :asset-path "js/compiled/cards-out"
                                   :output-to "resources/public/js/cards.js"
                                   :output-dir "resources/public/js/compiled/cards-out"}}

                       {:id "dev"
                        :source-paths ["src" "env/dev"]
                        :figwheel {:on-jsload "sheet-bucket.core/render"}
                        :compiler {:main sheet-bucket.core
                                   ;; Figwheel injects script tags for
                                   ;; development. This is the location
                                   ;; for the compiled resources
                                   :asset-path "js/compiled/out"
                                   ;; The outputted main bundle
                                   :output-to "resources/public/js/app.js"
                                   ;; Where to compile assets needed for
                                   ;; the development bundle, required by
                                   ;; :asset-path
                                   :output-dir "resources/public/js/compiled/out"}}

                       {:id "test"
                        :source-paths ["src" "test" "env/test"]
                        :figwheel true
                        :compiler {:main sheet-bucket.test-runner
                                   :asset-path "js/compiled/test-out"
                                   :output-to "resources/public/js/test.js"
                                   :output-dir "resources/public/js/compiled/test-out"
                                   :optimizations :none}}

                       {:id "min"
                        :source-paths ["src"]
                        :compiler {:output-to "resources/public/js/sheet-bucket.js"
                                   :main sheet-bucket.core
                                   :optimizations :advanced
                                   :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]})
