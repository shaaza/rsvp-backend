(ns rsvp-backend.handlers.view
  (:require [ring.util.response :as res]
            [rsvp-backend.db.invitee :as db]
            [rsvp-backend.view.invitee-list :as html]))


(defn html-table
  [request]
  (comment db/get-all-invitees)
  (res/response (html/render (db/get-all-invitees))))
