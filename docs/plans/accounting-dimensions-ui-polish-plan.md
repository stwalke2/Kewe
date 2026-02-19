# Accounting Dimensions UI Polish Plan

## Requirements
- Restore sortable behavior on **all** Accounting Dimensions table columns.
- Ensure table header typography is visually consistent across all headers.
- Improve responsive behavior for smaller viewports, including horizontal scroll for wide tab/table regions.
- Make search input narrower and aligned with toolbar layout.
- Add a working **Filter** button that toggles additional filtering controls.
- Add a working **Download** button that exports the current list to an Excel-compatible file.

## Non-goals
- No backend API changes.
- No changes to canonical dimension data model.
- No server-side filtering/sorting implementation.

## Proposed UI/API Changes
- Extend client-side sort keys to support all visible columns.
- Introduce toolbar action buttons for Filter and Download.
- Add an expandable filter panel (status + include inactive quick controls).
- Export currently filtered/sorted list as CSV (Excel-compatible).

## Test Plan
- Build the frontend (`npm run build`).
- Manually verify:
  - Sort toggles on each header.
  - Header font treatment is consistent.
  - Horizontal scroll appears when viewport is narrow.
  - Filter panel toggles and filters rows.
  - Download creates a CSV with visible rows.

## Risks / Open Questions
- “Similar to example” may imply additional visual details not currently in app design tokens.
- Export format requested as Excel; implementation will use CSV for broad Excel compatibility unless a native XLSX dependency is approved.
