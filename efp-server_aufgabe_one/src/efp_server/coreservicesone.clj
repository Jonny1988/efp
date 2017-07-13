(ns efp-server.coreservicesone
	(:require [clojure.data.json :as json]
	[ring.util.request :as req]
	[efp-server.task :as task])
	(:use 	[ring.adapter.jetty]
			[ring.middleware.json :only [wrap-json-body]]))

(defn validate_file
	"choose multhimethod by regex from filename"
	[file_content]
	(json/write-str (task/task1Check file_content)))

(defn handler [request]
  {:status 200
   :headers {	"Access-Control-Allow-Origin"  "*"
				"Access-Control-Allow-Methods" "POST"
				"Access-Control-Allow-Headers" "Content-Type"
				"Content-Type" "text/plain"}
   	:request-method :post	
	:body (validate_file (req/body-string request))})

(defn -main
	"Main for starting the server"
	[& args]
	(run-jetty handler{:port 5000 :ssl? true :join? false :ssl-port 8443 :key-password "qwertz" } ))