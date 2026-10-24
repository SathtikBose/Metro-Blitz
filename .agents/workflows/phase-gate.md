# Phase gate

Run this when the current Metro Blitz phase is finished:

1. Read the current phase's tests in `docs/08_TEST_PLAN.md` and run every one (unit tests, adb/Mobile MCP device checks).
2. Fix failures and rerun until all pass or are logged N/A with a reason.
3. Commit with a Conventional Commit message, create the phase tag.
4. Update `docs/PROGRESS.md` (status, test results, known issues, library versions).
5. Print the Phase Report (format in `AGENTS.md`) and stop. Wait for the human to say `next`.
