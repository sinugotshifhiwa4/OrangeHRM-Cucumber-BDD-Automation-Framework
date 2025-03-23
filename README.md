# OrangeHRM-Cucumber-BDD-Automation-Framework

## Overview
This project is a Cucumber-based test automation framework designed for testing the OrangeHRM application. It supports API testing using Cucumber and RestAssured while integrating manual encryption for authentication credentials.

## Prerequisites
Before running the tests, ensure the following prerequisites are met:

1. **Java:** Install JDK 8 or later.
2. **Maven:** Ensure Apache Maven is installed.
3. **Environment Configuration:** Set up an environment properties file (`uat.properties`, `dev.properties`, etc.).
4. **Manual Encryption:** Encrypt authentication credentials manually before running tests.

## Encryption Setup
Encryption is handled manually since it is a one-time operation. Therefore, JUnit annotations are not used for this process; instead, a `main` method is used for execution.

### Steps to Set Up Encryption:

1. **Generate a Secret Key:**
    - Run the `generateSecretKey` test in the `EncryptionFlowTests` class.
    - Path: `src/test/java/com/orangehrm/tests/encryption/EncryptionFlowTests.java`
    - This will generate the `envs` directory and a `.env` file containing the generated secret key for the UAT environment.

2. **Manually Create an Encrypted Credentials File:**
    - Create a new file named `.env.uat` inside the `envs` directory.
    - Add the following key-value pairs to the file:
      ```
      PORTAL_USERNAME=your_encrypted_username
      PORTAL_PASSWORD=your_encrypted_password
      ```
    - The credentials can be obtained from the [OrangeHRM demo website](https://opensource-demo.orangehrmlive.com/).

3. **Encrypt Credentials:**
    - Run the `encryptCredentials` test in the `EncryptionFlowTests` class.
    - Path: `src/test/java/com/orangehrm/tests/encryption/EncryptionFlowTests.java`

After completing these steps, you are ready to run the tests.

## Running Tests
To execute tests in the UAT environment, use the following command:
```sh
mvn clean test -Puat-sanity -Denv=uat
```

## Test Approach
- **Behavior-Driven Development (BDD):** Uses Cucumber to structure API test cases.
- **Feature Files:** Test scenarios are defined in `.feature` files.
- **Step Definitions:** Implements step definitions using RestAssured for API validation.
- **JUnit 5:** Supports parallel test execution for improved efficiency.

## Test Data Management
Test data is managed through feature file parameters and environment properties.

## Dependencies
- **Cucumber** (For BDD-based testing)
- **JUnit 5** (For test execution & parallel execution support)
- **Log4j2** (For logging)

## Useful Links
- [OrangeHRM Demo Website](https://opensource-demo.orangehrmlive.com/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)

