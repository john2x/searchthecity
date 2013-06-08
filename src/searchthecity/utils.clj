(ns searchthecity.utils
  (:require [clj-http.client :as http]
            [clojure.string :as s])
  (:use [clj-http.util :only [url-decode url-encode]]
        [net.cgrand.enlive-html :as en]))

(defn hmap->qstring
  [hmap]
  (s/join "&" (map #(apply format "%s=%s" (map name %)) hmap)))

(defn partition-str
  "Split the string at the first occurrence of sep, and return a 3-vector
  containing the part before the separator, the separator itself, and the part
  after the separator. If the separator is not found, return a 3-vector
  containing the string itself, followed by 2 empty strings."
  [s sep]
  (let [parts (s/split s sep)
        sep   (or (re-find sep s) "")]
    [(first parts) sep (s/join sep (rest parts))]))

(defn cleanup
  "remove/replace irrelevant html tags and characters"
  [s]
  (-> s s/join
      (s/replace #"<br ?/?>" "\n")
      (s/replace #"</?i ?>" "")))

