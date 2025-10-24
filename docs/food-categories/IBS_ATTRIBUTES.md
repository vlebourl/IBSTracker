# IBS Attributes - Detailed Guide

## 📚 Overview

This document provides detailed information about each IBS impact attribute, including:
- Medical/scientific rationale
- Common examples
- UI presentation guidelines
- User education content

## 🎯 Attribute Categories

Attributes are organized into 4 groups for better UX:

### 1. FODMAP Level (Required, Mutually Exclusive)
**UI:** Radio buttons
**Default:** LOW_FODMAP

### 2. Composition (Optional, Can Combine)
**UI:** Checkboxes
**Attributes:** GLUTEN_CONTAINING, LACTOSE_CONTAINING

### 3. Content Type (Optional, Can Combine)
**UI:** Checkboxes
**Attributes:** HIGH_FAT, HIGH_FIBER, SPICY, ARTIFICIAL_SWEETENER

### 4. Beverage Specific (Optional, Can Combine)
**UI:** Checkboxes (only shown when category = BEVERAGES)
**Attributes:** CAFFEINATED, CARBONATED, ALCOHOLIC, ACIDIC

---

## 📊 Detailed Attribute Definitions

### FODMAP Level

#### HIGH_FODMAP 🔴
**Full Name:** High FODMAP
**Description:** Foods high in Fermentable Oligosaccharides, Disaccharides, Monosaccharides, and Polyols

**Medical Rationale:**
- FODMAPs are short-chain carbohydrates poorly absorbed in small intestine
- Fermented by gut bacteria → gas, bloating, pain
- Osmotic effect → water retention in intestines → diarrhea
- Affects 70-80% of IBS sufferers

**Common Examples:**
- **Oligosaccharides:** Wheat, onions, garlic, legumes
- **Disaccharides:** Milk, yogurt, soft cheese (lactose)
- **Monosaccharides:** Apples, pears, honey, mango (fructose)
- **Polyols:** Stone fruits, mushrooms, cauliflower, artificial sweeteners

**User Tooltip:**
> "Foods high in fermentable carbs that can cause bloating, gas, and IBS symptoms. Most common IBS trigger."

**UI Color:** Red 300 (`#E57373`)

---

#### MODERATE_FODMAP 🟠
**Full Name:** Moderate FODMAP
**Description:** Medium FODMAP content, may trigger symptoms in some people

**Medical Rationale:**
- Contains moderate amounts of FODMAPs
- Tolerated in small portions by many IBS sufferers
- Portion size matters (small portion = low, large portion = high)

**Common Examples:**
- Avocado (small portions OK)
- Sweet potato
- Beetroot
- Butternut squash
- Almonds (10-12 nuts OK)

**User Tooltip:**
> "Medium FODMAP content. Often tolerated in small portions. Watch serving sizes."

**UI Color:** Orange 300 (`#FFB74D`)

---

#### LOW_FODMAP 🟢
**Full Name:** Low FODMAP
**Description:** Low in fermentable carbs, generally well tolerated

**Medical Rationale:**
- Minimal FODMAPs, unlikely to trigger symptoms
- Safe for elimination phase of low FODMAP diet
- Foundation of IBS-friendly eating

**Common Examples:**
- Rice, quinoa, oats
- Chicken, fish, eggs
- Carrots, cucumber, lettuce
- Strawberries, blueberries, oranges
- Lactose-free dairy

**User Tooltip:**
> "Low in fermentable carbs. Generally safe for IBS. Foundation of IBS-friendly diet."

**UI Color:** Green 300 (`#81C784`)

---

### Composition Attributes

#### GLUTEN_CONTAINING 🌾
**Full Name:** Contains Gluten
**Description:** Contains wheat, barley, or rye protein

**Medical Rationale:**
- Gluten = protein in wheat, barley, rye
- Different from celiac disease (autoimmune)
- ~30% of IBS patients report gluten sensitivity
- May trigger inflammation and symptoms
- Overlap with FODMAPs (wheat contains fructans)

**Common Examples:**
- Bread, pasta, cereals (wheat-based)
- Beer (barley)
- Some soy sauces
- Processed foods with wheat flour

**User Tooltip:**
> "Contains wheat, barley, or rye protein. May trigger symptoms in gluten-sensitive IBS sufferers."

**Icon:** Wheat symbol 🌾

---

#### LACTOSE_CONTAINING 🥛
**Full Name:** Contains Lactose
**Description:** Contains milk sugar (lactose)

**Medical Rationale:**
- Lactose = sugar in dairy products
- Requires lactase enzyme to digest
- ~65% of adults have reduced lactase (lactose intolerance)
- High overlap with IBS (many IBS patients are lactose intolerant)
- Lactose is also a FODMAP (disaccharide)

