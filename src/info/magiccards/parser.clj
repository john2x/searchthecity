;; Methods to parse the DOM of search query results from magiccards.info
;; ...
;; Here be dragons

(ns info.magiccards.parser
  (:require [clj-http.client :as http]
            [clojure.string :as s]
            [searchthecity.util :as util])
  (:use [clj-http.util :only [url-decode url-encode]]
        [net.cgrand.enlive-html :as en]))

(def MCI "http://magiccards.info")
(def MCIQ (str MCI "/query?q=%s&v=card&s=cname&p=%d"))
(def PT-RE #"[\d\+\*]+/[\d\+\*]+")
(def LOYALTY-RE #"\(Loyalty: [\d\*\+]+\)")
(def MANACOST-RE #"[WUBRG\{\}PX/\d]+ \(\d+\)")
(def CARDTYPE-RE #"[\w ]+(::)?")
(def CARDS-PER-PAGE 20)

(defn- syntax-name
  [names]
  (s/join " " names))

(defn- syntax-rules-text
  [rules-texts]
  (s/join " " (map #(str "o:" %) rules-texts)))

(defn- syntax-card-type
  [types]
  (s/join " " (map #(str "t:" %) types)))

(defn- split-mci-resource
  [page-resource]
  (if (seq (-> page-resource (en/select [:h1])))
    {:pagination-resource nil
     :cards-resource nil}

    {:pagination-resource (nth (en/select page-resource [:table]) 2)
     :cards-resource (-> (en/select page-resource [:table])
                         vec (subvec 3) butlast)}))

(defn- parse-power-toughness
  [s]
  (if 
    (or (s/blank? s) (not (re-find PT-RE s))) {:power nil :toughness nil}
    (let [power         (-> (re-find PT-RE s)
                            (s/split #"/") first)
          toughness     (-> (re-find PT-RE s)
                            (s/split #"/") last)]
      {:power     power
       :toughness toughness})))

(defn- parse-loyalty
  [s]
  (if 
    (or (s/blank? s) (not (re-find LOYALTY-RE s))) {:loyalty nil}
    (let [loyalty (-> (re-find LOYALTY-RE s)
                      (s/split #":") last s/trim (s/replace #"\)" ""))]
      {:loyalty loyalty})))

(defn- parse-manacost
  [s]
  (cond
    (= s "0") {:manacost 0 :cmc 0}
    (or (s/blank? s) (not (re-find MANACOST-RE s))) {:manacost nil :cmc nil}
    :else
    (let [manacost (-> s (s/replace #"\(\d+\)" "") s/trim)
          cmc      (-> (re-find #"\(\d+\)" s) (s/replace #"[\(\)]" ""))]
      {:manacost (or manacost 0)
       :cmc      (or cmc 0)})))

(defn- parse-card-type
  [s]
  (if
    (or (s/blank? s) (not (re-find CARDTYPE-RE s))) {:type nil :subtype nil}
    (let [parts     (util/partition-str s #"::")
          card-type (s/trim (first parts))
          subtype   (-> (last parts)
                        (s/replace PT-RE "")
                        (s/replace LOYALTY-RE "") s/trim)]
      (if (s/blank? subtype) {:type card-type
                              :subtype nil}
          {:type card-type
           :subtype subtype})
      )))


(defn- parse-card-attrs
  [attrs]
  (let [type+pt-part  (-> attrs (s/split #",") first)
        manacost-part (-> attrs (s/split #",") last s/trim)
        card-type     (parse-card-type type+pt-part)
        manacost      (parse-manacost manacost-part)
        attrs-map     (-> {:power nil :toughness nil :loyalty nil}
                          (merge manacost) (merge card-type))]
    (cond
      (re-find #"Creature" (:type card-type))
        (merge attrs-map (parse-power-toughness type+pt-part))
      (re-find #"Planeswalker" (:type card-type))
        (merge attrs-map (parse-loyalty type+pt-part))
      :else attrs-map
      )))

(defn- parse-card
  [card-resource]
  (if (nil? card-resource) nil
    (let [card-image-col (nth (en/select card-resource [:td]) 0)
          card-info-col  (nth (en/select card-resource [:td]) 1)

          img-url        (-> (en/select card-image-col [:img])
                             first (get-in [:attrs :src]))
          card-name      (-> (en/select card-info-col [[:span (attr? :style)]
                                                       :a])
                             first :content first)
          mci-link       (-> (en/select card-info-col [[:span (attr? :style)]
                                                       :a])
                             first (get-in [:attrs :href]))
          attrs          (-> (en/select card-info-col [:p])
                             first :content en/emit* first
                             (s/replace #"—" "::") parse-card-attrs)
          rules-text     (-> (en/select card-info-col [:.ctext :b])
                             first :content en/emit* s/join util/cleanup)
          flavor-text    (-> (en/select card-info-col [:p :i])
                             first :content en/emit* s/join util/cleanup)]
      {:img         img-url
       :name        card-name
       :url         (str MCI mci-link)
       :attrs       attrs
       :rules-text  rules-text
       :flavor-text flavor-text})))

(defn- extract-page-num
  [href]
  (if (s/blank? href) 1
    (let [page-param (re-find #"&p=\d+" href)]
      (if page-param (-> page-param (s/split #"=") last Integer/parseInt)
        nil))))

(defn- total-cards-column
  [pagination-resource]
  (-> pagination-resource
      (en/select [[:td (attr= :align "right")]])
      first))

(defn- pages-column
  [pagination-resource]
  (-> pagination-resource
      (en/select [[:td (attr= :align "center")]])
      first))

(defn- next-page-link-column
  [pagination-resource]
  (-> pagination-resource
      (en/select [:td])
      first))

(defn- multiple-cards?
  "look for the 'n cards' text in total-cards-column"
  [pagination-resource]
  (let [total-cards (-> pagination-resource total-cards-column
                        :content first)]
    (re-find #"\d+ cards" total-cards)))

(defn- parse-pagination
  [pagination-resource]
  (cond
    (nil? pagination-resource) nil
    (not (multiple-cards? pagination-resource)) nil
    :else
    (let [total-cards     (-> pagination-resource total-cards-column
                              :content first s/trim (s/replace #" cards" "") Integer/parseInt)
          total-pages     (-> pagination-resource pages-column
                              :content butlast last :attrs :href extract-page-num)
          next-page-link  (-> pagination-resource next-page-link-column
                              :content butlast last :attrs :href)
          next-page-num   (extract-page-num next-page-link)
          pagination      {:total-cards total-cards
                           :total-pages total-pages
                           :current-page 1
                           :cards-per-page CARDS-PER-PAGE}]
      (if (> next-page-num 2)
         (let [curr-page-num   (dec next-page-num)
               prev-page-num   (dec curr-page-num)
               prev-page-link  (if (> prev-page-num 0)
                                 (-> next-page-link (s/replace #"&p=\d+"
                                                               (str "&p=" prev-page-num)))
                                 nil)]
           (merge pagination {:current-page curr-page-num}))
        pagination))))
