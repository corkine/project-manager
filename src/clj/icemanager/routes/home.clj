(ns icemanager.routes.home
  (:require
   [icemanager.layout :as layout]
   [icemanager.db.core :as db]
   [clojure.java.io :as io]
   [icemanager.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html"))

