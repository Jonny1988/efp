(ns efp-project.javaAnalyse)

(def comment-regex #"((?:\/\*(?:[^*]|(?:\*+[^*\/]))*\*+\/)|(?:\/\/.*))")
(def import-regex #"(?<=import )(?:(?!;).)*")
(def function-regex #"(public|private) (\w+ )*(\w+)\(.*\).*\{")

(defn remove-comments
  "removes evey line of comments since thoose can really mess with the analyse"
  [java-as-string]
  (clojure.string/replace java-as-string comment-regex ""))
  
(defn get-all-matches
  "simple gets every match of an regex"
  [javastring regex]
  (loop [matcher (re-matcher regex javastring) matches []]
    (if-let [match (re-find matcher)]
      (recur matcher (conj matches match))
      matches)))

(defn get-every-function-position
  "returns and array of start and end positions of functions"
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
           ;2. then use the regex to search for this position, since there can only be one function with the same name and paramaters
           ;go into the function and count vor every bracket open +1 and for every bracket close -1
           ;on reaching a bracket close with a count of 0 the function end is reached -> stop 
           )
         )
    (loop [matcher (re-matcher function-regex javastring) matches []]
      (if-let [match (get (re-find matcher) 0)]
        (recur matcher (conj matches match))
        matches))
    ;1. first get every function match with a regex
    )
 )

(defn function-analyse
  "splits the java string into seperated function strings as well as an non Function part where the class variables could be"
  [javastring]
  (reduce 
    (fn [function-result function-position]
      (assoc function-result 
        :functions (conj (function-result :functions) {:name (re-find #"(?<= )(?:(?=\w+\().)+" (subs javastring (function-position :start) (function-position :end)))
                                                       :string (subs javastring (function-position :start) (function-position :end))})
        :nonFunction (str (function-result :nonFunction) (subs javastring (function-result :lastFunctionEnd) (function-position :start)))
        :lastFunctionEnd (function-position :end)
        ;read the functions string based on the positions as new entry, fills the nonFunction String with the parts inbetween functions
      )
     )
    {:functions [] :nonFunction "" :lastFunctionEnd (clojure.string/index-of javastring "{")}
    (get-every-function-position javastring)
   )
  )

(defn split-java-file
  [javastring]
  (def class-pos (clojure.string/index-of javastring "public class")) 
  ;search for the class position
  (into  
	  (let [split-to-class (subs javastring 0 class-pos)]
     ;split-to-class is everything to the class position
      {:imports (get-all-matches split-to-class import-regex)
       :package (re-find #"(?<=package )(?:(?!;).)*" split-to-class) }
	   )
     ;read the imports and package
	  (let [split-from-class (subs javastring class-pos) split-class-line (subs split-from-class 0 (clojure.string/index-of split-from-class "{"))]
     ;split-from-class is everything from the class position
     ;split-class-line is the one line with the class definition
	   (into
       (reduce
	        (fn [new-map [key val]]
	          (assoc new-map key (re-find (re-pattern (str "(?<=" val ")\\w+")) split-class-line)))
	        {}
	        {:class-name "public class " :extends "extends " :implements "implements "})
          ;reads the class name, exetendtions and implements
       (function-analyse split-from-class)
     )
    )
	 )
  )
 
(defn analyse-java-file
  "Analyses the java file, splits the File into different parts like the functions, imports and so on"
  [fileasstring]
  (split-java-file (remove-comments fileasstring)))