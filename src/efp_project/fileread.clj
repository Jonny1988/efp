(ns efp-project.fileread)

(def comment-regex #"((?:\/\*(?:[^*]|(?:\*+[^*\/]))*\*+\/)|(?:\/\/.*))")
(def import-regex #"(?<=import )(?:(?!;).)*")
(def function-regex #"(public|private) (\w+ )*(\w+)\(.*\).*\{")
;(def brackets-regex #"\{((?>[^{}]+)|(?R))*\}")
(def example-java (str (slurp "C:\\Users\\Kroemel\\Documents\\GitHub\\efp\\temp\\FileServer.java")))

(defn remove-comments
  "Removes every commentar in java code"
  [java-as-string]
  (clojure.string/replace java-as-string comment-regex ""))
  
(defn get-all-matches
  [javastring regex]
  (loop [matcher (re-matcher regex javastring) imports []]
    (if-let [match (re-find matcher)]
      (recur matcher (conj imports match))
      imports)))

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
       {:test (clojure.string/index-of split-from-class (get (re-find function-regex split-from-class) 0))}
     )
    )
	 )
  )
 
(defn analyse-java-file
  "Analyses the java file according to the first Task"
  [fileasstring]
  (split-java-file (remove-comments fileasstring)))

(analyse-java-file "")