(ns chat-re-frame.websocket
  (:require
   [re-frame.core :as re-frame]
   [websocket-fx.core :as wfx]))

(def socket-id :default)

(def options
  {; optional. defaults to /ws on the current domain. ws if http and wss if https
   :url    "wss://echo.websocket.org"
   ; optional. defaults to :edn, options are #{:edn :json :transit-json}
   :format :json
   ; optional. additional event to dispatch after the socket is connected
   :on-connect [::websocket-connected]
   ; optional. additional event to dispatch if the socket disconnects
   :on-disconnect [::websocket-disconnected]})

; start the connection process (will happen sometime later)
;; (re-frame/dispatch [::wfx/connect socket-id options])

; cleanup / disconnect that particular socket
(re-frame/dispatch [::wfx/disconnect socket-id])

(defn start
  []
  (re-frame/dispatch [::wfx/connect socket-id options]))

(defn stop
  []
  (re-frame/dispatch [::wfx/disconnect socket-id]))
