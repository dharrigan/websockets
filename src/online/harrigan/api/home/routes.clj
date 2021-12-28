(ns online.harrigan.api.home.routes
  {:author "David Harrigan"}
  (:require
   [online.harrigan.components.thymeleaf.interface :as thymeleaf]))

(set! *warn-on-reflection* true)

(def ^:private ok 200)

(defn ^:private home
  [app-config]
  (fn [_]
    {:status ok :body (thymeleaf/render "home" app-config)}))

;; PUBLIC API

(defn routes
  [app-config]
  ["/"
   {:get {:handler (home app-config)}}])
