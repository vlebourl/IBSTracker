-- IBS Tracker Test Data Population Script
-- This script resets the database and creates realistic IBS-related test data
-- Run this on the emulator database to test the analytics features

-- Clear all existing data
DELETE FROM FoodItem;
DELETE FROM Symptom;
DELETE FROM sqlite_sequence WHERE name IN ('FoodItem', 'Symptom');

-- Realistic test data spanning 45 days
-- This creates patterns that will show in the analysis:
-- 1. Dairy (Lait/Fromage) → Ballonnements (High correlation)
-- 2. Gluten (Pain/Pâtes) → Douleurs abdominales (Medium-High correlation)
-- 3. High FODMAP foods (Oignons/Ail) → Gaz (High correlation)
-- 4. Café → Diarrhée (Medium correlation)
-- 5. Alcool → Multiple symptoms (Lower correlation)
-- 6. Épices → Douleurs (Medium correlation)

-- Day 1-5: Establishing baseline patterns
-- Day 1: Dairy triggers
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Lait', '1 verre', 'Dairy', datetime('now', '-45 days', '08:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pain blanc', '2 tranches', 'Grains', datetime('now', '-45 days', '12:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Fromage', '50g', 'Dairy', datetime('now', '-45 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 7, datetime('now', '-45 days', '11:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 5, datetime('now', '-45 days', '21:00'));

-- Day 2: Gluten triggers
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Café', '1 tasse', 'Beverages', datetime('now', '-44 days', '07:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pâtes', '200g', 'Grains', datetime('now', '-44 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pain complet', '3 tranches', 'Grains', datetime('now', '-44 days', '19:30'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 4, datetime('now', '-44 days', '09:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 6, datetime('now', '-44 days', '14:30'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 5, datetime('now', '-44 days', '21:00'));

-- Day 3: FODMAP triggers
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Oignons', '1 petit', 'Vegetables', datetime('now', '-43 days', '12:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Ail', '2 gousses', 'Vegetables', datetime('now', '-43 days', '12:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Haricots', '150g', 'Legumes', datetime('now', '-43 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 8, datetime('now', '-43 days', '15:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 7, datetime('now', '-43 days', '16:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 5, datetime('now', '-43 days', '21:30'));

-- Day 4: Safe foods (low symptoms)
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Riz blanc', '200g', 'Grains', datetime('now', '-42 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Poulet', '150g', 'Proteins', datetime('now', '-42 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Carottes', '100g', 'Vegetables', datetime('now', '-42 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 2, datetime('now', '-42 days', '20:00'));

-- Day 5: Coffee and spicy food
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Café', '2 tasses', 'Beverages', datetime('now', '-41 days', '08:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Plat épicé', '1 portion', 'Spicy Foods', datetime('now', '-41 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 6, datetime('now', '-41 days', '10:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 7, datetime('now', '-41 days', '21:00'));

-- Day 6-10: Repeating patterns to strengthen correlations
-- Day 6: Dairy again
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Yaourt', '1 pot', 'Dairy', datetime('now', '-40 days', '09:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Fromage', '80g', 'Dairy', datetime('now', '-40 days', '20:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 8, datetime('now', '-40 days', '11:30'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 6, datetime('now', '-40 days', '22:00'));

-- Day 7: Mixed day
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pain blanc', '2 tranches', 'Grains', datetime('now', '-39 days', '08:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Salade', '1 bowl', 'Vegetables', datetime('now', '-39 days', '12:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Poisson', '150g', 'Proteins', datetime('now', '-39 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 4, datetime('now', '-39 days', '10:00'));

-- Day 8: FODMAP triggers again
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Oignons', '1 moyen', 'Vegetables', datetime('now', '-38 days', '13:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pomme', '1 fruit', 'Fruits', datetime('now', '-38 days', '16:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 7, datetime('now', '-38 days', '15:30'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 6, datetime('now', '-38 days', '18:00'));

-- Day 9: Gluten pattern
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pâtes', '250g', 'Grains', datetime('now', '-37 days', '12:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pain complet', '2 tranches', 'Grains', datetime('now', '-37 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 7, datetime('now', '-37 days', '15:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 5, datetime('now', '-37 days', '21:00'));

-- Day 10: Coffee pattern
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Café', '3 tasses', 'Beverages', datetime('now', '-36 days', '08:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 7, datetime('now', '-36 days', '10:30'));

