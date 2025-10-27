# Feature Specification: Improved Analysis Insights

**Feature Branch**: `001-improve-analysis-insights`  
**Created**: 2025-10-26  
**Status**: Draft  
**Input**: User description: "improve analysis page. I want to be more explanatory. I'm not sure yet how to improve it, but so far it's not very clear. The way triggers are listed is a bit confusing. For instance in my current Meal triggers I have: \"Coffee + Cofee 0/6 times . Moderate    0%\" and I don't really know what the \"0/6 times\" mean, what the Moderate mean and what the 0% mean. based on a thorough analysis of this page, and the way other similar app may do it, let's find a better way to analyse how food and drink intake or meals (as defined at the moment) correlate with symptoms. I don't need to see when a potential trigger didn't trigger anything, but I do want to know, when a symptom is registered, what are the triggers it could be related to, given what is registered in the history of the food entries. The point is to be able at some point to say something like \"well every time you have explosive diarhea you either had a lot of coffee in the day, or ate some very fatty food (like french fondue or raclette) or had something with a lot of tomatoes (tomates farçies or pizza with tomatoes, or anything else...)\". Do you understand my point?"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Symptom-Based Trigger Analysis (Priority: P1)

As a user tracking IBS symptoms, I want to see what foods or meals might have triggered each symptom occurrence, so I can identify patterns and avoid problematic foods.

**Why this priority**: This is the core value proposition - understanding what causes symptoms is the primary reason users track their food and symptoms.

**Independent Test**: Can be fully tested by entering several symptoms and food items, then viewing the analysis to see if correlations are clearly displayed.

**Acceptance Scenarios**:

1. **Given** I have recorded symptoms and food entries in the past 30 days, **When** I view the analysis page, **Then** I see a list of my symptoms with potential food triggers for each
2. **Given** I have a symptom that occurred multiple times, **When** I view its analysis, **Then** I see patterns in the foods consumed before each occurrence
3. **Given** I select a specific symptom occurrence, **When** I view its details, **Then** I see all foods consumed within the relevant time window before that symptom

---

### User Story 2 - Understand Correlation Strength (Priority: P2)

As a user, I want to understand how strongly each food correlates with my symptoms, so I can prioritize which foods to avoid.

**Why this priority**: Understanding the strength of correlations helps users make informed dietary decisions rather than guessing.

**Independent Test**: Can be tested by viewing correlation indicators (percentages, frequency counts) that clearly explain the relationship between foods and symptoms.

**Acceptance Scenarios**:

1. **Given** a food appears frequently before a symptom, **When** I view the analysis, **Then** I see a clear indication of how often this pattern occurs (e.g., "75% of the time")
2. **Given** multiple foods might be triggers, **When** I view the analysis, **Then** I see them ranked by correlation strength
3. **Given** I want to understand a correlation metric, **When** I tap/hover on it, **Then** I see an explanation of what it means

---

### User Story 3 - Filter and Focus Analysis (Priority: P2)

As a user, I want to filter the analysis by time period, symptom type, or severity, so I can focus on the most relevant patterns.

**Why this priority**: Different time periods and symptom severities may reveal different patterns; users need flexibility to explore their data.

**Independent Test**: Can be tested by applying various filters and verifying that the analysis updates to show only relevant correlations.

**Acceptance Scenarios**:

1. **Given** I want to analyze recent patterns, **When** I select "Last 7 days", **Then** the analysis only considers data from that period
2. **Given** I want to focus on severe symptoms, **When** I filter by severity >= 7, **Then** I only see triggers for high-intensity symptoms
3. **Given** I apply multiple filters, **When** I view the results, **Then** the analysis reflects all active filters

---

### User Story 4 - View Clear Explanatory Text (Priority: P3)

As a user, I want to see plain-language explanations of patterns, so I can quickly understand my triggers without interpreting complex metrics.

**Why this priority**: Natural language insights make the app accessible to all users regardless of their analytical skills.

**Independent Test**: Can be tested by verifying that each analysis section includes clear, actionable text explanations.

**Acceptance Scenarios**:

1. **Given** a strong correlation exists, **When** I view the analysis, **Then** I see a symptom card showing "Diarrhea" with triggers listed as "Coffee (47%), Cheese (85%), Tomato (98%)" in a well-designed Material UI card
2. **Given** no clear patterns exist for a symptom, **When** I view the analysis, **Then** I see a message explaining that no strong correlations were found
3. **Given** I'm new to the app, **When** I first view the analysis page, **Then** I see helpful tooltips or guidance explaining how to interpret the data

---

### Edge Cases

- What happens when a user has no symptom data yet?
- How does the system handle symptoms with no preceding food entries?
- What if a user has gaps in their tracking (missing days)?
- How are foods consumed at similar times differentiated as potential triggers?
- What happens with very infrequent symptoms (once per month)?

## Requirements *(mandatory)*

### Functional Requirements

#### Analysis Logic

