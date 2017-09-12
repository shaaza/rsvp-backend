(ns rsvp-backend.handlers.new-invitee
  (:require [ring.util.response :as res]
            [rsvp-backend.db.invitee :as db]))

(defn handler [{:keys [params]}]
  (let [query (:query params)]
    (res/response [{:a "b"}])))
