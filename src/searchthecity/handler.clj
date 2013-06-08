(ns searchthecity.handler
  (:use searchthecity.routes.home
        searchthecity.routes.api
        compojure.core
        ring.middleware.params)
  (:require [noir.util.middleware :as middleware]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [com.postspectacular.rotor :as rotor]))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn destroy []
  (timbre/info "picture-gallery is shutting down"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/append})
  
  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "searchthecity.log" :max-size 10000 :backlog 10})
  
  (timbre/info "searchthecity started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "searchthecity is shutting down..."))

;;append your application routes to the all-routes vector
(def all-routes [home-routes api-routes app-routes])

(def app (middleware/app-handler all-routes 
                                 ;;put any custom middleware
                                 ;;in the middleware vector
                                 :middleware []
                                 ;;add access rules here
                                 :access-rules []))

(def war-handler (middleware/war-handler app))
