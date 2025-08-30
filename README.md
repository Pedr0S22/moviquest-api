# MovieQuest API  

MovieQuest is an API built using **Java Spring Boot** and **MongoDB**, designed to manage a collection of movies and their user reviews. It supports features such as creating, updating, and retrieving movie data, as well as adding and managing user-generated reviews.  

More than just a codebase, MovieQuest is a **learning project** where I explored modern backend development practices, scalable architectures, and real-world patterns in software engineering.  

---

## Table of Contents  
- [Project Overview](#project-overview)  
- [User Roles, Functionalities, and API Endpoints](#user-roles-functionalities-and-api-endpoints)
  - [Movies-Related Functionalities](#movies-related-functionalities) 
  - [Review-Related Functionalities](#review-related-functionalities)
  - [Only Administrator-Related Functionalities](#only-administrator-related-functionalities)
  - [General Functionalities](#general-functionalities)  
- [Setup Instructions](#setup-instructions)    
- [Authors](#authors)  

---

## Project Overview  

### MovieQuest API Documentation  

This document aims to help developers and users understand how to install, run, and interact with the MovieQuest RESTful API, while also describing the architectural and technical choices made throughout the project.  

This project uses:  
- **Core Framework**: Spring Boot (MVC, Data, Security)  
- **Database**: MongoDB (NoSQL, JSON-like document structure)  
- **Security**: JWT-based authentication, Spring Security, role-based access (Admin/User), encryption techniques  
- **Testing**: JUnit, Mockito, initial exploration of integration testing for CI pipelines  
- **Deployment**: Docker containerization and GitHub Actions for CI/CD (with publishing to Docker Hub)  
- **Logging & Monitoring**: Logback + SLF4J, persisted logs in MongoDB and JSON rolling files  
- **Resilience**: Resilience4j (Retry, Circuit Breakers, Time Limits) for fault tolerance  
- **Other Java Concepts Applied**: Threads, Thread Pools, Concurrency, Generics, Virtual Threads (explored conceptually)  

### Project Objectives  

The purpose of MovieQuest was to **simulate a production-grade backend system** while sharpening my engineering skills. The key objectives were:  

- Strengthen knowledge of **Java Spring Boot**, focusing on clean, maintainable, and testable code.  
- Gain hands-on experience with **NoSQL databases** (MongoDB).  
- Learn and apply **containerization (Docker)** and **CI/CD (GitHub Actions)** for modern deployment workflows.  
- Explore **API security** with JWT tokens, Spring Security, and role-based access.  
- Develop **unit tests** (JUnit, Mockito) and begin experimenting with integration tests for automated pipelines.  
- Improve understanding of **RESTful API design** and CRUD operations.  
- Implement robust **logging and monitoring**, including persisting logs and using background threads for efficiency.  
- Experiment with **resilience patterns** (Resilience4j) to build fault-tolerant APIs.

### Other Project Features and Documentation

The project is well documented using **Javadocs**. You can access the API documentation in your browser:  
   - Open `docs/apidocs/index.html` to view the full generated documentation.  

The `logs/movies-api` folder contains rolling log files for one month in JSON format (powered by Logback). This allows for tracking errors and application activity efficiently.   

---

## User Roles Functionalities and API Endpoints  

**Important Notes Before Using the Endpoints:** 

1. These functionalities were tested with **Postman**. In this repository, there is a JSON file containing the Postman collection I created to test the app. You can download and import it for your own usage:  
   - **File name**: `API_MovieQuest_postman_collection.json`  

2. **All endpoints require authentication** to be used. You need to register and login to obtain a JWT access token. For each request (except login and signup), include the token in the request headers as a **Bearer Access Token**.  

### Movies-Related Functionalities

1. **Get All Movies**  
   - **Endpoint**: `GET /api/v1/movies`  
   - **Result**: Returns a list of all movies in the database.

2. **Get Movie by ID**  
   - **Endpoint**: `GET /api/v1/movies/{imdbId}`  
   - **Result**: Returns detailed information about a specific movie.
  
3. **Search Movies with filters**
    - **EndPoint**:  `POST /api/v1/movies/search`
    - **[Optional] Input Parameters**: "title", "genres", "releaseDateAfter", "releaseDateBefore".
    - **Note**: If no filter is applied, this endpoint have the same output as "Get All Movies".

4. **Add Movie**  
   - **Endpoint**: `POST /api/v1/movies/newMovie`  
   - **[Required] Input Parameters**: "title", "imdbId", "genres", releaseDate"
   - **[Optional] Input Parameters**: "trailerLink", "poster", "backdrops".
   - **Result**: Creates a new movie entry in the database and returns the movie created.
   - **Note**: Only admins can use this endpoint.
  
5. **Update Movie**
    - **Endpoint**: `PATCH /api/v1/movies/update/{imdbId}`  
    - **[Optional] Input Parameters**: "title", "releaseDate", "genres", "trailerLink", "poster", "backdrops".
    - **Result**: Updates a movie in the database and returns the movie updated.
    - **Note**: Only admins can use this endpoint.
  
6. **Delete Movie**  
   - **Endpoint**: `DELETE /api/v1/movies/delete/{imdbId}`  
   - **Result**: Deletes the movie with the given `imdbId`.
   - **Note**: Only admins can use this endpoint.

### Review-Related Functionalities

1. **Add Review**  
   - **Endpoint**: `POST /api/v1/reviews`  
   - **[Required] Input Parameters**: "imdbId", "body".  
   - **Result**: Creates a review associated with the given `imdbId`.  

2. **Update Review**  
   - **Endpoint**: `PATCH /api/v1/reviews/update/{id}`  
   - **[Required] Input Parameters**: "imdbId", "body".    
   - **Result**: Updates the specified review with the input parameters.
   - **Note**: Only the creator of that review can use this endpoint. The admins *don't* have access to this endpoint; By `{id}` I refer to the review Id showed while retrieving a movie with reviews.

3. **Delete Review**  
   - **Endpoint**: `DELETE /api/v1/reviews/delete/{imdbId}/{id}`  
   - **Result**: Deletes the indicated review.
   - **Note**: Only the creator of that review *or* admins can use this endpoint.

### Only Administrator-Related Functionalities

1. **Actuator** Monitoring
    - **Endpoint**: `GET /actuator`  
    - **Result**: Returns a list of all Actuator Endpoints. Its crucial to have access to all operational information about the running application.

2. **General Actuator Endpoints**
    - **Endpoint**: `GET /actuator/{endpoint}`  
    - **Result**: Returns a JSON of the operational information chosen in `{endpoint}`.

3. **Log Search**
    - **EndPoint**:  `POST /api/v1/logging/search`
    - **[Optional] Input Parameters**: "level", "timestampAfter", "timestampBefore", "logger", "thread", "mdc", "messageKeywords", [Boolean] "sortByTimestamp" (default set to true).
    - **Note**: If no filter is applied, this endpoint returns all logs that exists in database.

### General Functionalities

1. **User Registration**  
   - **Endpoint**: `POST /api/v1/auth/signup`  
   - **[Required] Input Parameters**: "username", "email, "password".
   - **[Optional] Input Parameters**: "roles". By default, the user gets user privileges, even if its empty.  
   - **Result**: Registers a new user and returns its informations, with the exception of the encrypted password.  

2. **User Login**  
   - **Endpoint**: `POST /api/v1/auth/login`  
   - **[Required] Input Parameters**: "username", "password".
   - **Result**: Returns a JWT token for authentication along with the user informations, with the exception of the encrypted password.

---

## Setup Instructions  

**Important Note for Both Setups**

Open the `.env.example` file in the project (in `src/main/java/resources`), fill in all required environment variables, and save it as `.env`. This ensures the application has all necessary configuration for MongoDB, JWT secrets, and other environment-specific settings.

### 1. Setup to Initialize the App Locally Using an IDE

---

1.1. **Clone the repository** and open it in your preferred IDE (IntelliJ IDEA, Eclipse, VS Code, etc.):  
   ```bash
   git clone https://github.com/Pedr0S22/moviquest-api.git
   ```

1.2. **Build the project** using Maven:
   ```bash
   mvn clean install
   ```

1.3. **Run** the application:

- **Through IDE**: Run the *MoviesApiApplication.java* file (in `src/main/java/dev/Pedro/movies_api`) directly from your IDE.
- **Through Terminal**: ```mvn spring-boot:run```

1.4. **Access the app** in your browser or Postman with:
   ```bash
   http://localhost:8080/<ENDPOINTS>
   ```

### 2. Setup to Initialize the App Using Docker

---

2.1. Install [Docker Desktop](https://www.docker.com/products/docker-desktop/) if not installed yet.

2.2. Ensure you are logged in to Docker - through Docker Desktop or at the Teminal using Docker ```Docker login <docker_hub_username>```.

2.3. Search for the project image in Docker Desktop or at the Terminal (optional):
  ```bash
   docker search k45q4ckc/movies-api
   ```

2.4. **Pull** the project image:
  ```bash
   docker pull k45q4ckc/movies-api
   ```

2.5. **Run** the Docker container:
  ```bash
   docker run -d -p 8080:8080 --env-file "<path_to_.env_file>.env" k45q4ckc/movies-api:latest
   ```

2.6. **Access the app** in your browser or Postman with:
   ```bash
   http://localhost:8080/<ENDPOINTS>
   ```
