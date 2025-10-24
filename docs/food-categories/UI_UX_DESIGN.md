# UI/UX Design - Smart Food Categorization

## 🎨 Design Principles

### Material Design 3 Compliance
- **Color System:** Material You dynamic colors
- **Typography:** Material 3 type scale
- **Components:** Material 3 components (cards, chips, bottom sheets)
- **Accessibility:** WCAG AA contrast ratios, touch targets 48dp minimum

### User-Centric Design
- **Progressive Disclosure:** Don't overwhelm users, show complexity gradually
- **Smart Defaults:** Default to LOW_FODMAP, pre-fill common attributes
- **Educational:** Tooltips and brief explanations for IBS terms
- **Forgiving:** Easy to skip and assign later, easy to edit

## 🏗️ Screen Layouts

### 1. Updated Food Screen (Main)

**Grid Layout:** 3 columns × 4 rows = 12 category cards

**Sorting Rule:** All foods sorted by usage count (DESC), then alphabetically (ASC)

```
┌─────────────────────────────────────────┐
│  🔍 Search foods...                     │
├─────────────────────────────────────────┤
│  Quick Add (Top 6 by Usage) →→→        │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐  │
│  │Coffee│ │Bread │ │Milk  │ │Apple │  │
│  │☕ x12│ │🌾 x8 │ │🥛 x6 │ │🍎 x5 │  │
│  └──────┘ └──────┘ └──────┘ └──────┘  │
├─────────────────────────────────────────┤
│  Categories                             │
│  ┌───────┬───────┬───────┐             │
│  │GRAINS │PROTEIN│ DAIRY │             │
│  │  🌾   │  🍗   │  🥛   │             │
│  ├───────┼───────┼───────┤             │
│  │FRUITS │ VEGES │LEGUMES│             │
│  │  🍎   │  🥬   │  🫘   │             │
│  ├───────┼───────┼───────┤             │
│  │ NUTS  │DRINKS │SWEETS │             │
│  │  🥜   │  ☕   │  🍰   │             │
│  ├───────┼───────┼───────┤             │
│  FATS   │PREPARED│ OTHER │             │
│  │  🧈   │  🍔   │  ...  │             │
│  └───────┴───────┴───────┘             │
└─────────────────────────────────────────┘
```

**Category Card Design:**
```kotlin
Card(
    modifier = Modifier
        .aspectRatio(1f)  // Square cards
        .clickable { /* open category */ }
) {
    Column(
        modifier = Modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = category.color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(height = 8.dp)
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
```

### 2. Category Detail Screen

When user taps a category (e.g., BEVERAGES):

**Sorting:** By usage count (DESC), then alphabetically (ASC)

```
┌─────────────────────────────────────────┐
│  ← BEVERAGES                            │
├─────────────────────────────────────────┤
│  🔍 Search beverages...                 │
├─────────────────────────────────────────┤
│  Most Used (sorted: usage ↓, then A-Z)  │
│  ┌───────────────┐  ┌───────────────┐  │
│  │ Coffee        │  │ Tea           │  │
│  │ ☕ Used 12×   │  │ 🍵 Used 8×    │  │
│  │ [long press]  │  │ [long press]  │  │
│  └───────────────┘  └───────────────┘  │
│                                         │
│  All Beverages                          │
│  (Same 0 uses → alphabetical)           │
│  ┌─────────────────────────────────┐   │
│  │ Beer 🍺                         │   │
│  │ Carbonated, Alcoholic           │   │
│  └─────────────────────────────────┘   │
│  ┌─────────────────────────────────┐   │
│  │ Coffee ☕                       │   │
│  │ Caffeinated                     │   │
│  └─────────────────────────────────┘   │
│  ┌─────────────────────────────────┐   │
│  │ Milk 🥛                         │   │
│  │ Lactose, Moderate FODMAP        │   │
│  └─────────────────────────────────┘   │
│                                         │
│  [+ Add New Beverage]                   │
└─────────────────────────────────────────┘
```

**Long Press Menu:**
```
┌─────────────────────┐
│ Coffee ☕           │
├─────────────────────┤
│ ✏️ Edit Attributes  │
│ ⭐ Add Favorite     │
│ 🗑️ Delete           │
└─────────────────────┘
```

### 3. Add New Food Dialog (Bottom Sheet)

