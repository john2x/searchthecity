(ns searchthecity.routes.api
  (:use compojure.core)
  (:require [searchthecity.views.layout :as layout]
            [searchthecity.util :as util]
            [searchthecity.mci-api :as mci]
            [noir.response]))

(defn query [request]
  (let [params (:params request)
        result (mci/query-mci (or (:q params) "") (Integer/parseInt (:p params "1")))]
    (println params)
    (noir.response/json result)))

(defroutes api-routes
  (GET "/api" [request] query))
