(ns rsvp-backend.app
  (:require [bidi.ring :as br]
            [cider.nrepl :as cider]
            [clojure.tools.nrepl.server :as nrepl]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :as json]
            [ring.middleware.params :as params]
            [rsvp-backend.handlers.invitee :as invitee]
            [rsvp-backend.handlers.view :as view]
            [rsvp-backend.util :as util]
            [rsvp-backend.middleware.cors :as cors]
            [rsvp-backend.middleware.logging :as logging]
            [taoensso.timbre :as log]))


(def routes ["/" [["admin/show" view/html-table]
                  ["invitee/new" invitee/new-invitee]
                  ["invitee/all" invitee/get-all-invitees]
                  ["invitee/invalid-coin" invitee/new-invalid-invitee]
                  [["invitee/" :code] invitee/get-invitee]
                  [["invitee/" :code "/metadata"] invitee/update-metadata]
                  [["invitee/" :code "/rsvp"] invitee/update-rsvp]
                  [["invitee/" :code "/details"] invitee/update-details]
                  [["invitee/" :code "/additional-invitees"] invitee/update-additional-invitees]
                  [["invitee/" :code "/optional-info"] invitee/update-optional-info]
                  [true (fn [_] (util/error-response 404 "Page not found."))]]
             true (fn [_] (util/error-response 404 "Page not found."))])

(def main-handler
  (-> (br/make-handler routes)
      logging/wrap-log-request-response
      logging/wrap-error-logging
      (json/wrap-json-body {:keywords? true :bigdecimals? true})
      json/wrap-json-response
      params/wrap-params
      cors/wrap-cors-policy))

(log/set-level! (util/get-config :logging :level))

(defn start! [port nrepl-port]
  (nrepl/start-server :port nrepl-port :handler cider/cider-nrepl-handler)
  (log/info {:event ::nrepl-server-started
             :message (str "NREPL server started on port " nrepl-port ".")})
  (log/info {:event ::jetty-server-started
             :message (str "Jetty server started on port " port ".")})
  (jetty/run-jetty main-handler {:port port}))
