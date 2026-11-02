# Backend test and coverage evidence

- Run date: 2026-11-02
- Command: mvn verify
- Result: BUILD SUCCESS; 24 tests, 0 failures, 0 errors, 0 skipped.
- Workflow coverage: the citizen/worker/admin API end-to-end scenario passed.
- Report: index.html and jacoco.xml in this directory.

| JaCoCo counter | Covered | Total | Coverage |
| --- | ---: | ---: | ---: |
| Instructions | 3,601 | 4,929 | 73.1% |
| Lines | 989 | 1,336 | 74.0% |
| Branches | 193 | 438 | 44.1% |
| Methods | 307 | 383 | 80.2% |
| Classes | 41 | 42 | 97.6% |
| Cyclomatic complexity | 338 | 603 | 56.1% |

The JaCoCo HTML report includes complexity counters at class and method level. The higher missed complexity in complaint service/controller code is a useful target for additional branch tests. These are measurements from the tests executed, not a claim that untested production scenarios are defect-free.
