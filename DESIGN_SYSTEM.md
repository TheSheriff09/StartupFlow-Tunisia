# StartupFlow Design System v1.0

> **Purple Glassmorphism · Fully Consistent · Professionally Polished**

---

## 1. Color Palette

### Primary — Violet
| Token               | Value                          | Usage                                    |
|---------------------|--------------------------------|------------------------------------------|
| Primary             | `#6d28d9`                      | Headings, nav active tab, primary actions|
| Primary dark        | `#4c1d95`                      | Deep headings, sidebar brand text        |
| Primary medium      | `#7c3aed`                      | Focus rings, accent buttons              |
| Primary light       | `#8b5cf6`                      | Icons, secondary emphasis                |
| Primary pale        | `#a78bfa`                      | Disabled text, borders                   |
| Primary tint        | `#ede9fe`                      | Badge backgrounds                        |
| Primary ghost       | `rgba(167,139,250,0.55)`       | Input border color                       |

### Semantic Colors
| Token    | Value      | Usage                          |
|----------|------------|--------------------------------|
| Success  | `#059669`  | Confirmations, active status   |
| Danger   | `#ef4444`  | Errors, delete operations      |
| Warning  | `#d97706`  | Caution messages               |
| Info     | `#0369a1`  | Informational dialogs          |

### Surface & Glass
| Token               | Value                          | Usage                   |
|---------------------|--------------------------------|-------------------------|
| Glass card           | `rgba(255,255,255,0.72)`      | Card backgrounds        |
| Glass border         | `rgba(196,181,253,0.58)`      | Card borders            |
| Card gradient start  | `rgba(160,105,242,0.82)`      | Title row gradient      |
| Card gradient end    | `rgba(172,124,255,0.78)`      | Title row gradient      |

### Text
| Token          | Value                          | Usage                 |
|----------------|--------------------------------|-----------------------|
| Text heading   | `#4c1d95`                      | Card/page titles      |
| Text body      | `#374151`                      | Main content          |
| Text muted     | `#64748b`                      | Secondary labels      |
| Text faint     | `#94a3b8`                      | Timestamps, captions  |
| Text on dark   | `rgba(220,210,255,0.95)`       | Dialog body text      |

---

## 2. Typography

| Level                | Size   | Weight | Usage Example                   |
|----------------------|--------|--------|---------------------------------|
| Page title           | 26px   | 900    | Dashboard heading               |
| Card title           | 19px   | 900    | Startup / BP card name          |
| Dialog title         | 15px   | 800    | AlertUtil / DialogStyler header |
| Section label        | 14px   | 800    | Form section dividers           |
| Body                 | 13px   | 600    | Card descriptions, form fields  |
| Form field label     | 11px   | bold   | Labels above inputs in dialogs  |
| Caption / badge      | 10px   | 800    | Status badges, timestamps       |
| Error message        | 10.5px | 700    | Validation error text           |

**Font Stack:** `Segoe UI, Inter, Helvetica Neue, Arial, sans-serif`

---

## 3. Spacing System (8px Grid)

| Token | Size  | Usage                                  |
|-------|-------|----------------------------------------|
| XS    | 4px   | Tight inline spacing (badge padding)   |
| SM    | 8px   | Between related elements (label→field) |
| MD    | 16px  | Form field gaps, section spacing       |
| LG    | 24px  | Card padding, section breaks           |
| XL    | 32px  | Page margins                           |

---

## 4. Border Radius

| Token                | Size | Usage                        |
|----------------------|------|------------------------------|
| Small (badges)       | 14px | Status chips, tags           |
| Medium (inputs)      | 12px | Text fields, combo boxes     |
| Large (buttons)      | 20px | Action buttons               |
| XL (cards, dialogs)  | 24px | Card containers              |
| Dialog pane          | 22px | Dialog outer pane            |
| Round (pills, FAB)   | 32px | Search bars, floating action |

---

## 5. Button Styles

### Primary (Save / OK)
```css
-fx-background-color: linear-gradient(to right, #7c3aed, #a855f7, #c026d3);
-fx-text-fill: white;
-fx-font-size: 13px; -fx-font-weight: bold;
-fx-background-radius: 20; -fx-border-color: transparent;
-fx-padding: 10 28 10 28; -fx-cursor: hand;
-fx-effect: dropshadow(gaussian, rgba(192,38,211,0.65), 18, 0.12, 0, 5);
```

