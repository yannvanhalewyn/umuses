(ns frontend.models.chord
  (:require [shared.specs :as specs]
            [clojure.core.match :refer-macros [match]]
            [clojure.spec.alpha :as s]
            [goog.string :refer [format contains caseInsensitiveContains]]
            [goog.string.format]
            [clojure.string :as str]))

;; Negative lookahead for ending accidental that are part of "b5" or "b9"
;; like Eb5 -> root = E
(def root-regx (str "((([#b])?([0-7]))|(([a-gA-G])([#b])?))?(?!5)"))
;; Negative lookahead for 'm' that is not part of 'maj'
(def triad-regx (str "min|m(?!aj)|-|aug|\\+|#5|b5"))
(def extension-regx (str "(7|maj|Maj)?7?([#b]?9)?([#b]5)?"))
(def chord-regex (re-pattern (format "%s(%s)?(%s)?"
                               root-regx triad-regx extension-regx)))

(defn parse
  "Parses a raw chord string to chord data"
  [s]
  (let [result (re-find chord-regex s)
        [nashville-acc nashville-root _ root acc
         triad extension seventh ninth fifth] (drop 3 result)]
    {:chord/root (cond
                   nashville-root
                   [nashville-root (case nashville-acc "#" :sharp "b" :flat :natural)]
                   root
                   [root (case acc "#" :sharp "b" :flat :natural)])
     :chord/triad (or (case fifth "b5" :diminished "#5" :augmented nil)
                    (match triad
                      (:or "m" "min" "-") :minor
                      (:or "aug" "+" "#5") :augmented
                      "b5" :diminished
                      :else :major))
     :chord/seventh (let [e (str/lower-case (or extension ""))]
                      (cond
                        (contains e "maj") :major
                        (contains e "7") :minor
                        :else nil))
     :chord/ninth (match ninth
                    "9" :natural
                    "b9" :flat
                    "#9" :sharp
                    :else nil)}))

(s/fdef parse
  :args (s/cat :input :chord/value)
  :ret :chord/parsed
  :fn (fn [res] (= (specs/unparse-chord (:ret res)) (-> res :args :input))))
