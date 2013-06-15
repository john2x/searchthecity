(ns searchthecity.test.mci-api
  (:require [clojure.test :refer :all]
            [searchthecity.mci-api :as mci]
            [net.cgrand.enlive-html :as en])
  (:use [clj-http.util :only [url-decode url-encode]])
  (:import (java.io File)))

(deftest build-query-test
  (is (=
       (mci/build-query {:names ["foo" "\"foo bar\""]
                         :rules ["flying" "\"add mana\""]
                         :types ["creature" "artifact"]})
       (url-encode "foo \"foo bar\" o:flying o:\"add mana\" t:creature t:artifact")))
  (is (=
       (mci/build-query {:names ["foo" "\"foo bar\""]
                         :rules ["flying" "\"add mana\""]
                         :types ["creature" "artifact"]})
       (url-encode "foo \"foo bar\" o:flying o:\"add mana\" t:creature t:artifact"))))

(deftest parsers-test
  ; power/toughness
  (is (=
       (mci/parse-power-toughness "4/7")
       {:power "4" :toughness "7"}))
  (is (=
       (mci/parse-power-toughness "Burn (Turn/Burn) 4/7")
       {:power "4" :toughness "7"}))
  (is (=
       (mci/parse-power-toughness "Creature :: Griffin 1+*/*")
       {:power "1+*" :toughness "*"}))

  ; loyalty
  (is (=
       (mci/parse-loyalty "Planeswalker - Jace (Loyalty: 1+*)")
       {:loyalty "1+*"}))
  (is (=
       (mci/parse-loyalty "(Loyalty: 3)")
       {:loyalty "3"}))

  ; manacosts
  (is (=
       (mci/parse-manacost "3W (4)")
       {:manacost "3W" :cmc "4"}))
  (is (=
       (mci/parse-manacost "XR (1)")
       {:manacost "XR" :cmc "1"}))
  (is (=
       (mci/parse-manacost "3{UP} (4)")
       {:manacost "3{UP}" :cmc "4"}))
  (is (=
       (mci/parse-manacost "X{G/U}{G/U} (2)")
       {:manacost "X{G/U}{G/U}" :cmc "2"}))
  (is (=
       (mci/parse-manacost "WBRG")
       {:manacost nil :cmc nil}))
  (is (=
       (mci/parse-manacost "{2/W}{2/U}{2/B}{2/R}{2/G} (10)")
       {:manacost "{2/W}{2/U}{2/B}{2/R}{2/G}" :cmc "10"}))

  ; attributes
  (is (=
       (mci/parse-card-attrs "Creature :: Griffin 2/2, 3W (4)")
       {:type "Creature"
        :subtype "Griffin"
        :power "2"
        :toughness "2"
        :manacost "3W"
        :cmc "4"
        :loyalty nil}))
  (is (=
       (mci/parse-card-attrs "Planeswalker :: Jace (Loyalty: 3), 1UU (3)")
       {:type "Planeswalker"
        :subtype "Jace"
        :power nil
        :toughness nil
        :manacost "1UU"
        :cmc "3"
        :loyalty "3"}))
  (is (=
       (mci/parse-card-attrs "Creature :: Werewolf 4/4")
       {:type "Creature"
        :subtype "Werewolf"
        :power "4"
        :toughness "4"
        :manacost nil
        :cmc nil
        :loyalty nil}))
  (is (=
       (mci/parse-card-attrs "Tribal Sorcery :: Eldrazi, 7 (7)")
       {:type "Tribal Sorcery"
        :subtype "Eldrazi"
        :power nil
        :toughness nil
        :manacost "7"
        :cmc "7"
        :loyalty nil}))
  (is (=
       (mci/parse-card-attrs "Legendary Land")
       {:type "Legendary Land"
        :subtype nil
        :power nil
        :toughness nil
        :manacost nil
        :cmc nil
        :loyalty nil}))
  (is (=
       (mci/parse-card-attrs "Basic Land :: Forest")
       {:type "Basic Land"
        :subtype "Forest"
        :power nil
        :toughness nil
        :manacost nil
        :cmc nil
        :loyalty nil}))
  )

(def test-html-resource (en/html-resource (File. "test/searchthecity/test/test.html")))
(def pagination-resource (:pagination-resource (mci/split-mci-resource test-html-resource)))
(def cards-resource (:cards-resource (mci/split-mci-resource test-html-resource)))
(def arcbound-ravager (nth cards-resource 0))
(def ravager-of-the-fells (nth cards-resource 1))
(def alive-alive+well (nth cards-resource 2))
(def nicol-bolas (nth cards-resource 3))

(deftest parse-card-test
  (is (=
       (mci/parse-card arcbound-ravager)
       {:img "http://magiccards.info/scans/en/ds/100.jpg", :name "Arcbound Ravager", :url "http://magiccards.info/ds/en/100.html", :attrs {:subtype "Beast", :type "Artifact Creature", :cmc "2", :manacost "2", :loyalty nil, :toughness "0", :power "0"}, :rules-text "Sacrifice an artifact: Put a +1/+1 counter on Arcbound Ravager.\n\nModular 1 (This enters the battlefield with a +1/+1 counter on it. When it dies, you may put its +1/+1 counters on target artifact creature.)", :flavor-text ""}))
 
  (is (=
       (mci/parse-card ravager-of-the-fells)
        {:img "http://magiccards.info/scans/en/dka/140b.jpg", :name "Ravager of the Fells", :url "http://magiccards.info/dka/en/140b.html", :attrs {:subtype "Werewolf", :type "Creature", :cmc nil, :manacost nil, :loyalty nil, :toughness "4", :power "4"}, :rules-text "Trample\n\nWhenever this creature transforms into Ravager of the Fells, it deals 2 damage to target opponent and 2 damage to up to one target creature that player controls.\n\nAt the beginning of each upkeep, if a player cast two or more spells last turn, transform Ravager of the Fells.", :flavor-text ""}))

  (is (=
       (mci/parse-card alive-alive+well)
       {:img "http://magiccards.info/scans/en/dgm/121a.jpg", :name "Alive (Alive/Well)", :url "http://magiccards.info/dgm/en/121a.html", :attrs {:subtype nil, :type "Sorcery", :cmc "4", :manacost "3G", :loyalty nil, :toughness nil, :power nil}, :rules-text "Put a 3/3 green Centaur creature token onto the battlefield.\n\nFuse (You may cast one or both halves of this card from your hand.)", :flavor-text ""}))
  (is (=
       (mci/parse-card nicol-bolas)
       {:img "http://magiccards.info/scans/en/m13/199.jpg", :name "Nicol Bolas, Planeswalker", :url "http://magiccards.info/m13/en/199.html", :attrs {:subtype "Bolas", :type "Planeswalker", :cmc "8", :manacost "4UBBR", :loyalty "5", :toughness nil, :power nil}, :rules-text "+3: Destroy target noncreature permanent.\n\n-2: Gain control of target creature.\n\n-9: Nicol Bolas, Planeswalker deals 7 damage to target player. That player discards seven cards, then sacrifices seven permanents.", :flavor-text ""}))
  )

(deftest parse-pagination-test
  (is (=
       (mci/parse-pagination pagination-resource)
       {:total-cards 82, :total-pages 5, :current-page 1, :cards-per-page 20})))

(def thopters (mci/query-mci "thopter t:creature" 1))
(def goblins-page-2 (mci/query-mci "goblin" 2))
(def no-results (mci/query-mci "asdf" nil))
(def ornithopter (mci/query-mci "ornithopter" nil))
(def anger (mci/query-mci "!Anger" nil))

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


