(ns rsvp-backend.handlers.invitee
  (:require [cheshire.core :as json]
            [clojure.walk :as clj-walk]
            [ring.util.request :as req]
            [ring.util.response :as res]
            [rsvp-backend.db.invitee :as db]
            [taoensso.timbre :as log]))

(defn new-invitee
  [request]
  (let [query (:query (:params request))]
    (log/debug (:body request))
    (res/response {:status "SUCCESS"
                   :data {:code (db/create-invitee (clj-walk/keywordize-keys (:body request)))}})))

(defn get-invitee
  [request]
  (let [code (:code (:route-params request))
        data (db/get-invitee (Integer/parseInt code))]
    (if (not (nil? data))
      (res/response {:status "SUCCESS"
                     :data data})
      (res/status
       (res/response {:status "NOT_FOUND"})
       404))))

(defn update-metadata
  [request]
  (let [code (:code (:route-params request))]
    (db/update-invitee-metadata (Integer/parseInt code) (clj-walk/keywordize-keys (:body request)))
    (res/response {:status "SUCCESS"})))

(defn update-rsvp
  [request]
  (let [code (:code (:route-params request))
        response (:response (clj-walk/keywordize-keys (:body request)))]
    (db/update-rsvp-status (Integer/parseInt code) response)
    (res/response {:status "SUCCESS"})))

(defn update-details
  [request]
  (let [code (:code (:route-params request))]
    (db/update-invitee-details (Integer/parseInt code) (clj-walk/keywordize-keys (:body request))))
  (res/response {:rsvp_state "FORM_SUBMITTED"}))

(defn update-additional-invitees
  [request]
  (let [code (:code (:route-params request))]
    (db/update-additional-invitees
     (Integer/parseInt code)
     {:additional_invitees_json (json/generate-string
                                 (clj-walk/keywordize-keys (:body request))
                                 {:pretty true})})
    (res/response {:status "SUCCESS"})))
