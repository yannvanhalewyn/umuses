(ns sheet-bucket.components.bar
  (:require [sheet-bucket.components.chord :refer [editable-chord displayed-chord]]))

(def style {:display "flex" :justify-content "space-between"})

(defn component [{:keys [chords selected on-chord-click on-chord-update]}]
  [:div
   {:style (merge style (when (> (count chords) 1) {:border-bottom "2px solid black"}))}
   (for [{:keys [id] :as chord} chords]
     (if (= selected id)

       ^{:key (str id "-edit")}
       [editable-chord {:text (:raw chord)
                        :on-blur (partial on-chord-update id)}]

       ^{:key id}
       [displayed-chord
        (assoc chord :on-click (partial on-chord-click id))]))])