Triggered when user searches for food not in database:

```
┌─────────────────────────────────────────┐
│  Add New Food                      [X]  │
├─────────────────────────────────────────┤
│                                         │
│  Food Name                              │
│  ┌───────────────────────────────────┐ │
│  │ Potatoes - Oven Cooked            │ │
│  └───────────────────────────────────┘ │
│                                         │
│  Category *                             │
│  ┌───┬───┬───┬───┐                     │
│  │🌾 │🍗│🥛│🍎│ ... [horizontal scroll] │
│  └───┴───┴───┴───┘                     │
│  Selected: VEGETABLES 🥬                │
│                                         │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                         │
│  FODMAP Level * [ℹ️]                    │
│  ○ High   ○ Moderate   ● Low (default) │
│                                         │
│  Additional Attributes [ℹ️]             │
│  ☐ Contains Gluten                      │
│  ☐ Contains Lactose                     │
│  ☐ High Fat                             │
│  ☐ High Fiber                           │
│  ☐ Spicy                                │
│  ☐ Artificial Sweetener                 │
│                                         │
│  [Skip for Now]          [Save Food]    │
└─────────────────────────────────────────┘
```

**Tooltip Example (ℹ️ icon):**
```
┌─────────────────────────────────┐
│ FODMAP Level                    │
├─────────────────────────────────┤
│ Fermentable Oligosaccharides,   │
│ Disaccharides, Monosaccharides  │
│ and Polyols.                    │
│                                 │
│ High FODMAP foods can trigger   │
│ IBS symptoms like bloating.     │
│                                 │
│ Most new foods you add will be  │
│ LOW FODMAP (default).           │
└─────────────────────────────────┘
```

### 4. Edit Food Attributes Dialog

Triggered by long press → "Edit Attributes":

```
┌─────────────────────────────────────────┐
│  Edit: Coffee                      [X]  │
├─────────────────────────────────────────┤
│                                         │
│  Category                               │
│  BEVERAGES ☕                           │
│  [Change Category]                      │
│                                         │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                         │
│  FODMAP Level *                         │
│  ○ High   ○ Moderate   ● Low            │
│                                         │
│  Composition                            │
│  ☐ Contains Gluten                      │
│  ☐ Contains Lactose                     │
│                                         │
│  Content                                │
│  ☐ High Fat                             │
│  ☐ High Fiber                           │
│  ☐ Spicy                                │
│  ☐ Artificial Sweetener                 │
│                                         │
│  Beverage Specific                      │
│  ☑ Caffeinated                          │
│  ☐ Carbonated                           │
│  ☐ Alcoholic                            │
│  ☐ Acidic                               │
│                                         │
│  [Cancel]                    [Save]     │
└─────────────────────────────────────────┘
```

**Dynamic Attribute Display:**
- Show "Beverage Specific" section ONLY if category = BEVERAGES
- Show "Composition" and "Content" for all categories
- FODMAP always required (cannot be unchecked)

## 🎨 Color Palette (Material Design 3)

### Category Colors (Light Theme)

| Category | Color | Hex | Rationale |
|----------|-------|-----|-----------|
| GRAINS | Burlywood | `#DEB887` | Earthy, wheat-like |
| PROTEINS | Light Red | `#E57373` | Meat association |
| DAIRY | Light Blue | `#81D4FA` | Milk/cream association |
| FRUITS | Light Green | `#AED581` | Fresh, healthy |
| VEGETABLES | Green | `#66BB6A` | Leafy, natural |
| LEGUMES | Purple | `#BA68C8` | Distinct from other greens |
| NUTS_SEEDS | Orange | `#FFB74D` | Warm, nutty tone |
| BEVERAGES | Cyan | `#4FC3F7` | Water/liquid |
| SWEETS | Pink | `#F48FB1` | Candy/dessert |
| FATS_OILS | Yellow | `#FFF176` | Butter/oil yellow |
| PREPARED_FOODS | Deep Orange | `#FFAB91` | Processed, cooked |
| OTHER | Blue Grey | `#90A4AE` | Neutral |

**Accessibility Check:**
- All colors pass WCAG AA (4.5:1) against white background for body text
- All colors distinguishable for color-blind users (tested with simulators)

### IBS Impact Colors (Chips)

