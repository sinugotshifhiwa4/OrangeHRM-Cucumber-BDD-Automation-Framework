Here’s the updated README with JUnit 5 details:

---

# OrangeHRM-Cucumber-BDD-Automation-Framework

## Overview
This project is a Cucumber-based test automation framework for testing the OrangeHRM application. It supports API testing using Cucumber and RestAssured while integrating manual encryption for authentication credentials.

## Prerequisites
Before running the tests, ensure the following prerequisites are met:

1. **Java:** Install JDK 8 or later.
2. **Maven:** Ensure Apache Maven is installed.
3. **Environment Configuration:** Set up an environment properties file (`uat.properties`, `dev.properties`, etc.).
4. **Manual Encryption:** Encrypt authentication credentials manually before running tests.

## Running Tests

To execute tests in the UAT environment, use:
```sh
mvn clean test -Denv=uat
```  

## Test Approach

- **Behavior-Driven Development (BDD):** Uses Cucumber to structure API test cases.
- **Feature Files:** Test scenarios are defined in `.feature` files.
- **Step Definitions:** Implements step definitions using RestAssured for API validation.
- **JUnit 5:** Supports parallel test execution for improved efficiency.

## Test Data Management
- Test data is managed through feature file parameters and environment properties.

## Dependencies
- **Cucumber** (For BDD-based testing)
- **JUnit 5** (For test execution & parallel execution support)
- **Log4j2** (For logging)

## Environment Configuration
Ensure your environment properties file contains:
```
PORTAL_USERNAME=your_encrypted_username  
PORTAL_PASSWORD=your_encrypted_password  
```  
Manually encrypt credentials before running tests.

---