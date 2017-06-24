(defproject efp-project "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :plugins [[lein2-eclipse "2.0.0"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
   :dependencies [[org.clojure/clojure "1.8.0"]
					[compojure "1.1.8"]
					[http-kit "2.1.16"]]
	:main efp_project.core.clj)
