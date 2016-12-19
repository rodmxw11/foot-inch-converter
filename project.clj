(defproject foot-inch-converter "0.1.0-SNAPSHOT"
  :description "Convert between fractional and decimal foot-inch measurements"
  :url "https://github.com/rodmxw11/foot-inch-converter"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main foot-inch-converter.core
  :dependencies [
                 [org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.293" :scope "provided"]
                 [lein-doo "0.1.7"]
                 ]
  :source-paths ["src"]
  :test-paths ["test"]
  :uberjar-name "foot-inch-converter-lein.jar"

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (run) and
  ;; (browser-repl) live.
  :repl-options {:init-ns user}


  :profiles
  {
   ;; :dev {:aliases {"test-all" ["with-profile" "dev,1.9:dev,1.7:dev" "test"]}}
   ;; :1.9 {:dependencies [[org.clojure/clojure "1.9.0-alpha12"]]}
   ;; :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
   :uberjar {:aot :all}
   }

  :plugins [[lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.7"]
            [lein-figwheel "0.5.8"]
            ]

  :hooks [leiningen.cljsbuild]
  :cljsbuild {
              :test-commands { "nashorn-test" ["jjs" "nashorn-out/tests.js"] }
              :builds [
                       #_{
                        :id "dev"
                        :source-paths ["src"]
                        ;; :figwheel true
                        :compiler {
                                   :output-to "out/main.js"
                                   :optimizations :whitespace
                                   :pretty-print true
                                   }
                        }
                       {
                        ;; lein doo nashorn test-nashorn once
                        :id "test-nashorn"
                        :source-paths ["src" "test"]
                        :compiler {
                                   :main runners.doo
                                   :optimizations :simple
                                   :target :nashorn
                                   :output-to "nashorn-out/tests.js"
                                   }
                        }
                       #_{
                        :id "test-phantom"
                        :source-paths ["src" "test"]
                        :compiler {
                                   :main runners.doo
                                   :optimizations :whitespace
                                   :target :phantom
                                   :output-to "phantom-out/tests.js"
                                   }
                        }
                       #_{
                        :id "test-slimer"
                        :source-paths ["src" "test"]
                        :compiler {
                                   :main runners.doo
                                   :optimizations :whitespace
                                   :target :slimer
                                   :output-to "slimer-out/tests.js"
                                   }
                        }
                       #_{
                        :id "test-rhino"
                        :source-paths ["src" "test"]
                        :compiler {
                                   :main runners.doo
                                   :optimizations :whitespace
                                   :target :rhino
                                   :output-to "rhino-out/tests.js"
                                   }
                        }
                       #_{
                        :id "test-node"
                        :source-paths ["src" "test"]
                        :compiler {
                                   :main runners.doo
                                   :optimizations :simple
                                   :target :nodejs
                                   :output-to "node-out/tests.js"
                                   }
                        }
                       ]}
  :clean-targets ^{:protect false} [:target-path
                                    :compile-path
                                    "node-out"
                                    "phantom-out"
                                    "slimer-out"
                                    "node-out"
                                    "nashorn-out"
                                    "rhino-out"
                                    "out"
                                    ]
  )
