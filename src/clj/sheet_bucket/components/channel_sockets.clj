(ns sheet-bucket.components.channel-sockets
  (:require [com.stuartsierra.component :as c]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]))

(defn- event-handler [{:as msg :keys [connected-uids uid send-fn event ring-req client-id]}]
  (println event uid))

(defn broadcast [chsk msg]
  (doseq [uid (:any @(:connected-uids chsk))]
    ((:send-fn chsk) uid [:broadcast/msg msg])))

(def SENTE_KEYS [:ajax-get-or-ws-handshake-fn
                 :ajax-post-fn :ch-recv :send-fn])

(defrecord ChannelSockets []
  c/Lifecycle
  (start [this]
    (println "Starting Channel Sockets listener...")
    (let [chsk (sente/make-channel-socket! (get-sch-adapter) {})
          stop-fn (sente/start-chsk-router! (:ch-recv chsk) #'event-handler)]
      (assoc (select-keys chsk SENTE_KEYS)
        :stop-fn stop-fn)))
  (stop [this]
    (println "Stopping Channel Sockets listener...")
    (if-let [stop (:stop-fn this)]
      (stop)
      (println "No stop-fn found for Channel Sockets listener. Doing nothing."))
    (apply dissoc this SENTE_KEYS :stop-fn)))

(defn component [] (ChannelSockets.))
