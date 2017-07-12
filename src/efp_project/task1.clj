(ns efp-project.task1)

(require 'efp-project.javaAnalyse)
(refer 'efp-project.javaAnalyse)

;; Klassenname FileServer -> ClassNameError
;; package var.mom.jms.file -> packageNameError
;; jndi.properties queue.var.mom.jms.file.requestqueue -> PropertiesError
;; JMS TextMessage -> TextMessageError

;; case aufgabe 2
;; Klassenname ChatClient
;; package var.rmi.chat
;; Conf.CHATSERVICE
;; Benutzername args[0]
;Replacing Template Method
(defn buildAddNotifcation [type]
  "shshsh"
  (fn [name comment] 
    {:type type
     :name name
     :description comment
    }
   )
  )

(def addWarning (buildAddNotifcation "Warnung"))
(def addFailure (buildAddNotifcation "Error"))

; = new InitialContext();
(defn getVariableName
  [string searchCrit]
  (let [start (clojure.string/last-index-of string " = " (clojure.string/index-of string searchCrit))]
    (subs string (clojure.string/last-index-of string " " start) start)
  )
 )
(defn check
  [javastring]
  (let [analyse (analyse-java-file javastring)]
    [(if (not (= (analyse :class-name) "FileServer2"))
       (addWarning "ClassNameError" "Klassen Namen muss FileServer heißen"))
     (if (not (re-find #"var" (analyse :package)))
       (addWarning "PackageNameError" "Klassenmuss im Package var liegen"))
    ]
    ;(print (getVariableName (analyse :nonFunction) "var.mom.jms.file.requestqueue"))
    )
  )