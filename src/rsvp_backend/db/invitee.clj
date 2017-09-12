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
    :code code
    :dateGiven date-given
    :givenBy given-by
    :preEnteredCompany pre-entered-company
    :preEnteredTitle pre-entered-title
    :originationSource origination-source
    :relationship relationship
    :confirmation "UNVERIFIED"
    :rsvpState "UNVERIFIED"})
  code)


(defn get-invitee
  "Given the passcode, get the prefilled invitee details"
  [code]
  (ddb/update-item client-opts db-name {:code code}
                   {:update-map {:rsvpState [:put "VERIFIED"]}})
  (ddb/get-item client-opts db-name {:code code}))

(defn update-invitee-metadata
  "Update prefilled invitee details"
  [code {:keys [given-by origination-source]
         :or {given-by nil origination nil}}]
  (ddb/update-item client-opts db-name {:code code}
                   {:update-map {:givenBy [:put given-by]
                                 :originationSource [:put origination-source]}}))

(defn update-rsvp-status
  "Allow the user to respond with YES, NO or MAYBE"
  [code response]
  (ddb/update-item client-opts db-name {:code code}
                   {:update-map {:confirmation [:put response]
                                 :rsvpState [:put "RESPONDED"]}}))

(defn update-invitee-details
  "Update the user's data with the submitted form data"
  [code {:keys [name company title address address-2 city state zip
                email mobile-phone work-phone website]
         :or {name nil company nil title nil address nil address-2 nil city nil state nil zip
              nil email nil mobile-phone nil work-phone nil website nil}}]
  (ddb/update-item client-opts db-name {:code code}
                   {:update-map {:name [:put name]
                                 :company [:put company]
                                 :title [:put title]
                                 :address [:put address]
                                 :address2 [:put address-2]
                                 :city [:put city]
                                 :state [:put state]
                                 :zip [:put zip]
                                 :email [:put email]
                                 :mobilePhone [:put mobile-phone]
                                 :workPhone [:put work-phone]
                                 :website [:put website]
                                 :rsvpState [:put "FORM_SUBMITTED"]}}))