**Common Examples:**
- Milk (all types except lactose-free)
- Ice cream
- Soft cheeses (cream cheese, ricotta)
- Yogurt (regular, not Greek)
- Butter (small amounts usually OK)

**User Tooltip:**
> "Contains milk sugar (lactose). Common IBS trigger, especially if lactose intolerant."

**Icon:** Milk carton 🥛

---

### Content Type Attributes

#### HIGH_FAT 🍔
**Full Name:** High Fat
**Description:** High fat content, may trigger IBS-D (diarrhea)

**Medical Rationale:**
- Fat triggers gastrocolic reflex (colon contractions)
- Delays gastric emptying → bloating, discomfort
- Stimulates bile release → diarrhea in sensitive individuals
- Particularly problematic for IBS-D subtype

**Common Examples:**
- Fried foods (french fries, fried chicken)
- Fatty meats (bacon, sausage, ribeye)
- Creamy sauces, butter, cheese
- Pastries, donuts, croissants
- Fast food

**User Tooltip:**
> "High fat content. Can trigger diarrhea and bloating, especially for IBS-D."

**Icon:** Fast food 🍔

---

#### HIGH_FIBER 🌾
**Full Name:** High Fiber
**Description:** High fiber content, can help or trigger depending on type

**Medical Rationale:**
- Soluble fiber (oats, psyllium) → usually helpful, regulates bowel
- Insoluble fiber (wheat bran, vegetables) → can worsen symptoms
- Too much too fast → gas, bloating
- Benefits: helps IBS-C (constipation), bulks stool

**Common Examples:**
- **Soluble:** Oats, psyllium, chia seeds, sweet potato
- **Insoluble:** Wheat bran, corn, leafy greens, bell peppers
- Beans, lentils
- High-fiber cereals

**User Tooltip:**
> "High fiber content. Can help with constipation but may cause gas. Soluble fiber usually better tolerated."

**Icon:** Grain/fiber 🌾

---

#### SPICY 🌶️
**Full Name:** Spicy
**Description:** Contains hot spices that may irritate digestive system

**Medical Rationale:**
- Capsaicin (chili compound) stimulates pain receptors
- Can speed gut transit → diarrhea
- May increase gut sensitivity in IBS patients
- Varies widely by individual tolerance

**Common Examples:**
- Hot peppers, chili
- Spicy sauces (hot sauce, sriracha)
- Curry, kimchi
- Black pepper (large amounts)
- Wasabi, horseradish

**User Tooltip:**
> "Contains hot spices that can irritate digestive system and trigger IBS symptoms."

**Icon:** Chili pepper 🌶️

---

#### ARTIFICIAL_SWEETENER 🍬
**Full Name:** Artificial Sweetener
**Description:** Contains sorbitol, xylitol, or other sugar alcohols

**Medical Rationale:**
- Sugar alcohols (sorbitol, xylitol, mannitol) are POLYols in FODMAPs
- Poorly absorbed → osmotic diarrhea
- Fermented by gut bacteria → gas, bloating
- Found in "sugar-free" products
- Very common IBS trigger

**Common Examples:**
- Sugar-free gum, mints, candy
- Diet sodas (some)
- Sugar-free ice cream, desserts
- Some fruits naturally (apples, pears, cherries)
- Protein bars labeled "low sugar"

**User Tooltip:**
> "Contains sorbitol, xylitol, or other sugar alcohols. Common IBS trigger, causes gas and diarrhea."

**Icon:** Candy 🍬

---

### Beverage Specific Attributes

#### CAFFEINATED ☕
**Full Name:** Caffeinated
**Description:** Contains caffeine which stimulates bowel movement

**Medical Rationale:**
- Caffeine stimulates colon contractions
- Increases gastric acid production
- Can speed gut transit → diarrhea
- Problematic for IBS-D, may help IBS-C
- Also has diuretic effect

**Common Examples:**
- Coffee (espresso, drip, cold brew)
- Tea (black, green, oolong)
- Energy drinks
- Colas, some sodas
- Chocolate (small amounts)

**User Tooltip:**
> "Contains caffeine which stimulates bowel movement. May trigger diarrhea in IBS-D."

**Icon:** Coffee ☕

---

#### CARBONATED 🫧
**Full Name:** Carbonated
**Description:** Fizzy drinks that can cause gas and bloating

**Medical Rationale:**
- CO2 bubbles introduce gas into digestive system
- Can increase bloating, distension
- May trigger belching, discomfort
- Combines poorly with other triggers (sugar, caffeine)

**Common Examples:**
- Sodas (Coke, Pepsi, Sprite)
- Sparkling water
- Beer
- Champagne, prosecco
- Kombucha

