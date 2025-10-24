## Completed ✅

* ~~create a single row quick add on top of the food categories with the 2, 3 or 4 most added items, this short list is "dynamical" and recomputed from food history each time a new item is added, and sorted from most added to least one. arrange in a single row, with relevant icon. don't add an item if it's only seen a single time in the food history.~~ **[Completed - Regression fixed in branch 002-fix-deprecation-warnings (commit b0ef404)]**
* ~~reorganize food quick add categorization. it should represent actual food categories and not IBS related categories, the IBS "impact" should be a hidden attribute that can be used in the analysis, not a quick category.~~ **[Completed in v1.9.0 - Smart Food Categorization with 12 categories and hidden IBS attributes]**
* ~~Fix all deprecation warnings (30 warnings across 7 files)~~ **[Completed in branch 002-fix-deprecation-warnings - All 30 external deprecation warnings eliminated, build time improved 24.5%]**

## TODO

* refactor the symptom page to match the style and functionalities of the food page.
* run a global analysis of style and UI/UX and create a plan to realign on latest material design recommendations.
* add a medication part in the "symptoms" tab, 
* fix colors in food selection once a category is selected, it's not pretty.
* use swipe left to edit and swipe right to remove with confirmation. implement it the same way as in many email apps, swiping one way reveals the icon corresponding to the action to do, and if swipped to the side enough, action is indeed triggered
* 