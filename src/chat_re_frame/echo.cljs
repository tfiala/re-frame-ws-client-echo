(ns chat-re-frame.echo
  (:require
   [re-frame.core :as re-frame]
   [websocket-fx.core :as wfx]
   [chat-re-frame.subs :as subs]
   [chat-re-frame.websocket :as ws]))

(def echo-subscription
  {:message {:kind :echo}
   :on-message [::echo-received]})

(re-frame/reg-event-db
 ::ws/websocket-connected
 (fn [db]
   (println "websocket connected: subscribing to messages")
   (re-frame/dispatch [::wfx/subscribe ws/socket-id :echo-watcher echo-subscription])
   (assoc db :websocket-status :connected)))

(re-frame/reg-event-db
 ::ws/websocket-disconnected
 (fn [db]
   (assoc db :websocket-status :disconnected)))

(re-frame/reg-event-db
 ::echo-received
 (fn [db message]
   (println "echo subscription received")
   (update-in db [:messages] conj message)))

(re-frame/reg-event-db
 ::echo-response
 (fn [db message]
   (println "echo response received")
   (update-in db [:messages] conj message)))

(re-frame/reg-event-db
 ::echo-timeout
 (fn [db _]
   (println "echo response timed out")
   db))

(defn websocket-status
  []
  [:h2 "websocket status"
   [:p
    @(re-frame/subscribe [::wfx/status ws/socket-id])]])
    ;; (case @(re-frame/subscribe [::subs/websocket-status]))
    ;;   :disconnected "disconnected"
    ;;  :connected "connected"]])

(defn echo-message
  [idx message-text]
  ^{:key idx}
  [:li message-text])

(defn send-payload
  [message]
  ;; {:message {:kind :echo :message message}})
  {:kind :echo :message "Hello, Todd!"})

(defn make-echo-request
  [message]
  {:message message
   :on-response [::echo-response]
   :on-timeout [::echo-timeout]
   :timeout 10000})

(defn dispatch-echo-request
  [message]
  (re-frame/dispatch [::wfx/request ws/socket-id (make-echo-request message)]))

(defn send-button
  []
  [:button
   {:on-click (fn [_e]
                (dispatch-echo-request "hello, world"))}
                ;; (re-frame/dispatch [::wfx/push ws/socket-id (send-payload "echo message")]))}
   "Send Message"])

(defn echo-view []
  [:div
   (websocket-status)
   [:h1 "Echo Messages"
    [:ul (map-indexed #'echo-message @(re-frame/subscribe [::subs/messages]))]
    (send-button)]])
