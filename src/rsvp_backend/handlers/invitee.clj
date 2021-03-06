(ns rsvp-backend.handlers.invitee
  (:require [cheshire.core :as json]
            [clojure.walk :as clj-walk]
            [ring.util.request :as req]
            [ring.util.response :as res]
            [rsvp-backend.db.invitee :as db]
            [taoensso.timbre :as log]))

(defn new-invitee
  [request]
  (if (=  (:request-method request) :options)
    (res/response {:status "OPTIONS_SUCCESS"})
    (let [query (:query (:params request))
          keywordized-req-body (clj-walk/keywordize-keys (:body request))
          code (Long/parseLong (:code keywordized-req-body))
          updated-req-body (assoc keywordized-req-body :code code)
          resp (db/create-invitee updated-req-body)]
      (if (= resp "AWS_ERROR")
        (res/status (res/response {:status "AWS_ERROR"}) 500)
        (res/response {:status "SUCCESS" :data {:code code}})))))

(defn get-invitee
  [request]
  (let [code (:code (:route-params request))
        data (db/get-invitee (Long/parseLong code))]
    (cond
      (nil? data) (res/status (res/response {:status "NOT_FOUND"}) 404)
      (= data "AWS_ERROR") (res/status (res/response {:status "AWS_ERROR"}) 500)
      (= data "ALREADY_RESPONDED_YES") (res/response {:status "ALREADY_RESPONDED"})
      true (res/response {:status "SUCCESS" :data data}))))

(defn update-metadata
  [request]
  (let [code (Long/parseLong (:code (:route-params request)))
        keywordized-req-body (clj-walk/keywordize-keys (:body request))
        resp (db/update-invitee-metadata code keywordized-req-body)]
    (if (= resp "AWS_ERROR")
      (res/status (res/response {:status "AWS_ERROR"}) 500)
      (res/response {:status "SUCCESS"}))))

(defn update-rsvp
  [request]
  (let [code (Long/parseLong (:code (:route-params request)))
        user-response (:response (clj-walk/keywordize-keys (:body request)))
        resp (db/update-rsvp-status code user-response)]
    (if (= resp "AWS_ERROR")
      (res/status (res/response {:status "AWS_ERROR"}) 500)
      (res/response {:status "SUCCESS"}))))

(defn update-details
  [request]
  (let [code (Long/parseLong (:code (:route-params request)))
        keywordized-req-body (clj-walk/keywordize-keys (:body request))
        resp (db/update-invitee-details code keywordized-req-body)]
    (if (= resp "AWS_ERROR")
      (res/status (res/response {:status "AWS_ERROR"}) 500)
      (res/response {:status "SUCCESS" :rsvp_state "FORM_SUBMITTED"}))))

(defn update-additional-invitees
  [request]
  (let [code (Long/parseLong (:code (:route-params request)))
        keywordized-request-body (clj-walk/keywordize-keys (:body request))
        resp (db/update-additional-invitees code (:additional_invitees keywordized-request-body))]
    (if (= resp "AWS_ERROR")
      (res/status (res/response {:status "AWS_ERROR"}) 500)
      (res/response {:status "SUCCESS"}))))

(defn update-optional-info
  [request]
  (let [code (Long/parseLong (:code (:route-params request)))
        keywordized-req-body (clj-walk/keywordize-keys (:body request))
        resp (db/update-optional-info code keywordized-req-body)]
    (if (= resp "AWS_ERROR")
      (res/status (res/response {:status "AWS_ERROR"}) 500)
      (res/response {:status "SUCCESS"}))))

(defn get-all-invitees
  [request]
  (let [resp (db/get-all-invitees)]
    (if (= resp "AWS_ERROR")
      (res/status (res/response {:status "AWS_ERROR"}) 500)
      (res/response {:status "SUCCESS" :data resp}))))

(defn new-invalid-invitee
  [request]
  (let [kw-req-body (clj-walk/keywordize-keys (:body request))
        email (:email kw-req-body)
        resp (db/new-invalid-invitee email)]
    (if (= resp "AWS_ERROR")
      (res/status (res/response {:status "AWS_ERROR"}) 500)
      (res/response {:status "SUCCESS"}))))
