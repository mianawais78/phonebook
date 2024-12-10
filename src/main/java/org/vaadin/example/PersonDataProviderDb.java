package org.vaadin.example;

import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PersonDataProviderDb extends AbstractBackEndDataProvider<Person, CrudFilter> {

    private static Consumer<Long> sizeChangeListener;
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;

    public PersonDataProviderDb() {
        this("jdbc:mysql://localhost:3306/phonebook", "root", "l1nx@3!");
    }

    public PersonDataProviderDb(String jdbcUrl, String jdbcUser, String jdbcPassword) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
    }

    @Override
    protected Stream<Person> fetchFromBackEnd(Query<Person, CrudFilter> query) {
        List<Person> resultList = new ArrayList<>();
        String sql = "SELECT * FROM Person";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Person person = new Person();
                person.setId(resultSet.getInt("id"));
                person.setFirstName(resultSet.getString("name"));
                person.setLastName(resultSet.getString("lname"));
                person.setStreet(resultSet.getString("street"));
                person.setCity(resultSet.getString("city"));
                person.setCountry(resultSet.getString("country"));
                person.setPhoneNumber(resultSet.getString("phoneNumber"));
                person.setEmail(resultSet.getString("email"));
                resultList.add(person);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Stream<Person> stream = resultList.stream();

        if (query.getFilter().isPresent()) {
            stream = stream.filter(predicate(query.getFilter().get()))
                    .sorted(comparator(query.getFilter().get()));
        }

        return stream.skip(query.getOffset()).limit(query.getLimit());
    }

    @Override
    protected int sizeInBackEnd(Query<Person, CrudFilter> query) {
        long count = fetchFromBackEnd(query).count();

        if (sizeChangeListener != null) {
            sizeChangeListener.accept(count);
        }

        return (int) count;
    }

    void setSizeChangeListener(Consumer<Long> listener) {
        sizeChangeListener = listener;
    }

    private static Predicate<Person> predicate(CrudFilter filter) {
        return filter.getConstraints().entrySet().stream()
                .map(constraint -> (Predicate<Person>) person -> {
                    try {
                        Object value = valueOf(constraint.getKey(), person);
                        return value != null && value.toString().toLowerCase()
                                .contains(constraint.getValue().toLowerCase());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }).reduce(Predicate::and).orElse(e -> true);
    }

    private static Comparator<Person> comparator(CrudFilter filter) {
        return filter.getSortOrders().entrySet().stream().map(sortClause -> {
            try {
                Comparator<Person> comparator = Comparator.comparing(
                        person -> (Comparable) valueOf(sortClause.getKey(), person));

                if (sortClause.getValue() == SortDirection.DESCENDING) {
                    comparator = comparator.reversed();
                }

                return comparator;

            } catch (Exception ex) {
                return (Comparator<Person>) (o1, o2) -> 0;
            }
        }).reduce(Comparator::thenComparing).orElse((o1, o2) -> 0);
    }

    private static Object valueOf(String fieldName, Person person) {
        try {
            Field field = Person.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(person);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    void persist(Person item) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)) {
            if (item.getId() == null) {
                String sql = "INSERT INTO Person (name,lname, street, city, country, phoneNumber, email) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, item.getFirstName());
                    statement.setString(2,item.getLastName());
                    statement.setString(3, item.getStreet());
                    statement.setString(4, item.getCity());
                    statement.setString(5, item.getCountry());
                    statement.setString(6, item.getPhoneNumber());
                    statement.setString(7, item.getEmail());
                    statement.executeUpdate();
                }
            } else {
                String sql = "UPDATE Person SET name = ?,lname = ?, street = ?, city = ?, country = ?, phoneNumber = ?, email = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, item.getFirstName());
                    statement.setString(2, item.getLastName());
                    statement.setString(3, item.getStreet());
                    statement.setString(4, item.getCity());
                    statement.setString(5, item.getCountry());
                    statement.setString(6, item.getPhoneNumber());
                    statement.setString(7, item.getEmail());
                    statement.setInt(8, item.getId());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException ignored) {

        }
    }

    void delete(Person item) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)) {
            String sql = "DELETE FROM Person WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, item.getId());
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Person findById(int id) {
        String sql = "SELECT * FROM Person WHERE id = ?";
        Person person = null;

        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                person = new Person();
                person.setId(resultSet.getInt("id"));
                person.setFirstName(resultSet.getString("name"));
                person.setLastName(resultSet.getString("lname"));
                person.setStreet(resultSet.getString("street"));
                person.setCity(resultSet.getString("city"));
                person.setCountry(resultSet.getString("country"));
                person.setPhoneNumber(resultSet.getString("phoneNumber"));
                person.setEmail(resultSet.getString("email"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return person;
    }
}