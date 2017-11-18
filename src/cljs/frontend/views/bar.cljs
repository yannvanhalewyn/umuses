(ns frontend.views.bar
  (:require [frontend.models.chord :refer [parse]]
            [frontend.views.chord :refer [editable-chord displayed-chord]]
            [frontend.views.util.draggable :as draggable]
            [re-frame.core :refer [dispatch]]
            [frontend.views.editable :as editable]))

(def svg-ratio (/ 54 80))
(def height 30)
(def width (* height svg-ratio))

(defn component [{:keys [bar selected] :as props}]
  (let [symbols (group-by :attachment/type (:bar/attachments bar))]
    [:div.bar
     ;; Segno's
     ;; =======
     (for [{:keys [db/id coord/x coord/y]} (:symbol/segno symbols)]
       ^{:key id}
       [draggable/component
        {:class (str "music-symbol music-symbol--segno"
                  (when (= id selected) " selected"))
         :style {:width width :height height}
         :on-drag-end #(dispatch [:sheet/move-symbol (:db/id bar) id %])
         :on-click #(dispatch [:sheet/select id])
         :start-pos [x y]}])

     ;; Chords
     ;; ======
     (let [width (/ 100 (count (:bar/chords bar)))]
       (for [chord (sort-by :coll/position (:bar/chords bar))]
         ^{:key (:db/id chord)}
         [:div {:style {:display "inline-block"
                        :width (str width "%")
                        :margin-right "5px"}}
          (if (= selected (:db/id chord))
            [editable-chord {:chord chord}]
            [displayed-chord {:chord (parse (:chord/value chord))
                              :on-click #(dispatch [:sheet/select (:db/id chord)])}])]))

     ;; Texboxes
     ;; ========
     (for [{:keys [db/id coord/x coord/y textbox/value] :as texbox} (:attachment/textbox symbols)]
       ^{:key id}
       [draggable/component {:start-pos [x y]
                             :class "draggable--textbox"
                             :on-click #(dispatch [:sheet/select id])}
        [editable/component {:edit-trigger :on-double-click
                             :on-change #(dispatch [:sheet/edit-textbox (:db/id bar) id %])
                             :value value}
         [:span value]]])

     ;; Coda's
     ;; ======
     (for [{:keys [db/id coord/x coord/y]} (:symbol/coda symbols)]
       ^{:key id}
       [draggable/component {:style {:width height :height height}
                             :class (str "music-symbol music-symbol--coda"
                                      (when (= id selected) " selected") )
                             :mode :align-right
                             :on-click #(dispatch [:sheet/select id])
                             :on-drag-end #(dispatch [:sheet/move-symbol (:db/id bar) id %])
                             :start-pos [x y]}])]))
