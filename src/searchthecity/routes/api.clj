(ns searchthecity.routes.api
  (:use compojure.core)
  (:require [searchthecity.views.layout :as layout]
            [searchthecity.util :as util]
            [info.magiccards.scraper :as scraper]
            [noir.response]))

(defn query [request]
  (let [params (:params request)
        result (scraper/query (or (:q params) "") (Integer/parseInt (:p params "1")))]
    (println params)
    (noir.response/json result)))

(defroutes api-routes
  (GET "/api" [request] query))
