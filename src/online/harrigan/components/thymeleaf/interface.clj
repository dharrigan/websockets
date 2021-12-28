(ns online.harrigan.components.thymeleaf.interface
  {:author "David Harrigan"}
  (:require
   [online.harrigan.components.thymeleaf.impl :as thymeleaf]))

(set! *warn-on-reflection* true)

(defn render
  ([viewname app-config]
   (render viewname nil app-config))
  ([viewname data app-config]
   (thymeleaf/render viewname data app-config)))
