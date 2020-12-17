# Rod Catch Returns API
[![Build Status](https://github.com/defra/rod-catch-returns-api/workflows/build.yml/badge.svg)](https://github.com/defra/rod-catch-returns-api/actions)
[![Maintainability](https://api.codeclimate.com/v1/badges/5a286ee063b6b20e6129/maintainability)](https://codeclimate.com/github/DEFRA/rod-catch-returns-api/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/5a286ee063b6b20e6129/test_coverage)](https://codeclimate.com/github/DEFRA/rod-catch-returns-api/test_coverage)
[![Licence](https://img.shields.io/badge/Licence-OGLv3-blue.svg)](http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3)

Spring Data REST API for Rod Catch Returns.

When you purchase a migratory salmon and sea trout rod licence, you are legally required to submit a catch return, which is a log of the fish caught that season. Users are sent a paper copy of the catch return form with their licence, and also directed towards the catch return website.

Users are asked to submit details of the fish they caught (species, weight, number etc) and where they caught them.

This API provides validation and persistence capabilities for Rod Catch Returns data.


## Cloning
Cloning via SSH from behind a corporate firewall which blocks port 22:
```bash
git clone ssh://git@ssh.github.com:443/DEFRA/rod-catch-returns-api
```

## Prerequisites

- Java 11
- (Optional) Maven 3.54 or greater (or use the supplied mvnw wrapper)

### Installing Java 11 on MacOS Catalina

To check your java version, do `java -version`. If you're not using v11, do:
```bash
sdk i java 11.0.2-open
```
Followed by
```bash
sdk default java 11.0.2-open
```
To default to Java 11.
This presumes you have sdkman installed, see here if you don't: https://sdkman.io/install
Then add this to your shell config file (e.g. `.zshrc`):
```
export SDKMAN_DIR="~/.sdkman"
[[ -s "~/.sdkman/bin/sdkman-init.sh" ]] && source "~/.sdkman/bin/sdkman-init.sh"
```

## Use with Visual Studio Code

If you're working with Visual Studio Code, you'll need some additional extensions:
- Lombok Annotations Support
- Spring Boot Tools
- Maven for Java

## Local Environment

The launches/docker-stack/rcr-local-services-stack.yml config exists to provide local Postgres and Redis instances:
```bash
docker stack deploy -c launches/docker-stack/rcr-local-services-stack.yml rcr
```
Create a local .env file:
```bash
cp .env.example .env 
```
Then initialise the database:
```bash
launches/dbctl init
```
Finally, run the migrations:
```bash
launches/dbctl update
```
After this, you can launch in default profile rather than using the in-memory database

## Launching via the spring-boot maven plugin (development use)

Run with default profile (using configured database):
```bash
launches/serverctl run
```

Run with in-memory database for testing:
```bash
launches/serverctl run --spring.profiles.active=h2
```

## Compilation

```bash
./mvnw clean
./mvnw compile
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

## Configuration

### App Config
This is done in src/main/resources/application.yml, which is setup for local development. These settings are overridden by values in the rod-catch-returns-deployments repo in GitLab. 
To overwrite them locally, use an .env file in the root of the repo. Each setting can be overridden by following the nesting, but transforming each setting to uppercase and delimiting with an underscore. For example, to override this:
```yaml
here:
  is:
    a:
      setting: abc
```
Put this into an .env file:
```
HERE_IS_A_SETTING=def
```

### Database config
The liquibase database migrations are held in xml files, in src/main/resources/db/changelog. The naming convention is hopefully obvious, just continue suffixing a version number as new versions are published, and making sure the new migration file is referenced in the master file in the correct sequence. The master file is referenced by the liquibase.properties file.

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
