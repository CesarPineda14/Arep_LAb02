# Microframeworks WEB

## Overview
SimpleWebFramework is a lightweight Java-based web framework designed to simplify the development of web applications. It provides an easy way to define REST services, handle static files, and extract query parameters from requests. This framework is ideal for developers looking to build web applications with minimal configuration and clear route definitions.

## Features

1. GET Static Method for REST Services:
   * Define REST services using lambda functions.
   * Example:
       ```
        get("/hello", (req, res) -> "hello world!");
      ```
2. Query Value Extraction Mechanism:
   * Extract and access query parameters from incoming requests.
   * Example:
     ```
        get("/hello", (req, res) -> "hello " + req.getValues("name"));

     ```
3. Static File Location Specification:
   * Specify the directory for static files.
   * Example:
     ```
        staticfiles("webroot/public");

     ```
4. Example Usage:
   * Define and serve a web application with static files and REST services.
   * Example:
     ```
        public static void main(String[] args) {
            staticfiles("/webroot");
            get("/hello", (req, resp) -> "Hello " + req.getValues("name"));
            get("/pi", (req, resp) -> String.valueOf(Math.PI));
        }


     ```
## Getting Started

  ### Prerequisites

  * Java 21
  * Maven
  * Git

  ### Installation

  1. Clone the repository:
     ```
        git clone https://github.com/CesarPineda14/Arep_LAb02.git

     ```
  2. Navigate to the project directory:
     ```
        cd Arep_LAb02

     ```
   3. Build the project using Maven:
      ```
       mvn clean package

      ```
  4. Run the application:
      ```
    
       java -jar target/SimpleWebServer-1.0-SNAPSHOT-jar-with-dependencies.jar

      ```
5. Access the following URL:
   * http://localhost:8080/index.html


## Project Structure

* 'src/main/java': Source code for the framework and examples.
* 'src/main/resources': Configuration files and static resources.
* 'target': Compiled classes and packaged application.
* 'pom.xml': Maven build configuration.


## Testing

* Unit tests are located in the 'src/test/java' directory.
* Run tests using Maven:
  ```
    
       mvn test

  ```
  
