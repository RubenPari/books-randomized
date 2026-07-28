# Books, Randomized Design System

## 0. Research Log

- Embedded references: shortlisted WIRED, Notion, and The Verge; selected the Minimalist execution guide with WIRED because its newsstand grid, serif/mono stack, square rules, and refusal of floating cards best fit a book-discovery showcase.
- Contemporary magazines: reviewed Public Books' section-led information architecture, Pentagram's minimalist Paris Review identity, and The New Yorker's content-first section model. The system takes their strong mastheads, issue-like indexing, and text-led hierarchy without copying brand marks or copy.
- Lazyweb: unavailable in this environment; current magazine sites and the curated WIRED reference supplied the real-product lane.
- ImageGen drafts: `design-concepts/editorial-index.png`, `design-concepts/literary-journal.png`, and `design-concepts/responsive-index.png`. `literary-journal.png` is the reference-fidelity contract because its issue-index composition scales component specimens without becoming a dashboard.
- Palette/type validation: the requested Newsreader, Work Sans, IBM Plex Mono stack and paper/ink/cobalt palette align with the editorial/readability guidance in the curated references.

## 1. Atmosphere & Identity

The interface feels like opening a freshly printed independent literary journal whose margins have become controls. It is composed, literate, direct, and a little mechanical. The signature is the **issue index**: every group is introduced by mono metadata, separated by square printer's rules, and given a clear place in the reading order.

Primary reader: a curious reader who wants surprise without losing control. Secondary reader: a keyboard or zoom user who needs structure and state to remain obvious. Situational persona: a reader on a narrow screen with interrupted attention. The surface prioritizes legible hierarchy and predictable actions over decoration.

## 2. Color

| Role | Token | Value | Usage |
|---|---|---|---|
| Paper | `--color-paper` | `#FCFBF7` | Page and control surfaces |
| Ink | `--color-ink` | `#171717` | Primary text, strong rules |
| Cobalt | `--color-cobalt` | `#0057B8` | Selected, link, focus, primary action |
| Cobalt dark | `--color-cobalt-dark` | `#003F87` | Hover/pressed interaction |
| Muted ink | `--color-muted` | `#66645F` | Secondary text |
| Hairline | `--color-rule` | `#C9C6BE` | Low-emphasis rules |
| Disabled | `--color-disabled` | `#8A8882` | Disabled labels |
| Error | `--color-error` | `#B42318` | Error border and copy |
| Error wash | `--color-error-wash` | `#FBEAE7` | Error state ground |
| Loading wash | `--color-loading` | `#E9E6DF` | Skeleton ground |

No raw color may appear in component styles. Cobalt is interactive, never decorative. There is no dark mode in this foundation.

## 3. Typography

| Level | Token | Size / line-height | Weight | Usage |
|---|---|---|---|---|
| Display | `--type-display` | `clamp(3rem, 9vw, 7.5rem)` / `.9` | 400 | Page masthead |
| H1 | `--type-h1` | `clamp(2.25rem, 5vw, 4.5rem)` / `.98` | 400 | Showcase title |
| H2 | `--type-h2` | `clamp(1.75rem, 3vw, 2.75rem)` / `1.05` | 400 | Section titles |
| H3 | `--type-h3` | `1.375rem` / `1.15` | 500 | Component/book titles |
| Body large | `--type-body-lg` | `1.125rem` / `1.5` | 400 | Decks |
| Body | `--type-body` | `1rem` / `1.55` | 400 | UI/body |
| Small | `--type-small` | `.875rem` / `1.45` | 500 | Hints/meta |
| Label | `--type-label` | `.75rem` / `1.25` | 600 | Uppercase mono labels |

- Display/editorial: `"Newsreader", Georgia, serif`.
- UI/body: `"Work Sans", Arial, sans-serif`.
- Metadata: `"IBM Plex Mono", "Courier New", monospace`.
- Mono labels are uppercase with `0.08em` tracking. Body copy never drops below 14px.

## 4. Spacing & Layout

Base unit: 4px.

