(ns chat-re-frame.echo
  (:require
   [re-frame.core :as re-frame]
   [websocket-fx.pows :as wfx]
   [chat-re-frame.subs :as subs]))

(def socket-id :default)

(re-frame/reg-event-db
 ::websocket-connected
 (fn [db]
   (println "event received: websocket connected")
   (assoc db :websocket-status :connected)))

(re-frame/reg-event-db
 ::websocket-disconnected
 (fn [db]
   (assoc db :websocket-status :disconnected)))

(re-frame/reg-event-fx
 ::ws-incoming-message
 (fn [{:keys [db]} message]
   (println "echo response received: " message)
   {:db (update-in db [:messages] conj message)}))

(re-frame/reg-event-db
 ::echo-timeout
 (fn [db _]
   (println "echo response timed out")
   db))

(re-frame/reg-event-fx
 ::send-echo-message
 (fn [_ message]
   (println "reg-event-fx, message: " message)
   (re-frame/dispatch [::wfx/send-message socket-id {:message message}])))

(defn websocket-status
  []
  [:h2 "websocket status"
   [:p
    @(re-frame/subscribe [::wfx/status socket-id])]])

(defn echo-message
  [idx message-text]
  ^{:key idx}
  [:li message-text])

(defn send-button
  []
  [:button
   {:on-click
    (fn [_e]
      (re-frame/dispatch [::send-echo-message socket-id "hello, world"]))}
   "Send Message"])

(defn echo-view []
  [:div
   (websocket-status)
   [:h1 "Echo Messages"
    [:ul (map-indexed #'echo-message @(re-frame/subscribe [::subs/messages]))]
    (send-button)]])

(def socket-options
  {; optional. defaults to /ws on the current domain. ws if http and wss if https
   :url    "wss://echo.websocket.org"
   ; optional. defaults to :edn, options are #{:edn :json :transit-json}
   :format :text
   ; optional. additional event to dispatch after the socket is connected
   :on-connect [::websocket-connected]
   ; optional. additional event to dispatch if the socket disconnects
   :on-disconnect [::websocket-disconnected]
   ; tfiala - added this
   :on-message ::ws-incoming-message})

(defn start
  []
  (re-frame/dispatch [::wfx/connect socket-id socket-options]))

(defn stop
  []
  (re-frame/dispatch [::wfx/disconnect socket-id]))