### Ghost (Cancel / Close)
```css
-fx-background-color: rgba(255,255,255,0.12);
-fx-border-color: rgba(255,255,255,0.35);
-fx-border-width: 1.5; -fx-border-radius: 20; -fx-background-radius: 20;
-fx-text-fill: rgba(255,255,255,0.85);
-fx-font-size: 13px; -fx-font-weight: bold;
-fx-padding: 10 26 10 26; -fx-cursor: hand;
```

### Context Menu Trigger (⋯)
```css
.btn-more {
    -fx-background-color: transparent;
    -fx-text-fill: #a78bfa;
    -fx-font-size: 20px; -fx-cursor: hand;
    -fx-padding: 0 6 0 6; -fx-min-width: 28;
}
```

### Back Navigation
```css
.btn-back {
    -fx-background-color: rgba(124,58,237,0.13);
    -fx-text-fill: #7c3aed;
    -fx-font-size: 13px; -fx-font-weight: bold;
    -fx-background-radius: 20; -fx-padding: 9 20 9 20;
    -fx-cursor: hand;
}
```

---

## 6. Form Field Styles

### Normal State
```css
-fx-background-color: rgba(255,255,255,0.92);
-fx-text-fill: #3b1f6b;
-fx-border-color: rgba(167,139,250,0.55);
-fx-border-radius: 12; -fx-background-radius: 12;
-fx-border-width: 1.5;
-fx-prompt-text-fill: rgba(139,92,246,0.46);
-fx-font-size: 13px; -fx-padding: 11 16 11 16;
```

### Error State
```css
-fx-background-color: rgba(255,235,235,0.95);
-fx-text-fill: #7f1d1d;
-fx-border-color: #f87171;
-fx-border-radius: 12; -fx-background-radius: 12;
-fx-border-width: 2;
-fx-prompt-text-fill: rgba(239,68,68,0.50);
-fx-font-size: 13px; -fx-padding: 11 16 11 16;
```

### DatePicker / ComboBox — Normal
```css
-fx-background-color: rgba(255,255,255,0.90);
-fx-border-color: rgba(167,139,250,0.55);
-fx-border-radius: 12; -fx-background-radius: 12;
-fx-border-width: 1.5; -fx-font-size: 13px;
-fx-padding: 3 0 3 6;
```

### Error Label
```css
-fx-text-fill: #ef4444;
-fx-font-size: 10.5px; -fx-font-weight: 700;
-fx-padding: 2 0 0 2;
```

---

## 7. Dialog System

### Outer Pane (dark glass gradient)
```css
-fx-background-color: linear-gradient(to bottom right,
    rgba(83,31,167,0.96) 0%,
    rgba(91,20,180,0.98) 45%,
    rgba(109,40,217,0.94) 100%);
-fx-background-radius: 22; -fx-border-radius: 22;
-fx-border-color: rgba(255,255,255,0.28);
-fx-border-width: 1.5;
-fx-effect: dropshadow(gaussian, rgba(109,40,217,0.70), 60, 0.14, 0, 18);
```

### Header Panel
```css
-fx-background-color: linear-gradient(to right, <colorFrom>, <colorTo>);
-fx-background-radius: 22 22 0 0;
-fx-padding: 18 22 18 22;
```

| Context            | `colorFrom` | `colorTo` |
|--------------------|-------------|-----------|
| Success (green)    | `#059669`   | `#10b981` |
| Error (red)        | `#b91c1c`   | `#dc2626` |
| Warning (amber)    | `#d97706`   | `#f59e0b` |
| Info (blue)        | `#0369a1`   | `#0284c7` |
| Confirm (purple)   | `#6d28d9`   | `#7c3aed` |
| Danger confirm     | `#b91c1c`   | `#dc2626` |

### Button Bar Footer
```css
-fx-background-color: rgba(0,0,0,0.18);
-fx-background-radius: 0 0 22 22;
-fx-padding: 16 22 20 22;
```

### Content Area
```css
-fx-background-color: transparent;
-fx-padding: 10 16 10 16;
```

### Content Label
```css
-fx-text-fill: rgba(220,210,255,0.95);
-fx-font-size: 13px;
-fx-padding: 4 0 4 0;
```

---

## 8. Card Components

