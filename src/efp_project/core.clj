(ns efp-project.core
(:require [compojure.core :refer :all]
            [org.httpkit.server :refer [run-server]])) ; httpkit is a server

(defroutes myapp
  (POST "/" [] "Hello World"))

(defn -main []
  (run-server myapp {:port 5000}))
