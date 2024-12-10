package org.vaadin.example;

import com.vaadin.flow.data.provider.Query;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MainViewTest {

    @InjectMocks
    private static PersonDataProviderDb dataProvider;

    @BeforeAll
    public static void setup() throws SQLException {
        DataSource dataSource = createDataSource();
        initializeDatabase(dataSource);
        dataProvider = new PersonDataProviderDb("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "sa");
    }

    private static DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");
        return dataSource;
    }

    private static void initializeDatabase(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE Person (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "lname VARCHAR(255)," +
                    "street VARCHAR(255)," +
                    "city VARCHAR(255)," +
                    "country VARCHAR(255)," +
                    "phoneNumber VARCHAR(255) UNIQUE ," +
                    "email VARCHAR(255)," +
                    "flag BOOLEAN" +
                    ")");
            statement.execute("INSERT INTO Person (name, lname, street, city, country, phoneNumber, email, flag) VALUES " +
                    "('John', 'Doe', '123 Main St', 'Springfield', 'USA', '555-1234', 'john.doe@example.com', FALSE)");
        }
    }

    @Test
    public void testFetchFromBackEnd() {
        List<Person> persons = dataProvider.fetchFromBackEnd(new Query<>()).toList();
        assertEquals(1, persons.size());
        assertEquals("Jane Doe", persons.get(0).getFirstName());
    }

    @Test
    public void testSizeInBackEnd() {
        int size = dataProvider.sizeInBackEnd(new Query<>());
        assertEquals(1, size);
    }

    @Test
    public void testPersistNewPerson() throws SQLException {
        Person person = new Person();
        person.setFirstName("Jane Doe");
        person.setStreet("456 Elm St");
        person.setCity("Metropolis");
        person.setCountry("USA");
        person.setPhoneNumber("555-5678");
        person.setEmail("jane.doe@example.com");
        person.setFlag(false);

        dataProvider.persist(person);

        List<Person> persons = dataProvider.fetchFromBackEnd(new Query<>()).toList();
        assertEquals(2, persons.size());
        assertEquals("Jane Doe", persons.get(1).getFirstName());
    }

    @Test
    public void testUpdatePerson() {
        Person person = dataProvider.fetchFromBackEnd(new Query<>()).toList().get(0);

        person.setFirstName("Jane Smith");
        person.setStreet("789 Oak St");
        person.setCity("Gotham");
        person.setCountry("USA");
        person.setPhoneNumber("555-9876");
        person.setEmail("jane.smith@example.com");

        dataProvider.persist(person);

        Person updatedPerson = dataProvider.findById(person.getId());

        assertNotNull(updatedPerson);
        assertEquals("Jane Smith", updatedPerson.getFirstName());
        assertEquals("789 Oak St", updatedPerson.getStreet());
        assertEquals("Gotham", updatedPerson.getCity());
        assertEquals("USA", updatedPerson.getCountry());
        assertEquals("555-9876", updatedPerson.getPhoneNumber());
        assertEquals("jane.smith@example.com", updatedPerson.getEmail());
    }

    @Test
    public void testDeletePerson() {
        Person person = dataProvider.fetchFromBackEnd(new Query<>()).findFirst().orElse(null);
        assertNotNull(person);

        dataProvider.delete(person);

        List<Person> persons = dataProvider.fetchFromBackEnd(new Query<>()).toList();
        assertEquals(1, persons.size());
    }

    @Test
    public void testFindById() {
        Person person = dataProvider.findById(1);
        assertNotNull(person);
        assertEquals("Jane Smith", person.getFirstName());
    }
}