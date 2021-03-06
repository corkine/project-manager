(ns icemanager.feature
  (:require
    [cheshire.core :refer [generate-string parse-string]]
    [next.jdbc.date-time]
    [next.jdbc.prepare]
    [next.jdbc :as jdbc]
    [next.jdbc.result-set]
    [clojure.tools.logging :as log]
    [conman.core :as conman]
    [icemanager.config :refer [env]]
    [icemanager.db.core :as db]
    [mount.core :refer [defstate]])
  (:import (java.time LocalDateTime)))

(defn all-features []
  (db/all-features))

(defn feature-by-rs-id [rs-id-lower]
  (db/get-feature-by-rs-id {:rs_id rs-id-lower}))

(defn update-feature [id feature-map]
  (let [select-data (select-keys feature-map
                                 [:rs_id
                                  :title
                                  :description
                                  :version
                                  :info])
        new-data (assoc select-data :update-at (LocalDateTime/now))]
    (jdbc/with-transaction
      [t db/*db*]
      (let [old-data (db/get-feature-by-id t {:id id})
            m-data (assoc (merge old-data new-data) :id id)]
        #_(log/info "data from db: " old-data
                    ", from frontEnd: " feature-map
                    ", from req: " new-data
                    ", merged: " m-data)
        (db/update-feature t m-data)))))

(defn fetch-usage []
  (jdbc/with-transaction
    [t db/*db*]
    (let [usage (db/api-served-count t)
          api-usage (db/last-10-edit t)]
      (merge usage {:usage api-usage}))))

(defn add-feature [data]
  (try
    (let [merged-data (assoc data :info {:status (:status data)}
                                  :description (or (:description data) ""))]
      {:message "添加项目成功。"
       :data (db/insert-feature merged-data)})
    (catch Exception e
      {:message (str "添加项目失败： "  e)})))

(defn delete-feature [data]
  (try
    {:message "删除项目成功。"
     :data (db/delete-feature data)}
    (catch Exception e
      {:message (str "删除项目失败： " e)})))
