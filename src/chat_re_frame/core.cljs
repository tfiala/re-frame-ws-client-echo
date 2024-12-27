(ns chat-re-frame.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [chat-re-frame.echo :as echo]
   [chat-re-frame.events :as events]
   [chat-re-frame.config :as config]
   [chat-re-frame.websocket :as ws]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [echo/echo-view] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root)
  (ws/start))
