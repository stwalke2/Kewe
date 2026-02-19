# Collapsible Sidebar + Brand Logo Plan

## Requirements
- Sidebar can collapse to a left rail that keeps navigation icons visible.
- Icons should be slightly larger in collapsed mode.
- Collapse/expand should animate smoothly.
- Sidebar brand should include a circular kiwi side-profile logo next to "Kewe".
- Logo should remain visible and scale slightly in collapsed mode.

## Non-goals
- Route or navigation information architecture changes.
- Mobile navigation redesign (existing mobile sidebar hiding remains unchanged).

## Proposed UI/API/Data Model
- UI-only change in `Layout.tsx` with local `sidebarCollapsed` state.
- Add a sidebar toggle button.
- Add inline SVG `KiwiLogo` component for temporary brand mark.
- Add CSS transitions to animate layout width and element visibility.
- No backend/API/data model changes.

## Test Plan
- Run UI build (`npm run build`) to validate TS/CSS compilation.
- Manually verify in browser:
  - Sidebar collapses to icon rail.
  - Icons/logo scale up slightly.
  - Transition is smooth.
  - Sidebar content still navigates correctly.

## Risks / Open Questions
- Hidden labels in collapsed state may need tooltips for accessibility in a future update.
- Current collapse state is session-local and not persisted.
