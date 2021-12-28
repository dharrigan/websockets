(ns online.harrigan.api.websocket.routes
  {:author "David Harrigan"}
  (:require
   [clojure.tools.logging :as log]
   [ring.adapter.jetty9 :as jetty]
   [taoensso.sente :as sente]
   [taoensso.sente.packers.transit :as sente-transit]
   [taoensso.sente.server-adapters.jetty9 :refer [get-sch-adapter]]))

(set! *warn-on-reflection* true)

(def ^:private super-secure-csrf-token "abcd1234") ;; same as in the Javascript client (index.js)

(defn fake-session
  [request]
  (assoc-in request [:session :csrf-token] super-secure-csrf-token)) ;; fake it until you make it!

(defn upgrade?
  [ajax-get-or-ws-handshake-fn]
  (fn [request]
    (when (jetty/ws-upgrade-request? request)
      (let [enriched-request (fake-session request)
            {:keys [status] :as ws-handler} (ajax-get-or-ws-handshake-fn enriched-request)]
        (if (contains? #{401 403} status)
          ws-handler ;; it's actually a response at this point!
          (jetty/ws-upgrade-response ws-handler))))))

(defn ^:private post
  [ajax-post-fn]
  (fn [request]
    (let [enriched-request (fake-session request)]
      (ajax-post-fn enriched-request)))) ;; fake it until you make it!

(defmulti event-handler :id)

(defmethod event-handler :foo/bar
  [{:keys [uid send-fn ?data] :as event}]
  (let [{:keys [message]} ?data]
    (log/infof "I received message '%s' from client '%s'." message uid)
    (send-fn uid [:foo/bar {:message (format "I received your message '%s'. It was '%s'." uid message)}])))

(defmethod event-handler :default
  [{:keys [id] :as event}]
  (when (= :chsk/bad-event id)
    (log/info event)))

(defn ^:private websocket-handler
  [_]
  (fn [event]
    (event-handler event)))

(def ^:private user-id-fn
  (fn [request]
    (or (-> request :session :uid)
        (:client-id request))))

(def ^:private socket-options
  {:user-id-fn user-id-fn
   :packer (sente-transit/get-transit-packer)})

;; PUBLIC API

(defn routes
  [app-config]
  (let [channel-socket-server (sente/make-channel-socket-server! (get-sch-adapter) socket-options)
        {:keys [ajax-get-or-ws-handshake-fn ch-recv ajax-post-fn]} channel-socket-server]
    (sente/start-server-chsk-router! ch-recv (websocket-handler app-config))
    ["/ws" {:get {:handler (upgrade? ajax-get-or-ws-handshake-fn)
                  :parameters {:query [:map [:client-id string?]]}}
            :post {:handler (post ajax-post-fn)}}]))
