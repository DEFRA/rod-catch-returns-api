# Rod Catch Returns API
[![Build Status](https://travis-ci.org/DEFRA/rod-catch-returns-api.svg?branch=master)](https://travis-ci.org/DEFRA/rod-catch-returns-api)
[![Maintainability](https://api.codeclimate.com/v1/badges/5a286ee063b6b20e6129/maintainability)](https://codeclimate.com/github/DEFRA/rod-catch-returns-api/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/5a286ee063b6b20e6129/test_coverage)](https://codeclimate.com/github/DEFRA/rod-catch-returns-api/test_coverage)
[![Licence](https://img.shields.io/badge/Licence-OGLv3-blue.svg)](http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3)

Spring Data REST API for Rod Catch Returns.


## Prerequisites

- Java 1.8
- (Optional) Maven 3.52 or greater (or use the supplied mvnw wrapper)


## Launching via the spring-boot maven plugin (development use)

Run with default profile (using configured database):
```bash
launches/serverctl run
```

Run with in-memory database for testing:
```bash
launches/serverctl run --spring.profiles.active=h2
```

## Verifying changes

To run unit and integration tests:
```bash
./mvnw verify
```

Full verification with OWASP dependency security checks:
```bash
./mvnw -P full-verify verify
```

## Generating site reports
To generate all site reports to target/site:

```bash
./mvnw verify site
```

This report includes:

| Document          | Description |
| ---               | ---         |
|Javadoc            | Javadoc API documentation. |
|Test Javadoc       | Test Javadoc API documentation.|
|Surefire Report    | Report on the test results of the project.|
|Failsafe Report	| Report on the integration test results of the project. |
|Checkstyle         | Report on coding style conventions.|
|Source Xref        | HTML based, cross-reference version of Java source code.|
|Test Source Xref	| HTML based, cross-reference version of Java test source code.|
|FindBugs	        | Generates a source code report with the FindBugs Library.|
|JaCoCo	            | JaCoCo Coverage Report.|


To include the OWASP dependency security report:

```bash
./mvnw -P full-verify verify site
```

In addition to the reports listed above, this includes:

| Document          | Description |
| ---               | ---         |
|dependency-check	|Generates a report providing details on any published vulnerabilities within project dependencies. This report is a best effort and may contain false positives and false negatives.|


## Building a jar with an embedded server
```bash
./mvnw package
```

## Building a docker container image
```bash
./mvnw dockerfile:build
```
The new image will be installed into the local docker repository under drp/rcr_api:latest


## Maven settings reference
Example maven settings to use a local postgres database:
```xml
<!-- Configuration file values for database access -->
<db.rcr_api.mgt.url>jdbc:postgresql://localhost:5432/postgres</db.rcr_api.mgt.url>
<db.rcr_api.url>jdbc:postgresql://localhost:5432/rcr_api</db.rcr_api.url>
<!-- DB name (as per url above) -->
<db.rcr_api.name>rcr_api</db.rcr_api.name>
<db.rcr_api.type>postgresql</db.rcr_api.type>
<db.rcr_api.driver>org.postgresql.Driver</db.rcr_api.driver>
<db.rcr_api.dialect>org.hibernate.dialect.PostgreSQL94Dialect</db.rcr_api.dialect>
<db.rcr_api.username>a username</db.rcr_api.username>
<db.rcr_api.password>a password</db.rcr_api.password>
<db.rcr_api.schema>public</db.rcr_api.schema>
```

## Contributing to this project

If you have an idea you'd like to contribute please log an issue.

All contributions should be submitted via a pull request.

## License

THIS INFORMATION IS LICENSED UNDER THE CONDITIONS OF THE OPEN GOVERNMENT LICENCE found at:

http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3

The following attribution statement MUST be cited in your products and applications when using this information.

>Contains public sector information licensed under the Open Government license v3

### About the license

The Open Government Licence (OGL) was developed by the Controller of Her Majesty's Stationery Office (HMSO) to enable information providers in the public sector to license the use and re-use of their information under a common open licence.

It is designed to encourage use and re-use of information freely and flexibly, with only a few conditions.
