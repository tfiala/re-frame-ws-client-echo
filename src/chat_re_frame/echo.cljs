(ns chat-re-frame.echo
  (:require
   [re-frame.core :as re-frame]
   [websocket-fx.pows :as wfx]
   [chat-re-frame.subs :as subs]
   [chat-re-frame.websocket :as ws]))

(def socket-id :default)

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
   :on-message [::ws-incoming-message]})

; start the connection process (will happen sometime later)
;; (re-frame/dispatch [::wfx/connect socket-id options])

(defn start
  []
  (re-frame/dispatch [::wfx/connect socket-id socket-options]))

(defn stop
  []
  (re-frame/dispatch [::wfx/disconnect socket-id]))

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
   (println "echo response received")
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
   {::wfx/ws-message {:socket-id socket-id :message message}}))

(defn websocket-status
  []
  [:h2 "websocket status"
   [:p
    @(re-frame/subscribe [::wfx/status ws/socket-id])]])

(defn echo-message
  [idx message-text]
  ^{:key idx}
  [:li message-text])

(defn make-echo-request
  [message]
  {:message message
   :on-response [::echo-response]
   :on-timeout [::echo-timeout]
   :timeout 10000})

(defn dispatch-echo-request
  [message]
  (re-frame/dispatch [::send-echo-message (make-echo-request message)]))

(defn send-button
  []
  [:button
   {:on-click (fn [_e]
                (dispatch-echo-request "hello, world"))}
   "Send Message"])

(defn echo-view []
  [:div
   (websocket-status)
   [:h1 "Echo Messages"
    [:ul (map-indexed #'echo-message @(re-frame/subscribe [::subs/messages]))]
    (send-button)]])
