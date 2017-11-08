(ns frontend.views.bar
  (:require [frontend.models.chord :refer [parse]]
            [frontend.views.chord :refer [editable-chord displayed-chord]]))

(defn component [{:keys [bar selected on-chord-click] :as props}]
  [:div.bar
   (let [width (/ 100 (count (:bar/chords bar)))]
     (for [chord (:bar/chords bar)]
       ^{:key (:db/id chord)}
       [:div {:style {:display "inline-block" :width (str width "%")}}
        (if (= selected (:db/id chord))
          [editable-chord (assoc (select-keys props [:append :remove :move :deselect :update-chord])
                            :chord chord)]
          [displayed-chord {:chord (parse (:chord/value chord))
                            :on-click (partial on-chord-click (:db/id chord)) }])]))])