| Token | Value |
|---|---|
| `--space-1` | `0.25rem` |
| `--space-2` | `0.5rem` |
| `--space-3` | `0.75rem` |
| `--space-4` | `1rem` |
| `--space-5` | `1.25rem` |
| `--space-6` | `1.5rem` |
| `--space-8` | `2rem` |
| `--space-10` | `2.5rem` |
| `--space-12` | `3rem` |
| `--space-16` | `4rem` |

- Content max: 1280px; 12-column desktop grid; 24px gutters; responsive outer gutter `clamp(1rem, 3vw, 2.5rem)`.
- Breakpoints under test: 375px, 768px, 1280px.
- At 375px all specimen grids become one reading column. At 768px, two columns are permitted. At 1280px, the specimen index uses up to four.
- Long and unbroken content uses `overflow-wrap: anywhere`; primary content never causes horizontal page scroll.

## 5. Components

### Masthead navigation
- **Structure**: landmark header, brand link, route links, menu trigger.
- **States**: default, link hover/focus/current, mobile drawer open.
- **Accessibility**: semantic nav; current route; drawer trigger exposes expanded state; Escape closes and focus returns.
- **Layout**: full-width editorial band with clustered actions.

### Buttons
- **Variants**: primary cobalt, secondary outline, tertiary text.
- **States**: default, hover, active, focus-visible, disabled, busy.
- **Spacing**: `--space-3` × `--space-5`; square corners; 2px rule.
- **Accessibility**: native button; busy state exposes text and `aria-busy`.
- **Motion**: active uses a small `transform`; motion removed for reduced-motion users.

### Field
- **Structure**: visible label, input, hint/error.
- **States**: default, focus, disabled, invalid.
- **Accessibility**: programmatic label and description/error association.

### Chip
- **Variants**: filter default and selected/removable.
- **States**: hover, focus, selected, disabled, long-label.
- **Layout**: compact cluster; square, not pill.

### Dialog and drawer
- **Structure**: CDK overlay, heading, close control, content/actions.
- **States**: closed/open; modal focus trap; dismiss by Escape.
- **Depth**: separated with ink rules and opaque paper, never shadow.
- **Motion**: transform/opacity only; no transition under reduced motion.

### Book tile
- **Structure**: abstract cover plane, mono category, title, author, summary, metadata, action.
- **Variants**: feature and compact.
- **States**: default, keyboard focus within, loading placeholder.
- **Layout**: editorial split; square cover; rules rather than a card surface.

### Feedback state
- **Variants**: loading, empty, error.
- **Accessibility**: loading announces politely; errors use alert semantics; actions remain reachable.
- **Layout**: bordered editorial notice, not a floating card.

## 6. Motion & Interaction

| Type | Token | Value | Usage |
|---|---|---|---|
| Micro | `--motion-fast` | `120ms` | Button and link feedback |
| Standard | `--motion-standard` | `220ms` | Drawer/dialog entry |
| Easing | `--motion-ease` | `cubic-bezier(.2,.8,.2,1)` | All transitions |

Only transform and opacity animate. Focus is a 3px cobalt outline with 3px offset. `prefers-reduced-motion: reduce` disables transitions and animation. No ambient motion or scroll reveals.

## 7. Depth & Surface

Strategy: **borders only**. Strong divisions use `2px solid var(--color-ink)`; quiet divisions use `1px solid var(--color-rule)`. All radii are zero. Shadows, gradients, glass, and floating card treatments are prohibited.

## 8. Accessibility Constraints & Accepted Debt

### Constraints

- WCAG 2.2 AA; 4.5:1 normal-text and 3:1 large-text contrast floors.
- Full keyboard reachability, persistent visible focus, semantic landmarks, 44px minimum primary pointer targets.
- Drawer/dialog manages focus and closes with Escape.
- Content reflows at 400% zoom and 375px without two-dimensional scrolling.
- Reduced motion is honored. Status is never communicated by color alone.
- Copy remains plain and specific; empty/error states always include a next action.

### Accepted Debt

| Item | Location | Why accepted | Owner / Exit |
|---|---|---|---|
| Font files are fetched from Google Fonts | global stylesheet | Foundation has no self-hosted font assets yet; robust fallbacks prevent blocking | Frontend owner; self-host before offline distribution |

