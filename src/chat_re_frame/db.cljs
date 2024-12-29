(ns chat-re-frame.db)

(def default-db
  {:name "re-frame"
   :messages []
   :websocket-status :disconnected
   :echo-message ""})
