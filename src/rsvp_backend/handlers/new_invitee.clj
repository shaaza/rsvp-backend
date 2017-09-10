(ns rsvp-backend.handlers.new-invitee
  (:require [ring.util.response :as res]))

(defn handler [{:keys [params]}]
  (let [query (:query params)]
    (res/response [{:a "b"}])))
