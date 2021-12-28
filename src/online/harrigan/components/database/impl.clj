(ns online.harrigan.components.database.impl
  {:author "David Harrigan"}
  (:require
   [clojure.tools.logging :as log]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.result-set :refer [as-kebab-maps]]
   [online.harrigan.components.database.migration :as migration])
  (:import
   [com.zaxxer.hikari HikariDataSource]))

(set! *warn-on-reflection* true)

(def ^:private additional-config {:maxLifetime (* 10 60 1000)})

(defn execute
  ([sql datasource] (execute sql datasource {}))
  ([sql datasource opts]
   (log/tracef "Executing JDBC '%s'." sql)
   (try
    (when-let [results (jdbc/execute-one! datasource sql (merge {:builder-fn as-kebab-maps} opts))]
      (log/tracef "JDBC Results '%s'." results)
      results)
    (catch Exception e
      (log/error e)
      (throw e)))))

(defn select
  ([sql datasource] (select sql datasource {}))
  ([sql datasource opts]
   (log/tracef "Executing JDBC '%s'." sql)
   (try
    (when-let [results (jdbc/execute-one! datasource sql (merge {:builder-fn as-kebab-maps} opts))]
      (log/tracef "JDBC Result '%s'." results)
      results)
    (catch Exception e
      (log/error e)
      (throw e)))))

(defn select-many
  ([sql datasource] (select-many sql datasource {}))
  ([sql datasource opts]
   (log/tracef "Executing JDBC '%s'." sql)
   (try
    (when-let [results (jdbc/execute! datasource sql (merge {:builder-fn as-kebab-maps} opts))]
      (log/tracef "JDBC Result '%s'." results)
      results)
    (catch Exception e
      (log/error e)
      (throw e)))))

;; CLIP Lifecycle Functions

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start ^HikariDataSource
  [config]
  (connection/->pool HikariDataSource (merge config additional-config)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn post-start
  "Migrate the database."
  [^HikariDataSource datasource migration-locations]
  (migration/migrate datasource migration-locations))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop
  [^HikariDataSource datasource]
  (.close datasource))
