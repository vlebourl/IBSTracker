## Completed ✅

* ~~reorganize food quick add categorization. it should represent actual food categories and not IBS related categories, the IBS "impact" should be a hidden attribute that can be used in the analysis, not a quick category.~~ **[Completed in v1.9.0 - Smart Food Categorization with 12 categories and hidden IBS attributes]**
* ~~Fix all deprecation warnings (30 warnings across 7 files)~~ **[Completed in branch 002-fix-deprecation-warnings - All 30 external deprecation warnings eliminated, build time improved 24.5%]**

## TODO

* **REGRESSION**: Re-implement quick add row for frequent food items - Was implemented in commit 0cc28ef but removed during Smart Food Categorization refactor (0353309). Components still exist (QuickAddSection.kt, QuickAddCard.kt) but are not integrated in FoodScreen.kt. Need to restore the feature.
* refactor the symptom page to match the style and functionalities of the food page.
* run a global analysis of style and UI/UX and create a plan to realign on latest material design recommendations.
* add a medication part in the "symptoms" tab, maybe rename the tab? the medication could indicate taking stuff like omeprazole, Gaviscon, lopéramide, etc (you may suggest others)
* automatic daily backup