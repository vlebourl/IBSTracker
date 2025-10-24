package com.tiarkaerell.ibstracker.util

import com.tiarkaerell.ibstracker.data.model.CommonFood
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.IBSImpact
import java.util.Date

/**
 * Pre-populated common foods database (72 foods total - 6 per category).
 *
 * This file contains verified foods covering all 12 categories with:
 * - French-focused food selection
 * - Bilingual names (French/English)
 * - IBS impact attributes (including FODMAP levels)
 * - Search terms for fuzzy matching
 *
 * Usage: Called during Migration_2_9 to populate the common_foods table.
 */
object PrePopulatedFoods {

    /**
     * All pre-populated foods with verified IBS attributes.
     * Sorted by category - 6 foods each (72 total).
     */
    val foods: List<CommonFood> = listOf(

        // ==================== GRAINS (6 foods) ====================
        CommonFood(
            name = "Pain blanc",
            nameFr = "Pain blanc",
            nameEn = "White Bread",
            category = FoodCategory.GRAINS,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.FODMAP_MODERATE),
            searchTerms = listOf("pain blanc", "pain", "baguette", "white bread", "french bread"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Viennoiserie",
            nameFr = "Viennoiserie",
            nameEn = "Pastry",
            category = FoodCategory.GRAINS,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("viennoiserie", "croissant", "pain au chocolat", "pastry"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Pain complet",
            nameFr = "Pain complet",
            nameEn = "Whole Wheat Bread",
            category = FoodCategory.GRAINS,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("pain complet", "whole wheat", "wheat bread", "brown bread"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Pain de mie",
            nameFr = "Pain de mie",
            nameEn = "Sandwich Bread",
            category = FoodCategory.GRAINS,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.FODMAP_MODERATE),
            searchTerms = listOf("pain de mie", "sandwich bread", "brioche", "soft bread"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Pâtes",
            nameFr = "Pâtes",
            nameEn = "Pasta",
            category = FoodCategory.GRAINS,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.FODMAP_MODERATE),
            searchTerms = listOf("pâtes", "pates", "pasta", "spaghetti", "nouilles"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Riz",
            nameFr = "Riz",
            nameEn = "Rice",
            category = FoodCategory.GRAINS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("riz", "rice", "white rice", "riz blanc"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== PROTEINS (6 foods) ====================
        CommonFood(
            name = "Poulet",
            nameFr = "Poulet",
            nameEn = "Chicken",
            category = FoodCategory.PROTEINS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("poulet", "chicken", "poitrine de poulet", "chicken breast"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Bœuf",
            nameFr = "Bœuf",
            nameEn = "Beef",
            category = FoodCategory.PROTEINS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("bœuf", "boeuf", "beef", "steak", "viande"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Porc",
            nameFr = "Porc",
            nameEn = "Pork",
            category = FoodCategory.PROTEINS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("porc", "pork", "côtelette", "chop"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Jambon",
            nameFr = "Jambon",
            nameEn = "Ham",
            category = FoodCategory.PROTEINS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("jambon", "ham", "jambon blanc", "jambon cru"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Poisson",
            nameFr = "Poisson",
            nameEn = "Fish",
            category = FoodCategory.PROTEINS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("poisson", "fish", "saumon", "salmon", "thon", "tuna"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Œufs",
            nameFr = "Œufs",
            nameEn = "Eggs",
            category = FoodCategory.PROTEINS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("œufs", "oeufs", "eggs", "egg", "oeuf"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== DAIRY (6 foods) ====================
        CommonFood(
            name = "Lait",
            nameFr = "Lait",
            nameEn = "Milk",
            category = FoodCategory.DAIRY,
            ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("lait", "milk", "lait entier", "whole milk"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Yaourt",
            nameFr = "Yaourt",
            nameEn = "Yogurt",
            category = FoodCategory.DAIRY,
            ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FODMAP_MODERATE),
            searchTerms = listOf("yaourt", "yogurt", "yoghurt", "yogourt"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Fromage",
            nameFr = "Fromage",
            nameEn = "Cheese",
            category = FoodCategory.DAIRY,
            ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("fromage", "cheese", "camembert", "cheddar", "emmental"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Beurre",
            nameFr = "Beurre",
            nameEn = "Butter",
            category = FoodCategory.DAIRY,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("beurre", "butter"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Crème",
            nameFr = "Crème",
            nameEn = "Cream",
            category = FoodCategory.DAIRY,
            ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("crème", "creme", "cream", "crème fraîche", "creme fraiche"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Fromage blanc",
            nameFr = "Fromage blanc",
            nameEn = "Fresh Cheese",
            category = FoodCategory.DAIRY,
            ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FODMAP_MODERATE),
            searchTerms = listOf("fromage blanc", "fresh cheese", "cottage cheese"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== VEGETABLES (6 foods) ====================
        CommonFood(
            name = "Pommes de terre",
            nameFr = "Pommes de terre",
            nameEn = "Potatoes",
            category = FoodCategory.VEGETABLES,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("pommes de terre", "pomme de terre", "patate", "patates", "potatoes", "potato"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Tomate",
            nameFr = "Tomate",
            nameEn = "Tomato",
            category = FoodCategory.VEGETABLES,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.ACIDIC),
            searchTerms = listOf("tomate", "tomates", "tomato", "tomatoes"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Carotte",
            nameFr = "Carotte",
            nameEn = "Carrot",
            category = FoodCategory.VEGETABLES,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("carotte", "carottes", "carrot", "carrots"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Courgette",
            nameFr = "Courgette",
            nameEn = "Zucchini",
            category = FoodCategory.VEGETABLES,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("courgette", "zucchini", "squash"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Haricots verts",
            nameFr = "Haricots verts",
            nameEn = "Green Beans",
            category = FoodCategory.VEGETABLES,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("haricots verts", "green beans", "string beans"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Salade",
            nameFr = "Salade",
            nameEn = "Lettuce",
            category = FoodCategory.VEGETABLES,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("salade", "laitue", "lettuce", "salad"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== FRUITS (6 foods) ====================
        CommonFood(
            name = "Pomme",
            nameFr = "Pomme",
            nameEn = "Apple",
            category = FoodCategory.FRUITS,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("pomme", "pommes", "apple", "apples"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Banane",
            nameFr = "Banane",
            nameEn = "Banana",
            category = FoodCategory.FRUITS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("banane", "bananes", "banana", "bananas"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Orange",
            nameFr = "Orange",
            nameEn = "Orange",
            category = FoodCategory.FRUITS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.ACIDIC),
            searchTerms = listOf("orange", "oranges"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Fraise",
            nameFr = "Fraise",
            nameEn = "Strawberry",
            category = FoodCategory.FRUITS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("fraise", "fraises", "strawberry", "strawberries"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Poire",
            nameFr = "Poire",
            nameEn = "Pear",
            category = FoodCategory.FRUITS,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("poire", "poires", "pear", "pears"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Raisin",
            nameFr = "Raisin",
            nameEn = "Grapes",
            category = FoodCategory.FRUITS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("raisin", "raisins", "grapes", "grape"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== LEGUMES (6 foods) ====================
        CommonFood(
            name = "Lentilles",
            nameFr = "Lentilles",
            nameEn = "Lentils",
            category = FoodCategory.LEGUMES,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("lentilles", "lentils"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Haricots blancs",
            nameFr = "Haricots blancs",
            nameEn = "White Beans",
            category = FoodCategory.LEGUMES,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("haricots blancs", "white beans", "navy beans"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Pois chiches",
            nameFr = "Pois chiches",
            nameEn = "Chickpeas",
            category = FoodCategory.LEGUMES,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("pois chiches", "chickpeas", "garbanzo beans"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Haricots rouges",
            nameFr = "Haricots rouges",
            nameEn = "Kidney Beans",
            category = FoodCategory.LEGUMES,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("haricots rouges", "kidney beans", "red beans"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Pois cassés",
            nameFr = "Pois cassés",
            nameEn = "Split Peas",
            category = FoodCategory.LEGUMES,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("pois cassés", "pois casses", "split peas"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Fèves",
            nameFr = "Fèves",
            nameEn = "Broad Beans",
            category = FoodCategory.LEGUMES,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("fèves", "feves", "broad beans", "fava beans"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== NUTS_SEEDS (6 foods) ====================
        CommonFood(
            name = "Noix",
            nameFr = "Noix",
            nameEn = "Walnuts",
            category = FoodCategory.NUTS_SEEDS,
            ibsImpacts = listOf(IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("noix", "walnuts", "walnut"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Amandes",
            nameFr = "Amandes",
            nameEn = "Almonds",
            category = FoodCategory.NUTS_SEEDS,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH, IBSImpact.FATTY),
            searchTerms = listOf("amandes", "amande", "almonds", "almond"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Noisettes",
            nameFr = "Noisettes",
            nameEn = "Hazelnuts",
            category = FoodCategory.NUTS_SEEDS,
            ibsImpacts = listOf(IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("noisettes", "noisette", "hazelnuts", "hazelnut"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Graines de tournesol",
            nameFr = "Graines de tournesol",
            nameEn = "Sunflower Seeds",
            category = FoodCategory.NUTS_SEEDS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("graines de tournesol", "sunflower seeds", "tournesol"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Pistaches",
            nameFr = "Pistaches",
            nameEn = "Pistachios",
            category = FoodCategory.NUTS_SEEDS,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH, IBSImpact.FATTY),
            searchTerms = listOf("pistaches", "pistache", "pistachios", "pistachio"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Cacahuètes",
            nameFr = "Cacahuètes",
            nameEn = "Peanuts",
            category = FoodCategory.NUTS_SEEDS,
            ibsImpacts = listOf(IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("cacahuètes", "cacahuetes", "peanuts", "arachides"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== BEVERAGES (6 foods) ====================
        CommonFood(
            name = "Café",
            nameFr = "Café",
            nameEn = "Coffee",
            category = FoodCategory.BEVERAGES,
            ibsImpacts = listOf(IBSImpact.CAFFEINE, IBSImpact.ACIDIC, IBSImpact.FODMAP_LOW),
            searchTerms = listOf("café", "cafe", "coffee", "espresso"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Thé",
            nameFr = "Thé",
            nameEn = "Tea",
            category = FoodCategory.BEVERAGES,
            ibsImpacts = listOf(IBSImpact.CAFFEINE, IBSImpact.FODMAP_LOW),
            searchTerms = listOf("thé", "the", "tea", "thé noir", "thé vert"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Eau",
            nameFr = "Eau",
            nameEn = "Water",
            category = FoodCategory.BEVERAGES,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("eau", "water"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Vin",
            nameFr = "Vin",
            nameEn = "Wine",
            category = FoodCategory.BEVERAGES,
            ibsImpacts = listOf(IBSImpact.ALCOHOL, IBSImpact.ACIDIC, IBSImpact.FODMAP_LOW),
            searchTerms = listOf("vin", "wine", "vin rouge", "vin blanc", "red wine", "white wine"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Jus d'orange",
            nameFr = "Jus d'orange",
            nameEn = "Orange Juice",
            category = FoodCategory.BEVERAGES,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.ACIDIC),
            searchTerms = listOf("jus d'orange", "orange juice", "oj"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Lait (boisson)",
            nameFr = "Lait",
            nameEn = "Milk (beverage)",
            category = FoodCategory.BEVERAGES,
            ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FODMAP_HIGH),
            searchTerms = listOf("lait", "milk"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== SWEETS (6 foods) ====================
        CommonFood(
            name = "Chocolat",
            nameFr = "Chocolat",
            nameEn = "Chocolate",
            category = FoodCategory.SWEETS,
            ibsImpacts = listOf(IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY, IBSImpact.CAFFEINE),
            searchTerms = listOf("chocolat", "chocolate", "chocolat noir", "dark chocolate"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Tarte",
            nameFr = "Tarte",
            nameEn = "Pie/Tart",
            category = FoodCategory.SWEETS,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("tarte", "pie", "tart", "tarte aux pommes", "apple pie"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Gâteau",
            nameFr = "Gâteau",
            nameEn = "Cake",
            category = FoodCategory.SWEETS,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("gâteau", "gateau", "cake"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Crêpe",
            nameFr = "Crêpe",
            nameEn = "Crepe",
            category = FoodCategory.SWEETS,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.LACTOSE, IBSImpact.FODMAP_MODERATE),
            searchTerms = listOf("crêpe", "crepe", "pancake"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Macaron",
            nameFr = "Macaron",
            nameEn = "Macaron",
            category = FoodCategory.SWEETS,
            ibsImpacts = listOf(IBSImpact.FODMAP_HIGH, IBSImpact.FATTY),
            searchTerms = listOf("macaron", "macaroon"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Mousse au chocolat",
            nameFr = "Mousse au chocolat",
            nameEn = "Chocolate Mousse",
            category = FoodCategory.SWEETS,
            ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("mousse au chocolat", "chocolate mousse", "mousse"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== FATS_OILS (6 foods) ====================
        CommonFood(
            name = "Huile d'olive",
            nameFr = "Huile d'olive",
            nameEn = "Olive Oil",
            category = FoodCategory.FATS_OILS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("huile d'olive", "olive oil", "evoo"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Beurre (matière grasse)",
            nameFr = "Beurre",
            nameEn = "Butter (fat)",
            category = FoodCategory.FATS_OILS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("beurre", "butter"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Huile de tournesol",
            nameFr = "Huile de tournesol",
            nameEn = "Sunflower Oil",
            category = FoodCategory.FATS_OILS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("huile de tournesol", "sunflower oil"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Margarine",
            nameFr = "Margarine",
            nameEn = "Margarine",
            category = FoodCategory.FATS_OILS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("margarine"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Huile de colza",
            nameFr = "Huile de colza",
            nameEn = "Rapeseed Oil",
            category = FoodCategory.FATS_OILS,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("huile de colza", "rapeseed oil", "canola oil"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Crème (matière grasse)",
            nameFr = "Crème",
            nameEn = "Cream",
            category = FoodCategory.FATS_OILS,
            ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("crème", "creme", "cream", "crème liquide", "heavy cream"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== PROCESSED (6 foods) ====================
        CommonFood(
            name = "Pizza",
            nameFr = "Pizza",
            nameEn = "Pizza",
            category = FoodCategory.PROCESSED,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.LACTOSE, IBSImpact.FODMAP_HIGH, IBSImpact.FATTY),
            searchTerms = listOf("pizza"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Quiche",
            nameFr = "Quiche",
            nameEn = "Quiche",
            category = FoodCategory.PROCESSED,
            ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.LACTOSE, IBSImpact.FODMAP_MODERATE, IBSImpact.FATTY),
            searchTerms = listOf("quiche", "quiche lorraine"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Plat préparé",
            nameFr = "Plat préparé",
            nameEn = "Ready Meal",
            category = FoodCategory.PROCESSED,
            ibsImpacts = listOf(IBSImpact.FODMAP_MODERATE),
            searchTerms = listOf("plat préparé", "plat prepare", "ready meal", "frozen dinner"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Soupe en brique",
            nameFr = "Soupe en brique",
            nameEn = "Boxed Soup",
            category = FoodCategory.PROCESSED,
            ibsImpacts = listOf(IBSImpact.FODMAP_MODERATE),
            searchTerms = listOf("soupe en brique", "soupe", "soup", "boxed soup"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Chips",
            nameFr = "Chips",
            nameEn = "Potato Chips",
            category = FoodCategory.PROCESSED,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.FATTY),
            searchTerms = listOf("chips", "potato chips", "crisps"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Jambon sous vide",
            nameFr = "Jambon sous vide",
            nameEn = "Packaged Ham",
            category = FoodCategory.PROCESSED,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("jambon sous vide", "packaged ham", "deli ham", "jambon tranché", "jambon tranche"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),

        // ==================== OTHER (6 foods) ====================
        CommonFood(
            name = "Sauce",
            nameFr = "Sauce",
            nameEn = "Sauce",
            category = FoodCategory.OTHER,
            ibsImpacts = listOf(IBSImpact.FODMAP_MODERATE),
            searchTerms = listOf("sauce"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Sel",
            nameFr = "Sel",
            nameEn = "Salt",
            category = FoodCategory.OTHER,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("sel", "salt"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Poivre",
            nameFr = "Poivre",
            nameEn = "Pepper",
            category = FoodCategory.OTHER,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.SPICY),
            searchTerms = listOf("poivre", "pepper", "black pepper"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Moutarde",
            nameFr = "Moutarde",
            nameEn = "Mustard",
            category = FoodCategory.OTHER,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("moutarde", "mustard"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Épices",
            nameFr = "Épices",
            nameEn = "Spices",
            category = FoodCategory.OTHER,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW, IBSImpact.SPICY),
            searchTerms = listOf("épices", "epices", "spices"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        ),
        CommonFood(
            name = "Herbes",
            nameFr = "Herbes",
            nameEn = "Herbs",
            category = FoodCategory.OTHER,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
            searchTerms = listOf("herbes", "herbs", "herbes de provence"),
            usageCount = 0,
            isVerified = true,
            createdAt = Date()
        )
    )

    /**
     * Get all foods for a specific category.
     */
    fun getFoodsByCategory(category: FoodCategory): List<CommonFood> {
        return foods.filter { it.category == category }
    }

    /**
     * Get count of foods by category.
     */
    fun getCategoryFoodCount(category: FoodCategory): Int {
        return foods.count { it.category == category }
    }

    /**
     * Total count of pre-populated foods.
     * Exactly 6 foods per category × 12 categories = 72 foods total.
     */
    fun getTotalFoodCount(): Int = foods.size
}
