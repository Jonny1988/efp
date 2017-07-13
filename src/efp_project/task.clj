(ns efp-project.task)

(require 'efp-project.javaAnalyse)
(refer 'efp-project.javaAnalyse)
(use 'clojure.string)

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
  (fn [result name comment]
    (into result
      [{:type type
        :name name
       :description comment}])
   )
  )

(def addWarning (buildAddNotifcation "Warnung"))
(def addError (buildAddNotifcation "Error"))

(defn index-of-end
  [string search pos]
  (if-let [pos (index-of string search pos)]
    (+ (count search) pos)
  )
 )

(defn getFunctionInner
  [contexte search]
  (reduce 
   (fn [result context]
    (if-let [queueEndPos (index-of-end context search 0)]
     (conj result 
       {:inner (trim (subs context queueEndPos (index-of context ")" queueEndPos)))
        :context context})
     result
     )
    )
   []
   contexte
   )
 )

(defn getStringValue
  "either returns the variable direct or searches for the definition in global context"
  [analyse value]
  (if (= (get value 0) "\"")
    value
    (let [nonFunction (analyse :nonFunction) indexTo (index-of-end nonFunction (str value " = ") 0)]
       (subs nonFunction indexTo (index-of nonFunction ";" indexTo))
    )
	 )
)

(defn isQueueRightConfigurated
  [analyse]
  (def queue (getFunctionInner (reduce 
              (fn [result function] (conj result (function :string)))
              []
              (analyse :functions)) ".createConsumer("))
  (if (= (count queue) 1)
    (let [lookupDesti (getFunctionInner [((get queue 0) :context)] (str ((get queue 0) :inner) " = (Destination) ctx.lookup("))]
      (def desti ((get lookupDesti (- (count lookupDesti) 1)) :inner))
       (if (not (= (getStringValue analyse desti) "\"queue.files\""))
         "Queue wurde nicht mit \"queue.files\" geladen"
       )
    )
    "Mehrere Queue wurden gefunden"
  )
 )

(defn isThereATextMessage
  [analyse]
  (if (not (re-find #"TextMessage (.)*\.receive\(" (reduce 
                 (fn [result function] (str result (function :string)))
                 ""
                 (analyse :functions))))
    "Konnte nicht TextMessage finden")
 )

(defn task1Check
  [javastring]
  (let [analyse (analyse-java-file javastring)]
    (def result [])
    (if (not (= (analyse :class-name) "FileServer"))
      (def result (addWarning result "ClassNameError" "Klassen Namen muss \"FileServer\" heißen")))
    (if (not (re-find #"var" (analyse :package)))
      (def result (addWarning result "PackageNameError" "Klassen muss im Package \"var\" liegen")))
    (if-let [reason (isQueueRightConfigurated analyse)]
      (def result (addError result "PropertiesError" reason)))
    (if-let [reason (isThereATextMessage analyse)]
      (def result (addError result "TextMessageError" reason)))
    result
    )
  )
