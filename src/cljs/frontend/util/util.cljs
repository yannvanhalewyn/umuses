(ns frontend.util.util
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test.check.generators]))

(defn gen
  "Will generate any amount of data based on a spec"
  [spec count]
  (gen/sample (s/gen spec) count))

(defn prevent-default
  "Returns a function to be used as an browser event handler. That
  function calls .preventDefault on the event. The return value of the
  fn is always nil. If f and args is supplied, calls f with args."
  [f & args]
  (fn [event]
    (.preventDefault event)
    (apply f args)
    nil))

(defn stop-propagation
  "Returns a function to be used as an browser event handler. That
  function calls .stopPropagation on the event. The return value of the
  fn is always nil. If f and args is supplied, calls f with args."
  [f & args]
  (fn [event]
    (.stopPropagation event)
    (apply f args)
    nil))

(defn event->keychord
  "Takes a keydown event, and returns a vector of all important keys like META, SHIFT, CTRL, ALT,.."
  [e]
  (let [code (.-which e)
        shift (if (.-shiftKey e) :shift)
        alt (if (.-altKey e) :alt)
        meta (if (.-metaKey e) :meta)]
    (filter identity [alt meta shift code])))

(def presence #(if (empty? %) nil %))

(defn create-selector
  [selectors f]
  (let [cache (atom [])]
    (fn [state]
      (let [args (map #(% state) selectors)
            [cached-args cached-result] @cache]
        (if (= args cached-args)
          cached-result
          (let [result (apply f args)]
            (reset! cache [args result])
            result))))))
