(ns recipe-search.core
  (:require [clojure.string :as str]
            [recipe-search.search :as search]))

;;Loading the recipes from resources or elsewhere
(defn- resources->string-recipes
  "Any recipes stored in resources will be turned into string recipes"
  []
  (let [files (file-seq (clojure.java.io/file "resources/recipes"))
        file-paths (map #(.getPath %) files)]
    (mapv slurp (drop 2 file-paths))))

(defn- load-recipes
  [path]
  (let [files (file-seq (clojure.java.io/file path))
        file-paths (map #(.getPath %) files)]
    (mapv slurp (drop 1 file-paths))))

(def string-recipes (resources->string-recipes))

;; Functions for converting string recipes to internal data structure
(defn- get-or-empty-str [v n]
  (get v n ""))

(defn- string-recipe->recipe
  [s]
  (when s
    (let [split-recipe (str/split-lines s)]
      {:recipe-name (get-or-empty-str split-recipe 0)
       :introduction (get-or-empty-str split-recipe 2)
       :ingredients (get-or-empty-str split-recipe 4)
       :method (get-or-empty-str split-recipe 6)})))

(defn- valid-recipe?
  "A recipe needs a name, ingredients and method for it to be valid."
  [{:keys [recipe-name ingredients method]}]
  (and (not-empty recipe-name)
       (not-empty ingredients)
       (not-empty method)))

(def valid-recipes
  (->> (map string-recipe->recipe string-recipes)
       (filter valid-recipe?)))

;;Different ways to search for recipes with basic relevancy sort and limit
(defn search-resource-recipes
  "This function assumes you would have parsed the existing recipes and are only searching on the 'valid' ones."
  [term limit]
  (->> valid-recipes
       (keep #(search/recipe-name-and-ingredients term %))
       (sort-by :relevancy >)
       (take limit)))

(defn search-new-recipes
  "This function allows you to point to 'new' recipes and then search them."
  [path term limit]
  (->> (map string-recipe->recipe (load-recipes path))
       (filter valid-recipe?)
       (keep #(search/recipe-name-and-ingredients term %))
       (sort-by :relevancy >)
       (take limit)))

(defn word-frequency-search
  [term limit]
  (->> valid-recipes
       (keep #(search/word-frequency term %))
       (sort-by :relevancy >)
       (take limit)))