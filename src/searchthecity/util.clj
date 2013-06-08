(ns searchthecity.util
  (:require [noir.io :as io]
            [markdown.core :as md]
            [clj-http.client :as http]
            [clojure.string :as s])
  (:use [clj-http.util :only [url-decode url-encode]]
        [net.cgrand.enlive-html :as en]))

(defn format-time
  "formats the time using SimpleDateFormat, the default format is
   \"dd MMM, yyyy\" and a custom one can be passed in as the second argument"
  ([time] (format-time time "dd MMM, yyyy"))
  ([time fmt]
    (.format (new java.text.SimpleDateFormat fmt) time)))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (->>
    (io/slurp-resource filename)
    (md/md-to-html-string)))

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

