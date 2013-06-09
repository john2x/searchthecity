(ns searchthecity.routes.home
  (:use compojure.core)
  (:require [searchthecity.views.layout :as layout]
            [searchthecity.util :as util]))

(defn home-page []
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")}))

(defn about-page []
  (layout/render "about.html"))

(defn syntax-page []
  (layout/render "syntax.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (GET "/syntax" [] (syntax-page)))
