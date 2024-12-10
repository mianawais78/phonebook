# Vaadin CRUD Application

## Description
This project is a Vaadin-based CRUD application (Phonebook) for managing a phonebook of persons. It includes functionalities to create, read, update, and delete person records. The application uses an in-memory data provider and a MySQL database for data persistence.

## Installation

### Prerequisites
- Java 17
- Vaadin
- Maven
- MySQL
- Jetty

### Setup
1. Clone the repository:
    ```sh
    git clone https://github.com/mianawais78/vaadin-crud-application.git
    cd vaadin-crud-application
    ```

2. Configure the MySQL database:
    - Create a database named `phonebook`.
    - Update the `jdbcUrl`, `jdbcUser`, and `jdbcPassword` in `src/main/java/org/vaadin/example/PersonDataProviderDb.java` with your MySQL credentials.

3. Build the project:
    ```sh
    mvn clean install
    ```

4. Run the application:
    ```sh
    mvn jetty:run
    ```

## Usage
- Open your web browser and navigate to `http://localhost:8080`.
- Use the CRUD interface to manage person records.

## Project Structure
- `src/main/java/org/vaadin/example/MainView.java`: Main view of the application.
- `src/main/java/org/vaadin/example/Person.java`: Person entity class.
- `src/main/java/org/vaadin/example/PersonDataProviderInMemory.java`: In-memory data provider.
- `src/main/java/org/vaadin/example/PersonDataProviderDb.java`: MySQL data provider.

## Validations
The application includes the following validations:
- **Name**: Required and must be at least 3 characters long.
- **Street**: Required.
- **City**: Required.
- **Country**: Required.
- **Phone Number**: Required, must be 11 digits, and must be unique.
- **Email**: Required and must be a valid email address.

## Data Providers
The application supports two types of data providers:
1. **In-Memory Data Provider**: Used for development and testing purposes.
2. **MySQL Data Provider**: Used for production with data persistence in a MySQL database.

## Application Flow
1. The user interacts with the CRUD interface to manage person records.
2. The application validates the input fields based on the defined validations.
3. The data is either stored in the in-memory data provider or the MySQL database, depending on the configuration.
4. The application updates the UI to reflect the changes made to the person records.

## License
This project is licensed under the MIT License.