-- Days 11-20: Continue establishing strong patterns
-- Day 11
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Lait', '1 verre', 'Dairy', datetime('now', '-35 days', '08:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Riz blanc', '180g', 'Grains', datetime('now', '-35 days', '13:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 7, datetime('now', '-35 days', '10:30'));

-- Day 12
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Ail', '3 gousses', 'Vegetables', datetime('now', '-34 days', '19:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Haricots', '200g', 'Legumes', datetime('now', '-34 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 9, datetime('now', '-34 days', '21:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 8, datetime('now', '-34 days', '22:00'));

-- Day 13
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Fromage', '100g', 'Dairy', datetime('now', '-33 days', '20:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 6, datetime('now', '-33 days', '22:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 5, datetime('now', '-33 days', '23:00'));

-- Day 14
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pâtes', '200g', 'Grains', datetime('now', '-32 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pain blanc', '3 tranches', 'Grains', datetime('now', '-32 days', '19:30'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 8, datetime('now', '-32 days', '14:30'));

-- Day 15
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Café', '2 tasses', 'Beverages', datetime('now', '-31 days', '07:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Plat épicé', '1 portion', 'Spicy Foods', datetime('now', '-31 days', '18:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 5, datetime('now', '-31 days', '09:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 6, datetime('now', '-31 days', '20:00'));

-- Day 16: Safe foods
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Riz blanc', '200g', 'Grains', datetime('now', '-30 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Poulet', '180g', 'Proteins', datetime('now', '-30 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Carottes', '150g', 'Vegetables', datetime('now', '-30 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 3, datetime('now', '-30 days', '21:00'));

-- Day 17
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Oignons', '1 gros', 'Vegetables', datetime('now', '-29 days', '12:30'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 8, datetime('now', '-29 days', '14:30'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 7, datetime('now', '-29 days', '15:00'));

-- Day 18
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Lait', '2 verres', 'Dairy', datetime('now', '-28 days', '08:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Yaourt', '1 pot', 'Dairy', datetime('now', '-28 days', '16:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 8, datetime('now', '-28 days', '10:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 6, datetime('now', '-28 days', '18:00'));

-- Day 19
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pain complet', '4 tranches', 'Grains', datetime('now', '-27 days', '08:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pâtes', '300g', 'Grains', datetime('now', '-27 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 7, datetime('now', '-27 days', '10:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 6, datetime('now', '-27 days', '21:00'));

-- Day 20
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Café', '3 tasses', 'Beverages', datetime('now', '-26 days', '07:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 8, datetime('now', '-26 days', '09:00'));

-- Days 21-30: More varied data
-- Day 21
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Vin rouge', '2 verres', 'Alcohol', datetime('now', '-25 days', '20:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 5, datetime('now', '-25 days', '22:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 4, datetime('now', '-25 days', '23:00'));

-- Day 22
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Haricots', '250g', 'Legumes', datetime('now', '-24 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Ail', '2 gousses', 'Vegetables', datetime('now', '-24 days', '12:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 9, datetime('now', '-24 days', '14:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 8, datetime('now', '-24 days', '15:00'));

-- Day 23
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Fromage', '120g', 'Dairy', datetime('now', '-23 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 7, datetime('now', '-23 days', '21:00'));

-- Day 24
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Plat épicé', '1 grande portion', 'Spicy Foods', datetime('now', '-22 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 8, datetime('now', '-22 days', '21:00'));

-- Day 25: Safe day
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Riz blanc', '250g', 'Grains', datetime('now', '-21 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Poisson', '200g', 'Proteins', datetime('now', '-21 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 2, datetime('now', '-21 days', '22:00'));

-- Day 26
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pâtes', '200g', 'Grains', datetime('now', '-20 days', '13:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 6, datetime('now', '-20 days', '15:00'));

-- Day 27
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Café', '2 tasses', 'Beverages', datetime('now', '-19 days', '08:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Lait', '1 verre', 'Dairy', datetime('now', '-19 days', '08:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 6, datetime('now', '-19 days', '10:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 7, datetime('now', '-19 days', '10:30'));

-- Day 28
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Oignons', '1 moyen', 'Vegetables', datetime('now', '-18 days', '12:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 7, datetime('now', '-18 days', '14:00'));

-- Day 29
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pain blanc', '3 tranches', 'Grains', datetime('now', '-17 days', '08:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Fromage', '90g', 'Dairy', datetime('now', '-17 days', '20:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 5, datetime('now', '-17 days', '10:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 6, datetime('now', '-17 days', '22:00'));

-- Day 30
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Ail', '4 gousses', 'Vegetables', datetime('now', '-16 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 8, datetime('now', '-16 days', '21:00'));

-- Days 31-45: Recent data for more patterns
-- Day 31
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Lait', '1 verre', 'Dairy', datetime('now', '-15 days', '09:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 7, datetime('now', '-15 days', '11:00'));

