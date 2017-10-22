(ns rsvp-backend.view.invitee-list
  (:require [hiccup.core :as h]))


(defn contact-details-td
  [row-data]
  [:td
   (:email row-data) [:br]
   (str "M: " (:mobile_number row-data)) [:br]
   (str "W: "(:work_phone row-data))])

(defn address-details-td
  [row]
  [:td
   (:address row) [:br]
   (:address_2 row) [:br]
   (str (:city row) ", " (:state row)) [:br]
   (:zip row)])

(defn company-details-td
  [row]
  [:td
   (str "Title: " (:title row)) [:br]
   (:company row) [:br]
   (:website row)])

(defn form-state-details-td
  [row]
  [:td
   (:rsvp_state row) [:br]
   (when (and (not (= (:times_verified row) nil))
              (> (:times_verified row) (Integer/parseInt 1))) "(Duplicate?)")])

(defn render
  [data]
  (h/html [:html
           [:head
            [:link {:rel "stylesheet"
                    :href "https://unpkg.com/purecss@1.0.0/build/pure-min.css"
                    :integrity "sha384-nn4HPE8lTHyVtfCBi5yW9d20FjT8BJwUXyWZT9InLYax14RDjBj46LmSztkmNP9w"
                    :crossorigin "anonymous"}]]
           [:body
            [:table {:class "pure-table"}
             [:thead
              [:tr
               [:th "Coin #"]
               [:th "Name"]
               [:th "Attending?"]
               [:th "Addl. invitees?"]
               [:th "Form State"]
               [:th "Contact"]
               [:th "Address"]
               [:th "Company"]]]
             [:tbody
              (for [row data]
                (when (and (not (= (:rsvp_state row) "UNVERIFIED"))
                           (not (= (:rsvp_state row) nil)))
                  [:tr
                   [:td (:code row)]
                   [:td (:name row)]
                   [:td (:confirmation row)]
                   [:td (:additional_invitees "Yes")]
                   (form-state-details-td row)
                   (contact-details-td row)
                   (address-details-td row)
                   (company-details-td row)
                   ]))]]]]))
