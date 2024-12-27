(ns chat-re-frame.db)

(def default-db
  {:name "re-frame"
   :messages ["Hi Todd! How are you?"
              "I am doing just fine, thanks :-)"]
   :websocket-status :disconnected})
