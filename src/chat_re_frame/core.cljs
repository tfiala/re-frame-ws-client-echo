(ns chat-re-frame.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [chat-re-frame.events :as events]
   [chat-re-frame.views :as views]
   [chat-re-frame.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/chat-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