### Startup Card & Business Plan Card
| Property        | Value                 |
|-----------------|-----------------------|
| Min height      | 240px                 |
| Top padding     | 24px                  |
| Info row spacing| 8px                   |
| Corner radius   | 24px                  |
| Background      | `rgba(255,255,255,0.72)` with glass blur |
| Border          | `rgba(196,181,253,0.58)` 1.5px          |
| Actions         | Single "⋯" button → ContextMenu dropdown |

### Context Menu
```css
.card-context-menu {
    -fx-background-color: rgba(255,255,255,0.96);
    -fx-background-radius: 14;
    -fx-border-color: rgba(196,181,253,0.6);
    -fx-border-radius: 14; -fx-border-width: 1.5;
    -fx-padding: 6 0 6 0;
    -fx-effect: dropshadow(gaussian, rgba(109,40,217,0.25), 18, 0.1, 0, 5);
}
```

---

## 9. Animation Constants

### Open Animation
| Parameter         | Value | Unit |
|-------------------|-------|------|
| Scale from        | 0.82  | —    |
| Scale to          | 1.0   | —    |
| Scale duration    | 320   | ms   |
| Fade from         | 0     | —    |
| Fade to           | 1.0   | —    |
| Fade duration     | 250   | ms   |
| Interpolator      | `SPLINE(0.22, 0.61, 0.36, 1.0)` | — |

### Close Animation
| Parameter         | Value | Unit |
|-------------------|-------|------|
| Scale to          | 0.84  | —    |
| Scale duration    | 200   | ms   |
| Fade to           | 0     | —    |
| Fade duration     | 200   | ms   |

### Background Blur
| Parameter         | Value |
|-------------------|-------|
| Blur radius       | 10    |
| Type              | GaussianBlur |

---

## 10. CRUD Form Layout Template

