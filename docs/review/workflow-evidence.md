# Manual workflow evidence

Complete this record during an actual browser run. Replace each Not run with Pass or Fail, the date, and concise evidence. Keep test accounts and personal information out of screenshots.

| Role | Steps | Result | Evidence / notes |
| --- | --- | --- | --- |
| Citizen | Register/sign in; create complaint with image; track status/history; receive status notification; submit one rating/comment/photo after resolution; view worker feedback and expand photo | Not run | |
| Worker | Sign in; view assigned work and citizen feedback/photo; update allowed status; upload completion photo and remarks; submit one rating/comment/photo; view workload/performance | Not run | |
| Admin | Sign in; view dashboard statistics; search/filter/sort/page complaints; assign/reassign and escalate overdue case; manage worker details/activation; review both feedback types and expand photos | Not run | |

## Automated browser smoke test

| Workflow | Result | Evidence |
| --- | --- | --- |
| Admin selects Admin Login, signs in with the seeded local account, and reaches the administrator dashboard | Pass — 2026-11-02 | Playwright test: swcms-frontend/e2e/admin-login.spec.js; HTML report: evidence/browser-ui/index.html |

