## Usage
1. Start REPL
2. Load core.clj
3. Call one of `search-resource-recipes`, `search-new-recipes` or `word-frequency-search`.
4. See returned recipes as maps with recipe-name, introduction, ingredients, method and relevancy score.

Example calls

`(search-resource-recipes "roast lamb" 5)`

This searches over the recipes in resources and gives you back the top 5

`(word-frequency-search "roast lamb" 5)`

This uses a slower "word frequency" search to give a different way to search recipes.

## TODO
- How to handle incoming recipes in a better way?
- Do we need to dedupe recipes?
- Is there a better way to clean the incoming data so it's easier to search?
- What other search strategies can we use to make it faster and more relevant?
- What to do with half finished recipes?

Given more time and domain knowledge there's a lot more fun and interesting things we could do to make recipe search,
more relevant to potential users.