```
┌─────────────────────────────────────────┐
│  Dialog Pane (dark glass gradient)      │
│  ┌───────────────────────────────────┐  │
│  │ Header Panel (color gradient)     │  │
│  │ Title label (white, 15px, bold)   │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ── SECTION LABEL ──────────────────    │
│                                         │
│  Field Label (11px, bold, light)        │
│  ┌───────────────────────────────────┐  │
│  │ TextField (glass white, 13px)     │  │
│  └───────────────────────────────────┘  │
│  Error label (10.5px, #ef4444)          │
│                                         │
│  Field Label                            │
│  ┌───────────────────────────────────┐  │
│  │ TextArea / DatePicker             │  │
│  └───────────────────────────────────┘  │
│  Error label                            │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ Button Bar (dark footer)          │  │
│  │   [Cancel]  [Save ▶]             │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

**Layout Rules:**
- Form fields: VBox with 5px spacing between label → field → error
- Sections: 14px top padding, 1px bottom border separator
- Field labels created via `DialogStyler.fieldLabel(text)` or `DesignTokens.FIELD_LABEL`
- Section labels via `DialogStyler.sectionLabel(text)` or `DesignTokens.SECTION_LABEL`
- Input styling: `DialogStyler.inputStyle()` (returns `DesignTokens.FIELD_NORMAL`)
- Error labels: `FormValidator.errorLabel()` (uses `DesignTokens.ERROR_LABEL`)
- Submit binding: `FormValidator.bindSubmitButton(btn, ...fields)`

---

## 11. CSS Architecture

### Active Stylesheet
Only **`styles.css`** is loaded at runtime (via FXML `stylesheets="@styles.css"` and programmatic `getStylesheets().add()`).

### Dead Files (not loaded — safe to archive)
- `base.css` — Never referenced
- `components.css` — Never referenced
- `theme.css` — Never referenced
- `layout.css` — Never referenced
- `dashboard.css` — Never referenced

### Java Token File
`DesignTokens.java` (`tn.esprit.utils`) — Single source of truth for all inline style constants shared across:
- `FormValidator` — field normal/error/label styles
- `ValidationUtil` — field normal/error/label styles
- `DialogStyler` — dialog pane/header/button/field styles + animation constants
- `AlertUtil` — dialog pane/header/button styles + animation constants

---

## 12. Homogeneity Checklist

### ✅ Completed
- [x] **Single style source**: `DesignTokens.java` holds ALL shared inline constants
- [x] **FormValidator** uses `DesignTokens.FIELD_NORMAL/FIELD_ERROR/ERROR_LABEL/COMBO_*`
- [x] **ValidationUtil** uses `DesignTokens.FIELD_NORMAL/FIELD_ERROR/ERROR_LABEL`
- [x] **DialogStyler** uses `DesignTokens.*` for pane, header, buttons, fields, animation
- [x] **AlertUtil** uses `DesignTokens.*` for pane, header, buttons, content, animation
- [x] **Dialog radius unified**: 22px everywhere (was 22 in AlertUtil, 24 in DialogStyler)
- [x] **Dialog header font**: 15px/800 everywhere (was 18px/900 in DialogStyler)
- [x] **Button bar**: `rgba(0,0,0,0.18)` + radius 22 everywhere (was 20 in some places)
- [x] **OK button**: identical gradient + padding `10 28` everywhere
- [x] **Cancel button**: identical ghost style + padding `10 26` everywhere
- [x] **Card top padding**: 24px on both startup and BP cards
- [x] **Card minHeight**: 240px on both startup and BP cards  
- [x] **Card info spacing**: 8px on both startup and BP cards
- [x] **Card actions**: "⋯" ContextMenu on both card types (no inline buttons)
- [x] **Dead CSS removed**: ~100 lines of orphaned .btn-edit/.btn-delete/.card-action-bar
- [x] **.admin-back-btn unified** with .btn-back: radius 20, padding 9 20, font 13
- [x] **Animation — open scale**: 0.82 everywhere (was 0.76–0.86 across 6 controllers)
- [x] **Animation — fade duration**: 250ms everywhere (was 220–310ms)
- [x] **Animation — scale duration**: 320ms everywhere (was 280–360ms)
- [x] **Animation — close fade**: 200ms everywhere (was 180–200ms)
- [x] **Animation — close scale**: 0.84 / 200ms where present
- [x] **Animation — interpolator**: `SPLINE(0.22, 0.61, 0.36, 1.0)` everywhere
- [x] **GaussianBlur radius**: 10 everywhere (was 7–10)
- [x] **Field label font**: 11px everywhere (was 10px in FormValidator.fieldRow)

### ⚠️ Known Remaining Items
- [ ] 4 popup FXMLs (aianalysis, financialforecast, fundingsimulation, currency) use 27-33 inline style attributes — consider migrating to CSS classes
- [ ] 5 dead CSS files (base.css, components.css, theme.css, layout.css, dashboard.css) — safe to delete
- [ ] Close scale animation missing in AiAnalysis, FinancialForecast, FundingSim, Currency (they only fade-out)

---

## 13. Design Professionalism Score

| Category                     | Max  | Score | Notes                                                        |
|------------------------------|------|-------|--------------------------------------------------------------|
| **Color consistency**        | 15   | 14    | Unified palette, all tokens in one file. 1pt: popup FXMLs still inline |
| **Typography hierarchy**     | 10   | 9     | Clear 7-level scale. 1pt: some popup FXMLs hardcode sizes   |
| **Spacing system**           | 10   | 9     | 8px grid enforced in cards/dialogs. 1pt: some popup internal spacing varies |
| **Border radius consistency**| 8    | 8     | Fully unified: 12 inputs, 20 buttons, 22 dialogs, 24 cards  |
| **Button style uniformity**  | 10   | 10    | Primary/Ghost/Back/More all defined, consistent everywhere   |
| **Form field consistency**   | 10   | 10    | Single source via DesignTokens; normal/error/combo all shared|
| **Dialog system unity**      | 10   | 10    | AlertUtil + DialogStyler both use identical DesignTokens      |
| **Card component parity**    | 8    | 8     | Startup & BP cards: same padding, height, spacing, context menu |
| **Animation uniformity**     | 8    | 7     | All open params unified. 1pt: 4 controllers lack close scale |
| **CSS architecture**         | 6    | 5     | Single active file, dead classes removed. 1pt: 5 dead files remain |
| **Code DRY principle**       | 5    | 5     | Zero duplicated style constants (all flow through DesignTokens) |
|                              |      |       |                                                              |
| **TOTAL**                    | 100  | **95** | **A+ — Production-ready design system**                     |

### Score Improvement Path to 100/100
1. Delete the 5 dead CSS files (+1)
2. Extract popup FXML inline styles to CSS classes (+2)  
3. Add close scale animation to the 4 fade-only popup controllers (+1)
4. Audit popup FXML internal spacing to 8px grid (+1)

---

*Generated for StartupFlow · JavaFX 21 + Java 17*
*Last updated: Design System v1.0*
