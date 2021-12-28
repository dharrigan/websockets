(ns online.harrigan.components.database.interface
  {:author "David Harrigan"}
  (:require
   [online.harrigan.components.database.impl :as db]))

(set! *warn-on-reflection* true)

;;
;; Alphabetical order please!
;;

(defn execute
  ([sql datasource]
   (execute sql datasource {}))
  ([sql datasource opts]
   (db/execute sql datasource opts)))

(defn select
  ([sql datasource]
   (select sql datasource {}))
  ([sql datasource opts]
   (db/select sql datasource opts)))

(defn select-many
  ([sql datasource]
   (select-many sql datasource {}))
  ([sql datasource opts]
   (db/select-many sql datasource opts)))
