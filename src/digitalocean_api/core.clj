(ns digitalocean-api.core
  (:require [clojure.walk :as w])
  (:require [clj-http.client :as client]))

(def ^:dynamic *base-url* "https://api.digitalocean.com")

(defn- transform-key
  ([key f]
     (-> key name f keyword)))

(defn clojurize [name]
  (.replace name "_" "-"))

(defn snakecaseize [name]
  (.replace name "-" "_"))

(defn deep-transform-keys [m f]
  "Walks map m and applies transformation function to each key. If value is a
  map, recursively transforms it, too.
  Caveat: may transform values, too. However, those should hardly appear as
  keywords"
  (w/postwalk (fn [x] (if (keyword? x) (transform-key x f) x)) m))

(defn- plural [word]
  (str word \s))

(defn- api-get [entity creds & [entity-id]]
  (let [response (client/get (apply str (interpose "/" [*base-url* (-> entity name snakecaseize) entity-id]))
                             {:as :auto
                              :query-params (deep-transform-keys creds snakecaseize)})]
    (if (= "OK" (get-in response [:body :status]))
      (deep-transform-keys response clojurize))))

(defmacro defcollection [x]
  "Usage: (defcollection droplets)"
  `(defn ~x []
     (get-in (api-get ~(keyword x) *credentials*) [:body ~(keyword x)])))

(defmacro defitem [x]
  "Usage: (defitem droplet)"
  `(defn ~x [id#]
     (get-in (api-get ~(-> x name plural keyword) *credentials* id#) [:body ~(keyword x)])))

(def ^:dynamic *credentials* {:client-id ""
                              :api-key ""})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; High-level API
(defcollection droplets)
(defcollection regions)
(defcollection images)
(defcollection ssh-keys)
(defcollection sizes)
(defcollection domains)

(defitem droplet)
(defitem region)
(defitem image)
(defitem ssh-key)
(defitem size)
(defitem domain)
(defitem event)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Support for API v0.1.0
;;
(defn show-droplets [creds & [id]]
  (binding [*credentials* creds]
    (if (nil? id)
      (droplets)
      (droplet id))))

