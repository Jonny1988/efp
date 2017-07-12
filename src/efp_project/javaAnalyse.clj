(ns efp-project.javaAnalyse)

(def comment-regex #"((?:\/\*(?:[^*]|(?:\*+[^*\/]))*\*+\/)|(?:\/\/.*))")
(def import-regex #"(?<=import )(?:(?!;).)*")
(def function-regex #"(public|private) (\w+ )*(\w+)\(.*\).*\{")
(def example-java (str (slurp "C:\\Users\\Kroemel\\Documents\\GitHub\\efp\\temp\\FileServer.java")))

(defn remove-comments
  "Removes every comment from java code"
  [java-as-string]
  (clojure.string/replace java-as-string comment-regex ""))
  
(defn get-all-matches
  [javastring regex]
  (loop [matcher (re-matcher regex javastring) matches []]
    (if-let [match (re-find matcher)]
      (recur matcher (conj matches match))
      matches)))

(defn get-every-function-position
  [javastring]
  (map (fn [function-name]
         (let [start (clojure.string/index-of javastring function-name) from-functionstart (subs javastring start)]
           {:start start
            :end (loop [string from-functionstart count -1 offset 0]
                   (let [open (clojure.string/index-of string "{") closed (clojure.string/index-of string "}")]
                     (if (and open (< open closed))
                       (recur (subs string (inc open)) (inc count) (+ open 1 offset))
                       (if (== count 0)
                         (+ closed offset start 1)
                         (recur (subs string (inc closed)) (dec count) (+ closed 1 offset))
                         )
                       )
                     )
                   )
            }
           )
         )
    (loop [matcher (re-matcher function-regex javastring) matches []]
      (if-let [match (get (re-find matcher) 0)]
        (recur matcher (conj matches match))
        matches))
    )
 )

(defn function-analyse
  [javastring]
  (reduce 
    (fn [function-result function-position]
      (assoc function-result 
        :functions (conj (function-result :functions) (subs javastring (function-position :start) (function-position :end)))
        :nonFunction (str (function-result :nonFunction) (subs javastring (function-result :lastFunctionEnd) (function-position :start)))
        :lastFunctionEnd (function-position :end)
      )
     )
    {:functions [] :nonFunction "" :lastFunctionEnd (clojure.string/index-of javastring "{")}
    (get-every-function-position javastring)
   )
  )

(defn split-java-file
  [javastring]
  (def class-pos (clojure.string/index-of javastring "public class"))
  (into  
	  (let [split-to-class (subs javastring 0 class-pos)]
      {:imports (get-all-matches split-to-class import-regex)
       :package (re-find #"(?<=package )(?:(?!;).)*" split-to-class) }
	   )
	  (let [split-from-class (subs javastring class-pos) split-class-line (subs split-from-class 0 (clojure.string/index-of split-from-class "{"))]
	   (into
       (reduce
	        (fn [new-map [key val]]
	          (assoc new-map key (re-find (re-pattern (str "(?<=" val ")\\w+")) split-class-line)))
	        {}
	        {:class-name "public class " :extends "extends " :implements "implements "})
       (function-analyse split-from-class)
     )
    )
	 )
  )
 
(defn analyse-java-file
  "Analyses the java file"
  [fileasstring]
  (split-java-file (remove-comments example-java)))