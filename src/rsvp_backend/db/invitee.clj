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
  [{:keys [name code date_given given_by pre_entered_company pre_entered_title origination_source relationship]
    :or {name nil code nil date_given nil given_by nil pre_entered_company nil pre_entered_title nil origination_source nil relationship nil}}]
  (ddb/put-item
   client-opts
   db-name
   {:name name
    :code (Integer/parseInt code)
    :date_given date_given
    :given_by given_by
    :pre_entered_company pre_entered_company
    :pre_entered_title pre_entered_title
    :origination_source origination_source
    :relationship relationship
    :confirmation "UNVERIFIED"
    :rsvp_state "UNVERIFIED"})
  code)


(defn get-invitee
  "Given the passcode, get the prefilled invitee details"
  [code]
  (let [entry (ddb/get-item client-opts db-name {:code code})]
    (when (not (nil? entry))
      (ddb/update-item client-opts db-name {:code code}
                       {:update-map {:rsvp_state [:put "VERIFIED"]}}))
    entry))

(defn update-invitee-metadata
  "Update prefilled invitee details"
  [code {:keys [given_by origination_source]
         :or {given_by nil origination_source nil}}]
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
                email mobile_number work_phone website]
         :or {name nil company nil title nil address nil address_2 nil city nil state nil zip
              nil email nil mobile_number nil work_phone nil website nil}}]
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
                                 :mobile_number [:put mobile_number]
                                 :work_phone [:put work_phone]
                                 :website [:put website]
                                 :rsvp_state [:put "FORM_SUBMITTED"]}}))
