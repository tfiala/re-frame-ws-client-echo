(ns chat-re-frame.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::messages
 (fn [db]
   (:messages db)))

(re-frame/reg-sub
 ::websocket-status
 (fn [db]
   (:websocket-status db)))
