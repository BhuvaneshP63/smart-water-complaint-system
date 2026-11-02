# SWCMS test plan and evidence

## Core automated modules

| Module | Existing tests | Main scenarios |
| --- | --- | --- |
| Complaint workflow | WorkflowEndToEndTest, ComplaintServiceTest, ComplaintServiceExceptionTest, ComplaintSearchServiceTest, validator tests | Registration/login, complaint creation, assignment, invalid transition rejection, paging/search, resolution |
| Feedback workflow | WorkerFeedbackServiceTest, WorkflowEndToEndTest | Citizen and worker feedback, photos, visibility by role, duplicate submission rejection |
| File storage | FileStorageServiceTest, WorkflowEndToEndTest | Image storage and retrieval for complaint, completion, and feedback |

## Test cases represented in code

- Positive: citizen creates a complaint; admin assigns it; worker progresses and resolves it with completion evidence; both sides submit feedback; authorized roles read feedback.
- Negative: invalid status transition is rejected; duplicate citizen and worker feedback are rejected.
- Boundary: complaint list pagination validates page input and returns the requested page; upload validation tests cover supported and rejected file content.

Run all backend tests from the project root. On Windows, use the in-process option if Surefire stalls while Mockito attaches its agent:

    mvn test -DforkCount=0

Run frontend checks from swcms-frontend:

    npm run build
    npm run lint

## Manual browser workflow record

Use workflow-evidence.md to record a real run. Do not mark a step passed based only on a screenshot of the login page or a passing API test.

## Coverage and complexity reports

JaCoCo is configured to generate a report during Maven verify. Run the end-to-end workflow and non-mocking core tests under the JaCoCo agent:

    mvn verify -Dtest=WorkflowEndToEndTest,ComplaintSearchServiceTest,ComplaintValidatorTest

The HTML report is written to target/site/jacoco/index.html. The current evidence copy and summary are under evidence/backend-coverage and evidence/coverage-summary.md. The verified report includes all 24 backend tests.

A Java complexity analyzer is not configured yet. Select a tool, run it against the reviewed source revision, and retain the output under docs/review/evidence/ before claiming that evidence is complete.

## Browser smoke test

Start the backend in Spring Tools with program argument --spring.profiles.active=local, then run the frontend browser test:

    cd swcms-frontend
    $env:PLAYWRIGHT_BROWSERS_PATH = '0'
    npx playwright install chromium
    npm run test:e2e

This test signs in through the UI as the seeded local administrator and verifies dashboard access. It requires Chromium and the backend on port 8080. The report is written to swcms-frontend/playwright-report/.

## Defects

Record actual reproducible defects in defect-log.md, including the affected version, steps, expected result, actual result, severity, and resolution. Do not record invalid credentials or environment setup as application defects unless reproduced as a product bug.
