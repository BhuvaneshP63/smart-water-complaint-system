# SWCMS review readiness

This folder tracks review criteria against evidence in the repository. It does not claim external Jira/GitHub setup or manual demonstrations that have not happened.

## Current evidence

- Backend tests cover complaint search/validation, complaint services, feedback, file storage, and an API workflow spanning citizen, worker, and admin roles. See Maven test output and target/surefire-reports/.
- Frontend production build and ESLint have passed in the current workspace.
- A Playwright browser test now covers local administrator sign-in and dashboard access; its execution still requires the API and Chromium.
- JaCoCo coverage and cyclomatic-complexity evidence has been generated for the 24-test backend run; the report and summary are under evidence/.
- The end-to-end API test covers registration/login, complaint submission, assignment, status transitions, citizen and worker feedback, duplicate submission rejection, and admin feedback visibility.
- A local Git repository is initialized for changes from this point onward. The baseline commit reflects the current checkout; it is not a reconstructed historical commit timeline.

## Still needs an actual external or live action

- Publish the repository to GitHub and add the real remote URL. No remote or existing Git history was present when this review started.
- Create/import the Jira backlog, assign real team members, and record actual sprint planning/review/retrospective outcomes. Import template: jira-backlog.csv.
- Run and record a successful browser demonstration for Citizen, Worker, and Admin. API integration tests are useful evidence but do not replace the review's UI workflow demonstration.
- Run the full three-role browser demonstration and record it in workflow-evidence.md; the current automated UI test covers Admin sign-in and dashboard access only.
- Attach the real sprint burndown, review/retrospective records, and individual contribution evidence after Jira/GitHub are configured.

## Account access

External Jira/GitHub actions must use the team's actual accounts and repository. Do not put credentials, tokens, or citizen-upload files in Git.
