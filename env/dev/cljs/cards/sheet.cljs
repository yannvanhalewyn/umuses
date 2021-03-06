(ns cards.sheet
  (:require [frontend.views.sheet :as subject]
            [cards.util :refer [gen]]
            [shared.specs :as specs]
            [re-frame.core :refer [reg-sub reg-event-db]])
  (:require-macros [devcards.core :refer [defcard-rg defcard-doc]]))

(defonce sheet (first (gen ::specs/sheet 1)))
(def selected
  {:selection/type :selection/chord
   :selection/id (-> sheet :sheet/sections first :section/rows first
                   :row/bars first :bar/chords second :db/id)})

(reg-sub :sub/sheet (constantly sheet))
(reg-sub :sub/selection (constantly selected))

(def props
  {:sheet sheet
   :on-chord-click js/alert
   :on-chord-update (.-log js/console)
   :selected selected})

(defcard-doc
  "# Sheet"
  "## Example sheet data"
  "The sheet component subscribes in re-frame to the `:subs/sheet`
  subscription. Here is some generated sample data that could be
  returned:"
  sheet)

(defcard-rg base
  "The second chord of the first bar of the first row of the first
  section should be editable if present."
  [subject/component props])
