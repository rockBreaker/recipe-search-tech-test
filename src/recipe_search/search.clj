(ns recipe-search.search
  (:require [clojure.string :as str]))

;;Helper Functions
(defn- strip-punctuation [text]
  (str/replace text #"\p{Punct}" ""))

(defn- strip-and-lower [s]
  (->> (strip-punctuation s)
       str/lower-case))

;;Naive search
(defn recipe-name-and-ingredients
  "A basic search over either the recipe name or ingredients for the exact search term.
  If the search term is found in the recipe name it scores a higher relevancy score"
  [term
   {:keys [recipe-name ingredients] :as recipe}]
  (let [pattern (re-pattern (strip-and-lower term))]
    (cond
      (re-find pattern (strip-and-lower recipe-name))
      (assoc recipe :found :recipe-name
                    :relevancy 2)

      (re-find pattern ingredients)
      (assoc recipe :found :ingredients
                    :relevancy 1)
      :else nil)))

(def simple-stop-words #{"a" "the" "and" "g" "or" "of" "with" "to" "for" "in"
                         "this" "is" "you" "it" "as" "if" "into" "its" "are"
                         "at" "your" "by" "so"})

(defn- recipe->words
  [{:keys [introduction ingredients]}]
  (-> (str introduction " " ingredients)
      strip-and-lower
      (str/split #" ")))

(defn- recipe->normalized-words
  "This extracts the introduction and ingredients and returns them cleaned"
  [recipe]
  (let [words (recipe->words recipe)]
    (->> (map #(str/replace % #"[^a-zA-Z]" "") words)
         (remove #(contains? simple-stop-words %))
         (remove empty?))))

(defn- exact-term-match?
  [match recipe-name]
  (and match
       (= (count match) (count recipe-name))))

(defn- strip-and-lower-refind
  "Strips punctuation and lower cases the term and recipe name before searching."
  [term recipe-name]
  (re-find (re-pattern (strip-and-lower term))
           (strip-and-lower recipe-name)))

(defn- any-term-recipe-name-match
  "Tries to match any part of the search term to the recipe name,
  giving a score of 1 for each matched term."
  [term recipe-name]
  (let [terms (str/split term #" ")]
    (mapv (fn [term] (when (strip-and-lower-refind term recipe-name)) 1) terms)))

(defn- recipe-name-match
  "Tries to find the search term in the recipe name.
  An exact match gives a higher relevancy score,
  a partial match a slightly lower score."
  [term recipe-name]
  (let [match (strip-and-lower-refind term recipe-name)
        exact-match-score 5
        partial-match-score 3]
    (cond
      (exact-term-match? match recipe-name) exact-match-score
      match partial-match-score
      :else nil)))

(defn- recipe-name-scorer
  [term {:keys [recipe-name] :as recipe}]
  (let [recipe-name-match (recipe-name-match term recipe-name)]
    (conj (any-term-recipe-name-match term recipe)
          recipe-name-match)))

(defn- relevancy-score
  [word-freq name-match]
  (->> (concat word-freq name-match)
       (remove nil?)
       (apply +)))

;;Slower word frequency search
(defn word-frequency
  "Gives a recipe a relevancy score in relation to the search term.
  The frequency the search term(s) appears in the introduction and ingredients will increase the relevancy score.
  The search term is also matched on the recipe name giving higher relevancy scores for exact matches."
  [term recipe]
  (let [terms (str/split (strip-and-lower term) #" ")
        word-freq (-> (recipe->normalized-words recipe)
                      frequencies)
        word-freq-score (mapv #(get word-freq %) terms)
        name-match-score (recipe-name-scorer term recipe)
        relevancy-score  (relevancy-score word-freq-score name-match-score)]
    (assoc recipe :relevancy relevancy-score)))

