package org.vaadin.example;

import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;

public class PersonDataProviderInMemory extends AbstractBackEndDataProvider<Person, CrudFilter> {

    final static Map<Integer, Person> DATABASE = new HashMap<>();
    private static Consumer<Long> sizeChangeListener;

    public PersonDataProviderInMemory() {
    }

    @Override
    protected Stream<Person> fetchFromBackEnd(Query<Person, CrudFilter> query) {
        int offset = query.getOffset();
        int limit = query.getLimit();

        Stream<Person> stream = DATABASE.values().stream();

        if (query.getFilter().isPresent()) {
            stream = stream.filter(predicate(query.getFilter().get()))
                    .sorted(comparator(query.getFilter().get()));
        }

        return stream.skip(offset).limit(limit);
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
        if (item.getId() == null) {
            item.setId(DATABASE.keySet().stream().max(naturalOrder()).orElse(0) + 1);
        }
        DATABASE.put(item.getId(), item);
        
    }



    Optional<Person> find(Integer id) {
        return Optional.ofNullable(DATABASE.get(id));
    }

    void delete(Person item) {
        DATABASE.remove(item.getId());

    }

}