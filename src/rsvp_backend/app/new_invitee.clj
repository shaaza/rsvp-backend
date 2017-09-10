(ns rsvp-backend.app.new-invitee
  (:require [ring.util.response :as res]))

(defn handler [{:keys [params]}]
  (let [query (:query params)]
    (res/response [{:a "b"}])))