- **FR-001**: System MUST display a symptom-centric view as specified in User Story 1, with expandable cards for each symptom showing ranked trigger probabilities
- **FR-002**: System MUST calculate correlation strength between foods and symptoms based on temporal proximity
- **FR-003**: System MUST only show foods consumed within 8 hours before a symptom occurrence
- **FR-004**: System MUST provide clear explanations for all metrics and percentages shown
- **FR-005**: Users MUST be able to filter analysis by date range (last 7 days, 30 days, 90 days, custom)
- **FR-006**: System MUST rank potential triggers by correlation strength (frequency of co-occurrence)
- **FR-007**: System MUST generate plain-language insights describing patterns in user-friendly terms
- **FR-008**: System MUST handle cases where no correlations exist gracefully with informative messages
- **FR-009**: Users MUST be able to filter by symptom severity/intensity
- **FR-010**: System MUST exclude non-relevant correlations (triggers that precede symptoms less than 20% of the time)
- **FR-011**: System MUST provide tooltips or help text explaining how to interpret the analysis
- **FR-012**: System MUST consider quantity/portion size when analyzing food triggers (larger portions weighted up to 1.5x higher in correlation calculations)
- **FR-013**: System MUST group similar foods together for pattern recognition (e.g., all coffee entries, all dairy products)
- **FR-014**: Users MUST be able to view detailed timeline for specific symptom occurrences
- **FR-015**: System MUST clearly indicate the time gap between food consumption and symptom onset
- **FR-016**: System MUST weight correlations based on temporal proximity (foods consumed 1 hour before symptom weighted more heavily than those 7 hours before)
- **FR-017**: System MUST factor in trigger impact levels when calculating correlation weights (high-impact triggers from FoodCategory baseline probabilities >0.6 weighted more than low-impact ones <0.4 at similar time distances)
- **FR-018**: System MUST apply a combined weighting formula considering both time proximity and trigger impact level to determine most likely responsible triggers
- **FR-019**: System MUST calculate and display probability percentages for each potential trigger (e.g., "Coffee: 47%", "Cheese: 85%", "Tomato: 98%") in descending order within each symptom's trigger list
- **FR-020**: System MUST account for known trigger potentials (e.g., coffee and dairy products having higher IBS trigger probability than neutral foods like water)

#### UI/UX Requirements

- **FR-022**: Interface MUST follow Material Design 3 principles consistent with the existing app
- **FR-023**: Each symptom MUST be displayed as an expandable card showing trigger probabilities
- **FR-024**: Probability percentages MUST be visually represented with progress bars or similar visual indicators
- **FR-025**: Colors MUST indicate probability severity (e.g., red for high probability >70%, orange for medium 40-70%, green for low <40%)
- **FR-026**: Interface MUST provide smooth transitions and animations when expanding/collapsing symptom details
- **FR-027**: Layout MUST be responsive and work well on various screen sizes
- **FR-028**: Typography MUST clearly differentiate between symptom names, food triggers, and probability values
- **FR-029**: System MUST provide visual grouping of related information (symptoms grouped together, triggers listed under each symptom)
- **FR-030**: Touch targets MUST meet accessibility guidelines (minimum 48dp)
- **FR-031**: Interface MUST integrate seamlessly with existing navigation patterns in the app

### Key Entities *(include if feature involves data)*

- **Symptom Occurrence**: A specific instance of a symptom with date, time, type, and intensity
- **Food Entry**: A record of food/drink consumed with date, time, name, and quantity
- **Correlation**: A calculated relationship between a food and symptom including strength percentage and occurrence count
- **Analysis Time Window**: The 8-hour period before a symptom that is considered for potential triggers
- **Correlation Weight**: A calculated value combining time proximity (1-8 hours) and trigger impact level to determine likelihood of causation
- **Trigger Probability**: A percentage value (0-100%) representing the likelihood that a specific food triggered a symptom, based on temporal proximity, known trigger potential, and historical patterns
- **Known Trigger Potential**: Baseline probability that a food type causes IBS symptoms (e.g., dairy=high, coffee=high, water=negligible)
- **Pattern**: A recurring correlation that appears multiple times across the user's history

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can identify their top 3 trigger foods within 30 seconds of analysis page load completion
- **SC-002**: 80% of users understand what the correlation metrics mean without external help
- **SC-003**: Analysis page loads with results in under 2 seconds for 90 days of data (approximately 500 food entries and 100 symptom occurrences)
- **SC-004**: Users report 50% improvement in understanding their symptom triggers after using the new analysis
- **SC-005**: 90% reduction in user confusion about analysis metrics (measured through support requests)
- **SC-006**: Users can explain their trigger patterns to others using insights from the app
- **SC-007**: Time to meaningful insight is under 1 minute for users with 30+ days of tracked data

## Scope & Boundaries *(mandatory)*

### In Scope
- Redesigning the analysis page UI for clarity with Material Design 3
- Symptom-centric view with probability-based correlations
- Clear visual representation of trigger probabilities
- Color-coded probability indicators
- Expandable cards for detailed symptom analysis
- Clear explanations of all metrics
- Plain-language pattern descriptions
- Filtering capabilities for focused analysis
- Food grouping and pattern recognition
- Integration with existing app navigation and design patterns

### Out of Scope
- Predictive analysis (forecasting future symptoms)
- Medical recommendations or diagnosis
- Integration with external health apps
- Automated meal planning based on triggers
- Social features for comparing patterns with other users

## Assumptions *(mandatory)*

- Users consistently track both food and symptoms for meaningful analysis
- An 8-hour time window is appropriate for capturing most food-symptom correlations
- Time proximity is a meaningful factor in determining causation (closer = more likely)
- Trigger impact levels can be classified and weighted appropriately
- Known trigger potentials can be established for common IBS triggers (dairy, coffee, fatty foods, etc.)
- Probability percentages are more intuitive than raw correlation scores
- Material Design 3 components are available in the existing app
- Users understand basic correlation concepts (with proper explanation)
- The existing database already captures necessary timestamp data for foods and symptoms
- Users want actionable insights, not just raw data
- Correlation strength can be meaningfully calculated from frequency of co-occurrence combined with temporal and impact weighting
- Similar foods can be grouped automatically based on name similarity or categories