(ns online.harrigan.components.thymeleaf.impl
  {:author "David Harrigan"}
  (:import
   [org.thymeleaf TemplateEngine]
   [org.thymeleaf.context Context IContext]
   [org.thymeleaf.templateresolver ClassLoaderTemplateResolver]))

(set! *warn-on-reflection* true)

(def ^:private template-resolver-defaults
  {:prefix "public/"
   :suffix ".html"
   :cacheable false
   :cache-ttl-ms 0})

(defn ^:private create-template-resolver
  [config]
  (let [{:keys [prefix suffix cacheable cache-ttl-ms]} (merge template-resolver-defaults config)]
    (doto
      (ClassLoaderTemplateResolver.)
      (.setCacheable cacheable)
      (.setCacheTTLMs cache-ttl-ms)
      (.setPrefix prefix)
      (.setSuffix suffix))))

(defn ^:private keywords-as-strings
  [m]
  (zipmap (map name (keys m)) (vals m)))

(defn render
  [viewname data {:keys [template-engine] :as app-config}]
  (let [context (Context.)]
    (when data
      (.setVariables context (keywords-as-strings data)))
    (.process ^TemplateEngine template-engine ^String viewname ^IContext context)))

;; CLIP Lifecycle Functions

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start ^ClassLoaderTemplateResolver
  [config]
  (doto
    (TemplateEngine.)
    (.setTemplateResolver (create-template-resolver config))))
