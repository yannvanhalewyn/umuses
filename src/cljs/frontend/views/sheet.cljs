(ns frontend.views.sheet
  (:require [frontend.views.section :as section]
            [frontend.views.editable :as editable]
            [frontend.views.sheet-tools :as sheet-tools]
            [frontend.util.util :as util :refer [prevent-default]]
            [frontend.keybindings :as bindings]
            [shared.utils :refer [presence]]
            [re-frame.core :refer [subscribe dispatch]]
            [goog.events :as events]
            [goog.events.EventType :refer [KEYDOWN]]
            [reagent.core :as reagent]))

(defn on-key-down [e]
  (when-let [rf-event (bindings/global-context (util/event->keychord e))]
    (.preventDefault e)
    (dispatch rf-event)))

(defn component [{:keys [sheet-id] :as props}]
  (reagent/create-class
    {:component-will-mount
     #(set! (.-keyboardShortcutsListener js/document)
        (events/listen js/document KEYDOWN on-key-down))
     :component-will-unmount
     #(events/unlistenByKey (.-keyboardShortcutsListener js/document))
     :reagent-render
     (fn []
       (let [sheet @(subscribe [:sub/sheet sheet-id])
             selection @(subscribe [:sub/selection])]
         [:div.u-max-height.sheet.l-content {:on-click #(dispatch [:sheet/deselect])}
          (let [title (or (presence (:sheet/title sheet)) "(title)")
                artist (or (presence (:sheet/artist sheet)) "(artist)")]
            [:div
             [editable/component {:on-change #(dispatch [:sheet/set-title (:db/id sheet) %])
                                  :value title}
              [:h1.sheet__title title]]
             [editable/component {:on-change #(dispatch [:sheet/set-artist (:db/id sheet) %])
                                  :value artist}
              [:h3.sheet__artist.u-margin-top--s artist]]])
          [:div.u-margin-top.sheet-body
           (for [section (sort-by :coll/position (:sheet/sections sheet))]
             ^{:key (:db/id section)}
             [section/component {:section section :selection selection}])]
          [sheet-tools/component {:selection selection}]]))}))
