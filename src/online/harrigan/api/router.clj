(ns online.harrigan.api.router
  {:author "David Harrigan"}
  (:require
   [muuntaja.core :as m]
   [online.harrigan.api.general.favicon :as favicon-api]
   [online.harrigan.api.general.health :as health-api]
   [online.harrigan.api.home.routes :as home-api]
   [online.harrigan.api.websocket.routes :as websocket-api]
   [reitit.coercion.malli :as rcm]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.spec :as rs]
   [ring.adapter.jetty9 :as jetty]
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.params :refer [wrap-params]])
  (:import
   [org.eclipse.jetty.server Server]))

(set! *warn-on-reflection* true)

(def ^:private cors-middleware
  [wrap-cors
   :access-control-allow-origin [#".*"]
   :access-control-allow-methods [:get :post]])

(defn ^:private router
  [app-config]
  (ring/router
   [(merge ["/api"])
           ;; put other routes here that live under the '/api' endpoint
    (home-api/routes app-config)
    (websocket-api/routes app-config)
    health-api/routes
    favicon-api/routes]
   {:validate rs/validate
    :exception pretty/exception
    :data {:coercion rcm/coercion
           :muuntaja m/instance
           :middleware [cors-middleware
                        wrap-params ;; for websockets (sente looks for the :param key in the request map)
                        wrap-keyword-params ;; for websockets (sente looks for the :param key in the request map)
                        muuntaja/format-middleware
                        parameters/parameters-middleware
                        coercion/coerce-exceptions-middleware
                        coercion/coerce-request-middleware
                        coercion/coerce-response-middleware]}}))

;; CLIP Lifecycle Functions

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start
  [{:keys [runtime-config] :as app-config}]
  (jetty/run-jetty
   (ring/ring-handler (router app-config) (ring/create-default-handler))
   (merge (:jetty runtime-config) {:send-server-version? false
                                   :send-date-header? false
                                   :allow-null-path-info true
                                   :join? false}))) ;; false so that we can stop it at the repl!

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop
  [^Server server]
  (.stop server) ; stop is async
  (.join server)) ; so let's make sure it's really stopped!