**User Tooltip:**
> "Fizzy drinks introduce gas into digestive system. Can increase bloating and discomfort."

**Icon:** Bubbles 🫧

---

#### ALCOHOLIC 🍺
**Full Name:** Alcoholic
**Description:** Contains alcohol which can trigger IBS symptoms

**Medical Rationale:**
- Alcohol irritates gut lining
- Speeds gut transit → diarrhea
- Disrupts gut microbiome
- Impairs nutrient absorption
- Often combined with other triggers (sugar, carbonation)

**Common Examples:**
- Beer (also gluten, carbonation)
- Wine (also acidic, sulfites)
- Spirits (vodka, whiskey, rum)
- Cocktails (often sugary)
- Hard seltzers

**User Tooltip:**
> "Contains alcohol which can irritate gut and trigger IBS symptoms. Often combined with other triggers."

**Icon:** Beer 🍺

---

#### ACIDIC 🍋
**Full Name:** Acidic
**Description:** High acidity (citrus, vinegar) may irritate stomach

**Medical Rationale:**
- Low pH can irritate stomach lining
- May trigger acid reflux (often co-occurs with IBS)
- Can increase stomach acid production
- Individual tolerance varies widely

**Common Examples:**
- Citrus juices (orange, grapefruit, lemon)
- Tomato juice
- Vinegar-based drinks
- Some wines
- Kombucha

**User Tooltip:**
> "High acidity may irritate stomach and trigger reflux. Common in IBS patients with GERD."

**Icon:** Lemon 🍋

---

## 🎨 UI Presentation Guidelines

### Attribute Display Order

**In "Add New Food" dialog:**
1. FODMAP Level (required) - always first
2. Composition (if relevant)
3. Content Type (if relevant)
4. Beverage Specific (ONLY if category = BEVERAGES)

**In food list/details:**
- Show only assigned attributes
- FODMAP level as colored chip
- Other attributes as small text/chips below

### Smart Hiding/Showing

```kotlin
when (selectedCategory) {
    FoodCategory.BEVERAGES -> {
        // Show all 4 sections including Beverage Specific
        showComposition = true
        showContent = true
        showBeverage = true
    }
    FoodCategory.GRAINS -> {
        // Grains often have gluten/fiber
        showComposition = true  // Gluten checkbox prominent
        showContent = true      // High Fiber checkbox prominent
        showBeverage = false
    }
    FoodCategory.DAIRY -> {
        // Dairy often has lactose/fat
        showComposition = true  // Lactose checkbox prominent
        showContent = true      // High Fat checkbox
        showBeverage = false
    }
    // ... etc
}
```

### Tooltip Triggers

- Info icon (ℹ️) next to each section header
- Long press on attribute checkbox shows detailed explanation
- First time user adds food → show brief tutorial overlay

### Accessibility

- Screen readers announce full name + description
- Color-blind safe (never rely on color alone)
- Keyboard navigation: Tab through attributes, Space to check/uncheck

---

## 📋 Common Combinations

### Typical Patterns

| Food Example | Category | FODMAP | Other Attributes |
|--------------|----------|--------|------------------|
| Coffee | BEVERAGES | Low | Caffeinated, Acidic |
| Milk | DAIRY | High | Lactose |
| Bread (wheat) | GRAINS | High | Gluten, High Fiber |
| Apple | FRUITS | High | - |
| French Fries | PREPARED_FOODS | Low | High Fat |
| Beans | LEGUMES | High | High Fiber |
| Beer | BEVERAGES | Moderate | Gluten, Carbonated, Alcoholic |
| Ice Cream | SWEETS | High | Lactose, High Fat |

### Mutually Exclusive Combinations

These combinations are rare/impossible (helps validation):

- ALCOHOLIC + SWEETS category (alcohol is in BEVERAGES)
- GLUTEN + FRUITS category (fruits don't have gluten)
- CARBONATED + FATS_OILS category (oils aren't carbonated)

---

## 🎓 User Education Strategy

### Progressive Learning

**First Food Entry:**
- Show brief tutorial: "Let's categorize this food!"
- Explain FODMAP in simple terms
- Suggest skipping detailed attributes if unsure

**After 5 Foods:**
- Show tip: "Did you know you can edit any food's attributes by long-pressing?"

**After 10 Foods:**
- Show stats: "You've added 10 foods! 70% are low FODMAP - great job!"

### In-App Help

- "?" icon in top bar → IBS Attributes guide (this document, simplified)
- Link to Monash FODMAP app for detailed info
- Glossary of terms

---

**Next Steps:**
1. Review attribute list with medical advisor (optional)
2. Finalize tooltips/descriptions for user-friendliness
3. Create icon assets for each attribute
4. Prepare tutorial/onboarding flow
