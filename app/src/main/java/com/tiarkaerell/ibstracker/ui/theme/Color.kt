package com.tiarkaerell.ibstracker.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
val md_theme_light_primary = Color(0xFF546E41)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFD7F5BD)
val md_theme_light_onPrimaryContainer = Color(0xFF122104)
val md_theme_light_secondary = Color(0xFF57624A)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFDBE7C8)
val md_theme_light_onSecondaryContainer = Color(0xFF151E0B)
val md_theme_light_tertiary = Color(0xFF396660)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFBCECE4)
val md_theme_light_onTertiaryContainer = Color(0xFF00201D)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFCFDF7)
val md_theme_light_onBackground = Color(0xFF1A1C18)
val md_theme_light_surface = Color(0xFFFCFDF7)
val md_theme_light_onSurface = Color(0xFF1A1C18)
val md_theme_light_surfaceVariant = Color(0xFFE0E4D6)
val md_theme_light_onSurfaceVariant = Color(0xFF44483E)
val md_theme_light_outline = Color(0xFF74796D)
val md_theme_light_inverseOnSurface = Color(0xFFF1F1EA)
val md_theme_light_inverseSurface = Color(0xFF2F312D)
val md_theme_light_inversePrimary = Color(0xFFBADC_A2)

// Dark Theme Colors
val md_theme_dark_primary = Color(0xFFBADC_A2)
val md_theme_dark_onPrimary = Color(0xFF273417)
val md_theme_dark_primaryContainer = Color(0xFF3D552B)
val md_theme_dark_onPrimaryContainer = Color(0xFFD7F5BD)
val md_theme_dark_secondary = Color(0xFFBFCBAD)
val md_theme_dark_onSecondary = Color(0xFF29331F)
val md_theme_dark_secondaryContainer = Color(0xFF3F4A34)
val md_theme_dark_onSecondaryContainer = Color(0xFFDBE7C8)
val md_theme_dark_tertiary = Color(0xFFA0D0C8)
val md_theme_dark_onTertiary = Color(0xFF013732)
val md_theme_dark_tertiaryContainer = Color(0xFF1F4E48)
val md_theme_dark_onTertiaryContainer = Color(0xFFBCECE4)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF1A1C18)
val md_theme_dark_onBackground = Color(0xFFE3E3DC)
val md_theme_dark_surface = Color(0xFF1A1C18)
val md_theme_dark_onSurface = Color(0xFFE3E3DC)
val md_theme_dark_surfaceVariant = Color(0xFF44483E)
val md_theme_dark_onSurfaceVariant = Color(0xFFC4C8BB)
val md_theme_dark_outline = Color(0xFF8E9286)
val md_theme_dark_inverseOnSurface = Color(0xFF1A1C18)
val md_theme_dark_inverseSurface = Color(0xFFE3E3DC)
val md_theme_dark_inversePrimary = Color(0xFF546E41)

// ============================================================================
// Extended Category Colors (Material Design 3 Extended Palette)
// For Smart Food Categorization System (v1.9.0)
// ============================================================================

// VEGETABLES - Green
val CategoryGreenLight = Color(0xFF4CAF50)      // MD3 Green 500
val CategoryGreenDark = Color(0xFF2E7D32)       // MD3 Green 800

// FRUITS - Orange
val CategoryOrangeLight = Color(0xFFFF9800)     // MD3 Orange 500
val CategoryOrangeDark = Color(0xFFE65100)      // MD3 Orange 900

// LEGUMES - Brown
val CategoryBrownLight = Color(0xFF795548)      // MD3 Brown 500
val CategoryBrownDark = Color(0xFF4E342E)       // MD3 Brown 800

// NUTS_SEEDS - Amber
val CategoryAmberLight = Color(0xFFFFC107)      // MD3 Amber 500
val CategoryAmberDark = Color(0xFFFF8F00)       // MD3 Amber 800

// BEVERAGES - Blue
val CategoryBlueLight = Color(0xFF2196F3)       // MD3 Blue 500
val CategoryBlueDark = Color(0xFF1565C0)        // MD3 Blue 800

// FATS_OILS - Yellow/Gold (darker for better readability)
val CategoryYellowLight = Color(0xFFFFA726)     // MD3 Orange 400 (gold tone, more readable)
val CategoryYellowDark = Color(0xFFFF8F00)      // MD3 Amber 800

// SWEETS - Pink
val CategoryPinkLight = Color(0xFFE91E63)       // MD3 Pink 500
val CategoryPinkDark = Color(0xFFAD1457)        // MD3 Pink 800

// PROCESSED - Red
val CategoryRedLight = Color(0xFFF44336)        // MD3 Red 500
val CategoryRedDark = Color(0xFFC62828)         // MD3 Red 800

// OTHER - Neutral Gray
val CategoryNeutralLight = Color(0xFF9E9E9E)    // MD3 Gray 500
val CategoryNeutralDark = Color(0xFF616161)     // MD3 Gray 700

// ============================================================================
// Semantic IBS Impact Colors (Material Design 3 Extended Palette)
// For IBSImpact enum attributes
// ============================================================================

// ERROR - High risk attributes (FODMAP_HIGH, GLUTEN, LACTOSE, ALCOHOL)
val IBSErrorLight = md_theme_light_error        // Red 500
val IBSErrorDark = md_theme_dark_error          // Red 400

// WARNING - Moderate risk attributes (FODMAP_MODERATE, CAFFEINE, SPICY, FATTY, ACIDIC)
val IBSWarningLight = Color(0xFFFFA726)         // MD3 Orange 400
val IBSWarningDark = Color(0xFFFF9800)          // MD3 Orange 500

// SUCCESS - Low risk attributes (FODMAP_LOW)
val IBSSuccessLight = Color(0xFF66BB6A)         // MD3 Green 400
val IBSSuccessDark = Color(0xFF4CAF50)          // MD3 Green 500
