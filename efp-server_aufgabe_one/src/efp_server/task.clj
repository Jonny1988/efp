(ns efp-server.task)

(require 'efp-server.javaAnalyse)
(refer 'efp-server.javaAnalyse)


(defn buildAddNotifcation [type]
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
  "like index-of but gives the position of the last character that was searched"
  [string search pos]
  (if-let [pos (clojure.string/index-of string search pos)]
    (+ (count search) pos)
  )
 )

(defn getFunctionInner
  "looks up for a specific search that should end with bracket inside an array of contexe, returns the the first hit result of every context if there is one"
  [contexte search]
  (reduce 
   (fn [result context]
    (if-let [queueEndPos (index-of-end context search 0)]
     (conj result 
       {:inner (clojure.string/trim (subs context queueEndPos (clojure.string/index-of context ")" queueEndPos)))
        :context context})
     ;inner defines the paramater or parameters the function uses
     ;context is simple there to refer where it was found
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
       (subs nonFunction indexTo (clojure.string/index-of nonFunction ";" indexTo))
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
