(ns info.magiccards.test.scraper
  (:require [clojure.test :refer :all]
            [info.magiccards.scraper :as scraper]
            [net.cgrand.enlive-html :as en])
  (:use [clj-http.util :only [url-decode url-encode]])
  (:import (java.io File)))

(def thopters (scraper/query "thopter t:creature" 1))
(def goblins-page-2 (scraper/query "goblin" 2))
(def no-results (scraper/query "asdf" nil))
(def ornithopter (scraper/query "ornithopter" nil))
(def anger (scraper/query "!Anger" nil))

(deftest magiccardsinfo-test
  ; thopters!
  (is (=
       (map :name (:cards thopters))
       ["Flowstone Thopter" "Ornithopter" "Roterothopter" "Spined Thopter" "Telethopter" "Thopter Assembly" "Thopter Squadron"]))
  ; pages
  (is (=
       (:pagination thopters)
       {:total-cards 7, :total-pages 1, :current-page 1, :cards-per-page 20}))
  ; pagination
  (is (= (:current-page (:pagination goblins-page-2))
         2))
  ; single result
  (is (= {:cards [{:img "http://magiccards.info/scans/en/m11/211.jpg", :name "Ornithopter", :url "http://magiccards.info/m11/en/211.html", :attrs {:subtype "Thopter", :type "Artifact Creature", :cmc 0, :manacost 0, :loyalty nil, :toughness "2", :power "0"}, :rules-text "Flying", :flavor-text "Regardless of the century, plane, or species, developing artificers never fail to invent the ornithopter."}], :pagination nil}
         ornithopter))
  ; no results
  (is (= {:cards nil :pagination nil} no-results))
  (is (= 1 (count (:cards anger))))
  )


