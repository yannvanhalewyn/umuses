(ns sheet-bucket.models.chord
  (:require [cljs.spec :as s]
            [cljs.core.match :refer-macros [match]]
            [clojure.test.check.generators]
            [goog.string :refer [format]]
            [goog.string.format]
            [cljs.spec.impl.gen :as gen]))

(defn rand-str [n]
  (clojure.string/join
   (take n (repeatedly #(.toString (rand-int 16) 16)))))

(def gen-id #(->> (s/gen string?) (gen/fmap (partial rand-str 5))))

(def min-maj? #{:minor :major})

(s/def ::id (s/spec (s/and string? #(= 5 (count %)))
                    :gen gen-id))
(s/def ::root #{:a :b :c :d :e :f :g})
(s/def ::triad min-maj?)
(s/def ::seventh min-maj?)
(s/def ::extension #{:nineth :thirteenth})
(s/def ::chord (s/keys :req-un [::id]
                       :opt-un [::root ::triad ::seventh ::extension]))

(s/def ::bar (s/coll-of ::chord :max-count 4 :min-count 1))
(s/def ::row (s/coll-of ::bar :max-count 6 :min-count 1))

(def gen #(gen/sample (s/gen ::chord) %))

(def gen-row #(gen/sample (s/gen ::row) %))

(def root-regx (str "([#b])?([a-gA-G1-7])([#b])?"))
(def triad-regx (str "m(?!aj)|-|aug|\\+") ) ;; Negative lookahead for 'm' that is not part of 'maj'
(def seventh-regx (str "7|maj"))
(def chord-regex (re-pattern (format "%s(%s)?(%s)?"
                                     root-regx triad-regx seventh-regx)))

(defn parse
  "Parses a raw chord string to chord data"
  [s]
  (let [result (rest (re-find chord-regex s))
        [_ root _ triad seventh] result]
    {:root (match (vec (take 3 result))
             ["b" root _] [root :flat]
             ["#" root _] [root :sharp]
             [_ root "b"] [root :flat]
             [_ root "#"] [root :sharp]
             :else [root])
     :triad (match triad
              (:or "m" "-") :minor
              (:or "aug" "+") :augmented
              :else :major)
     :seventh (match seventh
                "7" :minor
                "maj" :major
                :else nil)}))
