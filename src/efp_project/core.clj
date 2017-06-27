(ns efp-project.core
(:require [compojure.core :refer :all]
            [org.httpkit.server :refer [run-server]])) ; httpkit is a server
(require 'efp-project.fileread)
(refer 'efp-project.fileread)

(defroutes myapp
  (GET "/" [] "sss")
  (POST "/" [] "HELLO POST"))

(defn -main []
  (run-server myapp {:port 5000})) 
