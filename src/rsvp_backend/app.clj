(ns rsvp-backend.app
  (:require [bidi.ring :as br]
            [cider.nrepl :as cider]
            [clojure.tools.nrepl.server :as nrepl]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :as json]
            [ring.middleware.params :as params]
            [rsvp-backend.handlers.invitee :as invitee]
            [rsvp-backend.util :as util]
            [rsvp-backend.middleware.cors :as cors]
            [rsvp-backend.middleware.logging :as logging]
            [taoensso.timbre :as log]))


(def routes ["/" [["invitee/new" invitee/new-invitee]
                  [["invitee/" :code] invitee/get-invitee]
                  [["invitee/" :code "/metadata"] invitee/update-metadata]
                  [["invitee/" :code "/rsvp"] invitee/update-rsvp]
                  [["invitee/" :code "/details"] invitee/update-details]
                  [true (fn [_] (util/error-response 404 "Page not found."))]]
             true (fn [_] (util/error-response 404 "Page not found."))])

(def main-handler
  (-> (br/make-handler routes)
      json/wrap-json-body
      json/wrap-json-response
      params/wrap-params
      logging/wrap-log-request-response
      logging/wrap-error-logging
      cors/wrap-cors-policy))

(log/set-level! (util/get-config :logging :level))

(defn start! [port nrepl-port]
  (nrepl/start-server :port nrepl-port :handler cider/cider-nrepl-handler)
  (log/info {:event ::nrepl-server-started
             :message (str "NREPL server started on port " nrepl-port ".")})
  (log/info {:event ::jetty-server-started
             :message (str "Jetty server started on port " port ".")})
  (jetty/run-jetty main-handler {:port port}))