-- Day 32
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Café', '3 tasses', 'Beverages', datetime('now', '-14 days', '07:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 7, datetime('now', '-14 days', '09:00'));

-- Day 33
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pâtes', '250g', 'Grains', datetime('now', '-13 days', '12:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 7, datetime('now', '-13 days', '14:00'));

-- Day 34: Safe foods
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Riz blanc', '200g', 'Grains', datetime('now', '-12 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Poulet', '150g', 'Proteins', datetime('now', '-12 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 2, datetime('now', '-12 days', '21:00'));

-- Day 35
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Haricots', '200g', 'Legumes', datetime('now', '-11 days', '12:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Oignons', '1 petit', 'Vegetables', datetime('now', '-11 days', '12:30'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 8, datetime('now', '-11 days', '15:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 7, datetime('now', '-11 days', '16:00'));

-- Day 36
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Fromage', '110g', 'Dairy', datetime('now', '-10 days', '19:30'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 8, datetime('now', '-10 days', '21:30'));

-- Day 37
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Plat épicé', '1 portion', 'Spicy Foods', datetime('now', '-9 days', '18:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 7, datetime('now', '-9 days', '20:00'));

-- Day 38
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pain complet', '3 tranches', 'Grains', datetime('now', '-8 days', '08:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pâtes', '200g', 'Grains', datetime('now', '-8 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 6, datetime('now', '-8 days', '10:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 5, datetime('now', '-8 days', '21:00'));

-- Day 39
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Café', '2 tasses', 'Beverages', datetime('now', '-7 days', '08:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 6, datetime('now', '-7 days', '10:00'));

-- Day 40
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Lait', '1 verre', 'Dairy', datetime('now', '-6 days', '08:30'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Yaourt', '1 pot', 'Dairy', datetime('now', '-6 days', '16:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 8, datetime('now', '-6 days', '10:30'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 6, datetime('now', '-6 days', '18:00'));

-- Day 41
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Ail', '3 gousses', 'Vegetables', datetime('now', '-5 days', '19:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Oignons', '1 moyen', 'Vegetables', datetime('now', '-5 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Gaz', 9, datetime('now', '-5 days', '21:00'));
INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 8, datetime('now', '-5 days', '22:00'));

-- Day 42: Safe foods
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Riz blanc', '200g', 'Grains', datetime('now', '-4 days', '12:00'));
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Poisson', '180g', 'Proteins', datetime('now', '-4 days', '19:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 3, datetime('now', '-4 days', '21:00'));

-- Day 43
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Fromage', '100g', 'Dairy', datetime('now', '-3 days', '20:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Ballonnements', 7, datetime('now', '-3 days', '22:00'));

-- Day 44
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Pâtes', '250g', 'Grains', datetime('now', '-2 days', '12:30'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Douleurs abdominales', 7, datetime('now', '-2 days', '14:30'));

-- Day 45 (today)
INSERT INTO FoodItem (name, quantity, category, timestamp) VALUES
('Café', '2 tasses', 'Beverages', datetime('now', '-1 days', '08:00'));

INSERT INTO Symptom (type, intensity, timestamp) VALUES
('Diarrhée', 5, datetime('now', '-1 days', '10:00'));

-- Summary of expected correlations:
-- HIGH CONFIDENCE (70%+):
-- - Lait/Fromage/Yaourt → Ballonnements (appears ~15 times)
-- - Oignons/Ail/Haricots → Gaz (appears ~12 times)
--
-- MEDIUM-HIGH CONFIDENCE (50-70%):
-- - Pâtes/Pain → Douleurs abdominales (appears ~10 times)
-- - Café → Diarrhée (appears ~8 times)
--
-- MEDIUM CONFIDENCE (40-50%):
-- - Plat épicé → Douleurs abdominales (appears ~5 times)
--
-- LOW TRIGGERS (for contrast):
-- - Riz blanc, Poulet, Poisson, Carottes → Very low symptoms
