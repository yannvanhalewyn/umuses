(ns sheet-bucket.containers
  (:require [sheet-bucket.components.sheet :as sheet]
            [sheet-bucket.selectors :refer [sections attributes selected]]
            [sheet-bucket.actions :refer [select-chord update-chord append deselect]]
            [redux.utils :refer [create-container]]))

(def app
  (create-container
   :component sheet/component
   :selectors {:sections sections
               :attrs attributes
               :selected selected}
   :actions {:update-chord update-chord
             :on-chord-click select-chord
             :deselect deselect
             :append append
             :move #(.log js/console "Move: " %2)
             :remove #(.log js/console "Remove " %2)}))
