# Confirmed feedback adjustments apply once to the next Plan prescription

For beginners, adaptation must be explainable and must not rewrite training history. We will use two consecutive `TOO_EASY` or `TOO_HARD` ExerciseFeedback values to propose one bounded adjustment to the next unstarted occurrence of that Exercise in the Active Plan; the User must confirm it, and an audit record preserves the before/after prescription and outcome instead of silently changing a Workout or carrying the proposal across Plans.

## Considered Options

- Automatic or Plan-wide rewrites would make the generated Plan and completed training difficult to trust and audit.
- A full training-log/estimated-1RM system needs actual set, repetition, and load data that the product does not yet collect.
- Reusing the whole Workout replacement chain would duplicate an entire Workout for one prescription-level change.

## Consequences

The first version only covers Active Plan feedback and makes one small, confirmed change at a time. On-demand Workouts, templates, automatic renewals, full set logging, and cross-Plan learning remain separate future decisions.
