(ns digitalocean-api.core
  (:require [clj-http.client :as client]))

(def ^:dynamic *base-url* "https://api.digitalocean.com")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- fix-keyword
  "Takes a parameter name and replaces the - with a _"
  [param-name]
  (keyword (.replace (name param-name) \- \_)))

(defn- transform-map
  "transforms a clojure map into something that can be sent to a
  web server as query-params"
  [params]
  (into {} (for [[k v] params] [(fix-keyword (name k)) v])))

(defn- api-get [entity creds & [entity-id]]
  (client/get (apply str (interpose "/" [*base-url* (name entity) entity-id]))
              {:as :auto
               :query-params (transform-map creds)}))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def show-droplets (partial api-get :droplets))
