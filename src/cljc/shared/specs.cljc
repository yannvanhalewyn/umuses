(ns shared.specs
  (:require [shared.utils :refer [gen-temp-id]]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators]
            [clojure.spec.gen.alpha :as gen]))

;; Generators
;; ==========
(defn unparse-chord
  "Takes a parsed chord and returns a possible raw input value for that chord."
  [{:keys [:chord/root :chord/triad :chord/seventh :chord/ninth]}]
  (str
    (first root)
    (case (second root) :sharp "#" :flat "b" "")
    (case triad :minor "-" :augmented "+" :diminished "b5" "")
    (case seventh :major "Maj7" :minor "7" "")
    (case ninth :natural "9" :flat "b9" :sharp "#9" "")))

(defn gen-id []
  (gen/fmap (fn [_] (gen-temp-id)) (gen/any)))

(defn gen-chord-value []
  (gen/fmap unparse-chord (s/gen :chord/parsed)))

;; Chord
;; =====
(def root? #{"A" "B" "C" "D" "E" "F" "G" "1" "2" "3" "4" "5" "6" "7"})
(def accidental? #{:flat :sharp :natural})
(def extension? #{9 13})

(s/def :db/id (s/spec (s/or :datomic pos-int? :tmp-id string?) :gen gen-id))
(s/def :coll/position int?)
(s/def :chord/value (s/spec string? :gen gen-chord-value))
(s/def ::chord (s/keys :req [:db/id :coll/position :chord/value]))

;; Parsed chord
(s/def :chord/root (s/tuple root? accidental?))
(s/def :chord/triad #{:minor :major :augmented :diminished})
(s/def :chord/seventh #{:minor :major})
(s/def :chord/ninth accidental?)
(s/def :chord/parsed (s/keys :req [:chord/root :chord/triad :chord/seventh :chord/ninth]))

;; Sheet
;; =====

(s/def :bar/chords (s/coll-of ::chord :min-count 1 :gen-max 4))
(s/def :bar/start-repeat boolean?)
(s/def :bar/end-repeat boolean?)
(s/def ::bar (s/keys :req [:db/id :coll/position :bar/chords :bar/start-repeat :bar/end-repeat]))

(s/def :row/bars (s/coll-of ::bar :min-count 1 :gen-max 3))
(s/def ::row (s/keys :req [:db/id :coll/position :row/bars]))

(s/def :section/title (s/spec string? :gen #(s/gen #{"Intro" "Verse" "Chorus"})))
(s/def :section/rows (s/coll-of ::row :min-count 1 :gen-max 3))
(s/def ::section (s/keys :req [:db/id :coll/position :section/title :section/rows]))

(s/def :sheet/title (s/spec string? :gen #(s/gen #{"Whole lotta love" "Breathe" "Lean on me"})))
(s/def :sheet/artist (s/spec string? :gen #(s/gen #{"Led Zeppelin" "Pink Floyd" "Bill Withers"})))
(s/def :sheet/sections (s/coll-of ::section :min-count 1 :gen-max 3))
(s/def ::sheet (s/keys :req [:db/id :sheet/title :sheet/artist :sheet/sections]))