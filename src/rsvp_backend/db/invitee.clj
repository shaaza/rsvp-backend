(ns rsvp-backend.db.invitee
  (:require [clojure.string :as str]
            [rsvp-backend.util :as util]
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
  (try (ddb/put-item
        client-opts
        db-name
        {:name name
         :code code
         :date_given date_given
         :given_by given_by
         :pre_entered_company pre_entered_company
         :pre_entered_title pre_entered_title
         :origination_source origination_source
         :relationship relationship
         :confirmation "UNVERIFIED"
         :rsvp_state "UNVERIFIED"})
       (catch Exception e "AWS_ERROR")))


(defn get-invitee
  "Given the passcode, get the prefilled invitee details"
  [code]
  (let [entry (try (ddb/get-item client-opts db-name {:code code})
                   (catch Exception e "AWS_ERROR"))
        times-verified (or (:times_verified entry) 0)]
    (cond
      (and (not (nil? entry))
           (= (:rsvp_state entry) "UNVERIFIED"))
      (do (try (ddb/update-item client-opts db-name {:code code}
                                {:update-map {:rsvp_state [:put "VERIFIED"]
                                              :times_verified [:put 1]}})
               (assoc entry :rsvp_state "VERIFIED")
               (catch Exception e "AWS_ERROR")))
      (and (not (nil? entry))
           (= (:rsvp_state entry) "VERIFIED"))
      (try (ddb/update-item client-opts db-name {:code code}
                            {:update-map {:times_verified [:put (+ 1 times-verified)]}})
           entry
           (catch Exception e "AWS_ERROR"))
      (and (not (nil? entry))
           (or (= (:rsvp_state entry) "RESPONDED")
               (= (:rsvp_state entry) "FORM_SUBMITTED"))
           (or (= (str/lower-case (:confirmation entry)) "yes")
               (= (str/lower-case (:confirmation entry)) "maybe")))
      "ALREADY_RESPONDED_YES"
      (not (nil? entry))
      (try (ddb/update-item client-opts db-name {:code code}
                            {:update-map {:times_verified [:put (+ 1 times-verified)]}})
           entry
               (catch Exception e "AWS_ERROR")))))

(defn update-invitee-metadata
  "Update prefilled invitee details"
  [code {:keys [given_by origination_source]
         :or {given_by nil origination_source nil}}]
  (try (ddb/update-item client-opts db-name {:code code}
                        {:update-map {:given_by [:put given_by]
                                      :origination_source [:put origination_source]}})
       (catch Exception e "AWS_ERROR")))

(defn update-rsvp-status
  "Allow the user to respond with YES, NO or MAYBE"
  [code response]
  (try (ddb/update-item client-opts db-name {:code code}
                        {:update-map {:confirmation [:put response]
                                      :rsvp_state [:put "RESPONDED"]}})
       (catch Exception e "AWS_ERROR")))

(defn update-invitee-details
  "Update the user's data with the submitted form data"
  [code {:keys [name company title address address_2 city state zip
                email mobile_number work_phone website]
         :or {name nil company nil title nil address nil address_2 nil city nil state nil zip
              nil email nil mobile_number nil work_phone nil website nil}}]
  (try (ddb/update-item client-opts db-name {:code code}
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
                                      :rsvp_state [:put "FORM_SUBMITTED"]}})
       (catch Exception e "AWS_ERROR")))

(defn update-additional-invitees
  [code {:keys [additional_invitees_json] :or [additional_invitees_json nil]}]
  (try (ddb/update-item client-opts db-name {:code code}
                        {:update-map {:additional_invitees [:put additional_invitees_json]}})
       (catch Exception e "AWS_ERROR")))

(defn update-optional-info
  "Update prefilled invitee details"
  [code {:keys [is_advisor is_mentor is_affiliated_municipality
                is_support_ventures is_interested_in_partnership
                is_interested_in_innovation_fair startup_phase
                any_funding_yet industry incubator
                incubator_name corporate_partnerships]
         :or {is_advisor nil is_mentor nil is_affiliated_municipality nil
              is_support_ventures nil is_interested_in_partnership nil
              is_interested_in_innovation_fair nil startup_phase nil
              any_funding_yet nil industry nil incubator nil
              incubator_name nil corporate_partnerships nil}}]
  (try (ddb/update-item client-opts db-name {:code code}
                        {:update-map {:is_advisor [:put is_advisor]
                                      :is_mentor [:put is_mentor]
                                      :is_affiliated_municipality [:put is_affiliated_municipality]
                                      :is_support_ventures [:put is_support_ventures]
                                      :is_interested_in_partnership [:put is_interested_in_partnership]
                                      :is_interested_in_innovation_fair [:put is_interested_in_innovation_fair]
                                      :startup_phase [:put startup_phase]
                                      :any_funding_yet [:put any_funding_yet]
                                      :industry [:put industry]
                                      :incubator [:put incubator]
                                      :incubator_name [:put incubator_name]
                                      :corporate_partnerships [:put corporate_partnerships]
                                      }})
       (catch Exception e "AWS_ERROR")))

(defn get-all-invitees
  []
  (try (ddb/scan client-opts db-name)
       (catch Exception e "AWS_ERROR")))
