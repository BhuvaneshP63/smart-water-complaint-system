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

Run backend tests from the project root:

    mvn test

Run frontend checks from swcms-frontend:

    npm run build
    npm run lint

## Manual browser workflow record

Use workflow-evidence.md to record a real run. Do not mark a step passed based only on a screenshot of the login page or a passing API test.

## Coverage and complexity reports

JaCoCo and a Java complexity analyzer are not configured in the current build. Add and run the selected Maven reporting plugins before claiming those reports are complete. Reports should be committed under docs/review/evidence/ only after confirming they contain the current source revision and successful test run.

## Defects

Record actual reproducible defects in defect-log.md, including the affected version, steps, expected result, actual result, severity, and resolution. Do not record invalid credentials or environment setup as application defects unless reproduced as a product bug.
