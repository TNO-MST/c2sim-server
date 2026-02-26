# C2SIM-server unit testing

## JUnit

The solution uses JUnit 5 (Jupiter) for unit testing.

All unit tests are located in `src/test/java` within each module.

⚠️ **In IntelliJ IDE there was no support for JUnit 6, this is the reason for Junit 5.**

## Running Unit Tests

Unit testing is integrated into the standard Maven build process.

Full Build (includes tests)

The `unit testing` is integrated in the automatic build

```
mvn clean verify
```

Run Only Unit Tests:

```
mvn clean test
```

## Temporarily Disable Unit Tests

To skip unit tests during a build:

```
mvn clean verify -DskipTests
```

## CI Integration

Tests are automatically executed during:

```
mvn verify
```

CI will fail if:

- A test fails
- A test throws an exception
- The build fails

## Mocking & Dependency Injection

The following frameworks are used for testing:

* [Google Guice]((https://github.com/google/guice)  – Dependency Injection

* [Mockito](https://site.mockito.org/) – Mocking framework

These frameworks help isolate components during unit testing.

## Test Reports

The test reports can be found in folder `<module>\target\surefire-reports`



## Code coverage

The project uses [**JaCoCo**](https://www.jacoco.org/jacoco/) to measure unit test code coverage.  

⚠️ **Work in progress; the current unit test only checks functionality. Not all classes and method are unit tested. This results in a very low coverage score***

The code coverage HTML test report can be found in `c2sim-server\target\site\jacoco-aggregate`.


