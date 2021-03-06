(ns frontend.models.sheet-test
  (:require [frontend.models.sheet :as sut]
            [cljs.test :as t :refer-macros [deftest testing is are]]
            [datascript.core :as d]))

(def BLANK_SHEET
  {:db/id 1
   :sheet/title "Title"
   :sheet/artist "Artist"
   :sheet/sections {:db/id 2
                    :section/title "Intro"
                    :coll/position 0
                    :section/rows {:db/id 3
                                   :coll/position 0
                                   :row/bars {:db/id 4
                                              :coll/position 0
                                              :bar/chords {:db/id 5
                                                           :coll/position 0
                                                           :chord/value ""}}}}})

(def db (let [conn (d/create-conn sut/schema)]
          (d/transact! conn [BLANK_SHEET])
          @conn))

(defn- tx-apply [db tx-fn & args]
  (:db-after (d/with db (binding [sut/*string-tmp-ids* false]
                          (apply tx-fn db args)))))

(deftest update-chord
  (is (= "Ab" (:chord/value (d/entity (tx-apply db sut/update-chord 5 "Ab") 5)))))

(deftest append
  (testing "Append chords at the end"
    (let [db (tx-apply db sut/append :chord 5)]
      (is (= [{:chord/value "" :coll/position 0}
              {:chord/value "" :coll/position 1}]
            (map #(into {} %) (:bar/chords (d/entity db 4)))))))

  (testing "Append chord in between"
    (let [db (-> db
               (tx-apply sut/update-chord 5 "first")
               (tx-apply sut/append :chord 5)
               (tx-apply sut/update-chord 6 "last")
               (tx-apply sut/append :chord 5)
               (tx-apply sut/update-chord 7 "middle"))]
      (is (= [{:chord/value "first" :coll/position 0}
              {:chord/value "last" :coll/position 2}
              {:chord/value "middle" :coll/position 1}]
            (map #(into {} %) (:bar/chords (d/entity db 4)))))))

  (testing "Append bar"
    (is (= [[:db/add 3 :row/bars "new-bar"]
            {:db/id "new-bar" :coll/position 1 :bar/chords "new-chord"}
            {:db/id "new-chord" :chord/value "" :coll/position 0}]
          (sut/append db :bar 5))))

  (testing "Append bar in between"
    (let [db (-> db
               (tx-apply sut/append :bar 5)
               (tx-apply sut/append :bar 5))
          bars (:row/bars (d/entity db 3))]
      (is (= 3 (count bars)))
      (is (= [0 2 1] (map :coll/position bars)))))

  (testing "Append row"
    (let [db (-> db
               (tx-apply sut/append :row 5)
               (tx-apply sut/append :row 5))
          rows (:section/rows (d/entity db 2))]
      (is (= 3 (count rows)))
      (is (= [0 2 1] (map :coll/position (sort-by :db/id rows))))))

  (testing "Append section"
    (let [db (-> db
               (tx-apply sut/append :section 5)
               (tx-apply sut/append :section 5))
          sections (:sheet/sections (d/entity db 1))]
      (is (= 3 (count sections)))
      (is (= [0 2 1] (map :coll/position (sort-by :db/id sections))))
      (is (= ["Intro" "Section" "Section"] (map :section/title sections))))))

(deftest delete
  (let [db (-> db
             (tx-apply sut/append :chord 5)
             (tx-apply sut/append :row 5)
             (tx-apply sut/append :section 5))]

    (testing "remove chord"
      (let [db (tx-apply db sut/remove* :chord 5)
            chords (:bar/chords (d/entity db 4))]
        (is (= 1 (count chords)))
        (is (= 6 (:db/id (first chords))))))

    (testing "remove last chord in row"
      (let [db (tx-apply db sut/remove* :chord 9)
            rows (:section/rows (d/entity db 2))]
        (is (= 1 (count rows)))
        (is (= 3 (:db/id (first rows))))))

    (testing "remove last chord in section"
      (let [db (tx-apply db sut/remove* :chord 13)
            sections (:sheet/sections (d/entity db 1))]
        (is (= 1 (count sections)))
        (is (= 2 (:db/id (first sections))))))))
