# erupt-test

JUnit 5 test suite for the Erupt framework core.

Runs integration tests against an H2 in-memory database configured in `src/test/resources/application.yml`. Uses [Instancio](https://instancio.org/) for random test data generation. `EruptApplicationTests` provides a login helper as the base class for all test cases.

```bash
mvn test -pl erupt-test
mvn test -pl erupt-test -Dtest=EruptTest#modules
```
