# Reviewed ExerciseSubstitute Catalog

The bundled `data/exercise-substitutes-reviewed.json` file is the publishable, editor-confirmed source for the initial `EQUIPMENT_SWAP` relations. The application imports only relations whose `confirmed` field is `true` and does so idempotently.

Candidate generation remains a separate editorial workflow: a generated candidate is not eligible for user-facing substitution until an editor confirms it and adds it to this catalog. This preserves the `ExerciseSubstitute` decision in `CONTEXT.md` while allowing the template repair flow to ship with a small reviewed system catalog.
