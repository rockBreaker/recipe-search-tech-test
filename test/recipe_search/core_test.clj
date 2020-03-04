(ns recipe-search.core-test
  (:require [clojure.test :refer :all]
            [recipe-search.core :refer :all]))

;;Basic test cases to show the different tests working
(deftest search-resource-recipes-test
  (testing "Given a relevant search term, returns some relevant results"
    (is (= ["Courgette and tomato soup with basil croutons"
            "Fresh tomato soup with tapenade toast"
            "Pappa al pomodoro (bread and tomato soup)"]
           (mapv :recipe-name (search-resource-recipes "tomato soup" 3)))))
  (testing "Given an irrelevant search term, returns nothing"
    (is (empty? (search-resource-recipes "page 22" 10)))))

(deftest search-new-recipes-test
  (testing "Given a relevant search term and a path to new recipes, returns relevant results"
    (is (= "Apple and beetroot cake"
           (:recipe-name (first (search-new-recipes "resources/new-recipes" "Apple and beetroot" 3)))))))

(deftest word-frequency-search-test
  (testing "Given an exact match search term, returns results with exact match with higher relevance"
    (let [results (mapv (juxt :recipe-name :relevancy) (word-frequency-search "Zaalouk" 5))]
      (is (= [["Zaalouk" 6]
              ["Aglio olio" 1]
              ["One pot mixed veg (agrodolce) to go with your sunday roast" 1]
              ["All-in-one roast lamb" 1]
              ["all purpose stuffing" 1]]
             results))))
  (testing "Given a partial match search term, returns results with partial matches with higher relevance"
    (let [results (mapv (juxt :recipe-name :relevancy) (word-frequency-search "roast beef" 5))]
      (is (= [["One pan roast beef and roots with horseradish glaze" 11]
              ["Mustard crusted roast beef with white bean mash & walnut dressed beans" 9]
              ["Pot roast beef brisket in ale" 8]
              ["Roast beef blinis" 8]
              ["Warm roast beef with a salad of new potatoes, watercress and wasabi" 7]]
             results)))))