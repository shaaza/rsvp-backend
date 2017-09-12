(ns rsvp-backend.db.invitee
  (:require [rsvp-backend.util :as util]
            [taoensso.faraday :as ddb]))

(def client-opts
  {:access-key (util/get-config :aws :dynamodb :access-key)
   :secret-key (util/get-config :aws :dynamodb :secret-key)
   :endpoint "https://dynamodb.us-west-2.amazonaws.com"
   })

(def db-name (util/get-config :aws :dynamodb :db-name))

(defn create-invitee
  "Add a new invitee to the database"
  [{:keys [name code date-given given-by pre-entered-company pre-entered-title origination-source relationship]
    :or {name nil code nil date-given nil given-by nil pre-entered-company nil pre-entered-title nil origination-source nil relationship nil}}]
  (ddb/put-item
   client-opts
   db-name
   {:name name
    :code (Integer/parseInt code)
    :date_given date-given
    :given_by given-by
    :pre_entered_company pre-entered-company
    :pre_entered_title pre-entered-title
    :origination_source origination-source
    :relationship relationship
    :confirmation "UNVERIFIED"
    :rsvp_state "UNVERIFIED"})
  code)


(defn get-invitee
  "Given the passcode, get the prefilled invitee details"
  [code]
  (ddb/update-item client-opts db-name {:code code}
                   {:update-map {:rsvp_state [:put "VERIFIED"]}})
  (ddb/get-item client-opts db-name {:code code}))

(defn update-invitee-metadata
  "Update prefilled invitee details"
  [code {:keys [given_by origination_source]
         :or {given_by nil origination-source nil}}]
  (ddb/update-item client-opts db-name {:code code}
                   {:update-map {:given_by [:put given_by]
                                 :origination_source [:put origination_source]}}))

(defn update-rsvp-status
  "Allow the user to respond with YES, NO or MAYBE"
  [code response]
  (ddb/update-item client-opts db-name {:code code}
                   {:update-map {:confirmation [:put response]
                                 :rsvp_state [:put "RESPONDED"]}}))

(defn update-invitee-details
  "Update the user's data with the submitted form data"
  [code {:keys [name company title address address_2 city state zip
                email mobile_phone work_phone website]
         :or {name nil company nil title nil address nil address_2 nil city nil state nil zip
              nil email nil mobile_phone nil work_phone nil website nil}}]
  (ddb/update-item client-opts db-name {:code code}
                   {:update-map {:name [:put name]
                                 :company [:put company]
                                 :title [:put title]
                                 :address [:put address]
                                 :address_2 [:put address_2]
                                 :city [:put city]
                                 :state [:put state]
                                 :zip [:put zip]
                                 :email [:put email]
                                 :mobile_phone [:put mobile_phone]
                                 :work_phone [:put work_phone]
                                 :website [:put website]
                                 :rsvp_state [:put "FORM_SUBMITTED"]}}))
