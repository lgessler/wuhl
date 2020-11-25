(ns wuhl.core
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp]
            [wuhl.application :refer [SPA]]
            [wuhl.ui.root :refer [FulcroRoot]]
            [wuhl.router :as r]))

(defn ^:export refresh []
  (println "Hot code Remount")
  (comp/refresh-dynamic-queries! SPA)
  #_(app/mount! SPA FulcroRoot "app"))

(defn ^:export init []
  (println "Application starting.")
  (app/set-root! SPA FulcroRoot {:initialize-state? true})
  (println "Router starting.")
  (r/init! SPA)
  (println "MOUNTING APP")
  (js/setTimeout #(app/mount! SPA FulcroRoot "app" {:initialize-state? false})))