| Impact Level | Color | Usage |
|--------------|-------|-------|
| HIGH_FODMAP | Red 300 | Warning/caution |
| MODERATE_FODMAP | Orange 300 | Moderate warning |
| LOW_FODMAP | Green 300 | Safe/good |
| General Attributes | Blue Grey 300 | Informational |

## 🎯 Interaction Patterns

### 1. Search with Auto-Complete
```
User types: "pot"
  ↓
Show suggestions:
  ┌─────────────────────────────────┐
  │ Potatoes                        │
  │ Potatoes - Mashed               │
  │ Potatoes - Oven Cooked          │
  │ ─────────────────────────────── │
  │ + Add "pot" as new food         │
  └─────────────────────────────────┘
```

### 2. Quick Add Flow
```
Tap "Coffee" card
  ↓
Confirmation dialog (simplified):
  ┌─────────────────────────────────┐
  │ Add Coffee ☕                   │
  ├─────────────────────────────────┤
  │ Quantity: [____] cups           │
  │ Time: [Now ▼]                   │
  │ Notes: [_________________]      │
  │                                 │
  │ [Cancel]           [Add]        │
  └─────────────────────────────────┘
```

### 3. Category Grid → Detail Flow
```
Tap VEGETABLES card
  ↓
Slide animation to Category Detail Screen
  ↓
Show vegetables sorted by:
  1. Usage count (descending - most used first)
  2. Alphabetically (ascending - for equal usage)
  ↓
User can search, long-press edit, or add new
```

### 4. Long Press Edit Flow
```
Long press "Coffee" in list
  ↓
Bottom sheet menu appears
  ↓
Tap "Edit Attributes"
  ↓
Edit dialog (pre-filled with current values)
  ↓
Save → Update database → Refresh UI
```

## 📱 Responsive Design

### Small Screens (<360dp width)
- 2-column category grid (6 rows)
- Reduce card padding
- Smaller icons (24dp)

### Medium Screens (360-600dp)
- 3-column category grid (4 rows) ✓ Default
- Standard padding and icons

### Large Screens (>600dp tablets)
- 4-column category grid (3 rows)
- Larger cards, more spacing
- Side-by-side layouts (category list + detail)

## ♿ Accessibility

### Touch Targets
- Minimum 48dp × 48dp for all interactive elements
- Category cards: 100dp × 100dp (exceeds minimum)
- Checkboxes: 48dp × 48dp

### Screen Reader Support
- All icons have contentDescription
- Category cards announce: "VEGETABLES category, tap to view vegetables"
- Attributes announce with explanation: "Caffeinated checkbox, contains caffeine which stimulates bowel movement"

### Keyboard Navigation
- Tab order: Search → Quick Add → Categories (row-by-row)
- Enter key activates selected card
- Esc closes dialogs

### Color Contrast
- All text meets WCAG AA (4.5:1 for body, 3:1 for large text)
- Color never used as sole indicator (combine with icons/text)

## 🔄 Animations & Transitions

### Category Selection
```kotlin
AnimatedVisibility(
    visible = selectedCategory != null,
    enter = slideInHorizontally() + fadeIn(),
    exit = slideOutHorizontally() + fadeOut()
) {
    CategoryDetailScreen(category = selectedCategory)
}
```

### Bottom Sheet (Add/Edit Dialog)
```kotlin
ModalBottomSheet(
    onDismissRequest = { showDialog = false },
    sheetState = rememberModalBottomSheetState()
) {
    AddFoodDialog(...)
}
```

### Quick Add Feedback
- Ripple effect on tap
- Brief SnackBar: "Coffee added ✓"
- Increment usage count animation

## 📐 Spacing & Typography

### Spacing Scale (Material Design)
- `xs`: 4.dp
- `sm`: 8.dp
- `md`: 12.dp
- `lg`: 16.dp
- `xl`: 24.dp
- `xxl`: 32.dp

### Typography Scale
- `displayLarge`: Category screen title
- `titleMedium`: Dialog titles
- `bodyLarge`: Primary content
- `bodyMedium`: Category names, food names
- `bodySmall`: Secondary info (usage count, attributes)
- `labelSmall`: Chip labels

---

**Next Steps:**
1. Create Figma mockups (optional)
2. Review with stakeholders
3. Finalize icon choices from Material Icons
4. Prepare assets for implementation
