(ns chat-re-frame.echo
  (:require
   [re-frame.core :as re-frame]
   [websocket-fx.pows :as wfx]
   [chat-re-frame.subs :as subs]))

(def socket-id :default)

;; ---
;; event handlers
;; ---

(re-frame/reg-event-db
 ::websocket-connected
 (fn [db]
   ;; (println "event received: websocket connected")
   (assoc db :websocket-status :connected)))

(re-frame/reg-event-db
 ::websocket-disconnected
 (fn [db]
   (assoc db :websocket-status :disconnected)))

(re-frame/reg-event-fx
 ::ws-incoming-message
 (fn [{:keys [db]} [_ _socket-id message]]
   {:db (update-in db [:messages] conj message)}))

(re-frame/reg-event-db
 ::update-echo-message
 (fn [db [_ message]]
   (assoc db :echo-message message)))

(re-frame/reg-event-fx
 ::send-echo-message
 (fn [_ [_ _ message]]
   {:fx [[:dispatch [::wfx/send-message socket-id {:message message}]]
         [:dispatch [::update-echo-message ""]]]}))

;; ---
;; Subscriptions
;; ---

(re-frame/reg-sub
 ::messages
 (fn [db]
   (:messages db)))

(re-frame/reg-sub
 ::echo-message
 (fn [db]
   (:echo-message db)))

;; ---
;; Components
;; ---

(defn websocket-status-component []
  [:h2 "websocket status"
   [:p
    @(re-frame/subscribe [::wfx/status socket-id])]])

(defn echo-message-component
  [idx message-text]
  ^{:key idx}
  [:li message-text])

(defn chat-input-component []
  (let [echo-message (re-frame/subscribe [::echo-message])]
    [:div.container-fluid
     [:input
      {:value    @echo-message
       :on-change   #(re-frame/dispatch [::update-echo-message (-> % .-target .-value)])
       :onKeyPress #(if (= (.-charCode %) 13)
                      (re-frame/dispatch [::send-echo-message socket-id @echo-message]))}]
     [:button
      {:on-click
       (fn [_e]
         (re-frame/dispatch [::send-echo-message socket-id @echo-message]))}
      "Send Message"]]))

(defn echo-view []
  [:div
   (websocket-status-component)
   [:div
    [:h1 "Echo Messages"]
    [:ul (map-indexed #'echo-message-component @(re-frame/subscribe [::messages]))
     (chat-input-component)]]])

;; ---
;; websocket control
;; ---

(def socket-options
  {:url    "wss://echo.websocket.org"
   :format :text
   :on-connect [::websocket-connected]
   :on-disconnect [::websocket-disconnected]
   :on-message ::ws-incoming-message})

(defn start
  []
  (re-frame/dispatch [::wfx/connect socket-id socket-options]))

(defn stop
  []
  (re-frame/dispatch [::wfx/disconnect socket-id]))
