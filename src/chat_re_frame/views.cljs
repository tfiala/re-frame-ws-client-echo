(ns chat-re-frame.views
  (:require
   [re-frame.core :as re-frame]
   [chat-re-frame.subs :as subs]))

(defn message
  [idx message-text]
  ^{:key idx}
  [:li message-text])

(defn chat-panel []
  [:ul (map-indexed #'message @(re-frame/subscribe [::subs/messages]))])

(comment
  (defn main-panel []
    (let [name (re-frame/subscribe [::subs/name])]
      [:div
       [:h1
        "Hello from " @name]])))
