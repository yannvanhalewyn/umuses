(ns umuses.socket-handler
  (:require [clojure.stacktrace :as st]
            [datomic.api :as d]
            [shared.datsync :as datsync]
            [taoensso.timbre :as timbre]))

(defn- translate-tx-form
  "Takes a transaction and conforms any negative numbers for entity or
  values as string tempids"
  [[op e a v added]]
  (let [conform-id #(if (and (number? %) (neg? %)) (str %) %)]
    [op (conform-id e) a (conform-id v) added]))

(defmulti socket-handler "Multimethod for handling socket messages" :id)

(defmethod socket-handler :default [{:keys [event ?reply-fn]}]
  (when-not (= :chsk/ws-ping event)
    (timbre/debug "Unhandled event" event))
  (when-let [reply ?reply-fn]
    (reply {:unmatched-event-echo event})))

(defmethod socket-handler :tx/sync
  [{:keys [?data ring-req ?reply-fn]}]
  (let [report @(d/transact (:db-conn ring-req) ?data)]
    {:tempids (:tempids report)
     :tx-data (datsync/report->datom-vecs report #{:attachment/type})}))

(defn- wrap-stacktrace
  "Wrap a handler such that exceptions are caught and a helpful debugging
   response is returned."
  [handler]
  (fn [msg]
    (try
      (handler msg)
      (catch Exception e
        (let [res {:error (with-out-str (st/print-stack-trace e))}
              reply (or (:?reply-fn msg) #'timbre/error)]
          (reply res))))))

(defn- wrap-reply
  "Wraps a handler and calls the reply fn if any"
  [handler]
  (fn [msg]
    (if-let [reply (:?reply-fn msg)]
      (reply (handler msg))
      (handler msg))))

(def handler (-> socket-handler wrap-reply wrap-stacktrace))
