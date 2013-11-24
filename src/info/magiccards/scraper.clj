;;
;; Naive library to scrape results from magiccards.info
;;

(ns info.magiccards.scraper
  (:require [info.magiccards.parser :as parser])
  (:use [clj-http.util :only [url-encode]]
        [net.cgrand.enlive-html :as en]))

; TODO: allow non-english cards

(defn- fetch-mci-resource
  [url]
  (-> url en/html-resource))

(defn- build-url
  [q page]
  ; append "l:en" to force only english cards results
  ; but don't append when using ! syntax
  (let [query (if (-> (first q) (not= \!)) (str q " l:en") q)]
    (java.net.URL. (format parser/MCIQ (url-encode query) (or page 1)))))

(defn query
  [q page]
  (let [url (build-url q page)
        resources (#'parser/split-mci-resource (fetch-mci-resource url))
        pagination (#'parser/parse-pagination (:pagination-resource resources))
        cards (map #'parser/parse-card (:cards-resource resources))]
    {:cards (seq cards)
     :pagination pagination}
     ))

