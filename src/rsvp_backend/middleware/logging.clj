(ns rsvp-backend.middleware.logging
  (:require [cheshire.core :as json]
            [ring.util.response :as response]
            [ring.util.request :as req]
            [taoensso.timbre :as log]))

(def standard-ring-request-keys
  [:server-port :server-name :remote-addr :uri :query-string :scheme
   :headers :request-method :params :form-params :query-params])

(defn wrap-log-request-response
 "Logs requests to the server and responses with INFO level."
 [handler]
  (fn [request]
   (log/debug {:event    ::request
               :request  (select-keys request standard-ring-request-keys)})
   (let [response (handler request)]
     (log/debug {:event    ::response
                 :response response})
     response)))

(defn error-response
  [status msg]
  (-> (response/response {:error msg})
      (response/status status)))

(defn wrap-error-logging
  [handler]
  (fn [request]
    (try
      (if-let [response (handler request)]
        response
        (do
          (log/error {:event   ::nil-response
                      :request (select-keys request standard-ring-request-keys)})
          (error-response 500 "Internal Server Error")))
      (catch Throwable ex
        (log/error ex {:event   ::unhandled-exception
                       :request (select-keys request standard-ring-request-keys)})
        (error-response 500 "Internal Server Error")))))
