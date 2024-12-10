package org.vaadin.example;


import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The main view contains a text field for getting the user name and a button
 * that shows a greeting message on a new line.
 */


@Route("")

public class MainView extends VerticalLayout {

    private Crud<Person> crud;


    private String FIRST_NAME = "firstName";
    private String LAST_NAME = "lastName";
    private String STREET = "street";
    private String CITY = "city";
    private String COUNTRY = "country";
    private String PHONE_NUMBER = "phoneNumber";
    private String EMAIL = "email";
    private String EDIT_COLUMN = "vaadin-crud-edit-column";
    private final static PersonDataProviderDb dataProvider = new PersonDataProviderDb();
    private final static ConcurrentHashMap<Integer, Integer> personViewed = new ConcurrentHashMap<>();
    private static Integer viewCount = 0;
    //private final static PersonDataProviderInMemory dataProvider = new PersonDataProviderInMemory();



    public MainView() {
        crud = new Crud<>(Person.class, createEditor());
        crud.setDataProvider(dataProvider);
        setupGrid();
        setupDataProvider();


        add(crud);

    }
    public CrudEditor<Person> createEditor() {
        TextField firstName = new TextField("First name");
        TextField lastName = new TextField("Last name");
        TextField street = new TextField("Street");
        TextField city = new TextField("City");
        TextField country = new TextField("Country");
        TextField phoneNumber = new TextField("Phone number");
        EmailField email = new EmailField("Email");
        FormLayout form = new FormLayout(firstName, lastName, street, city, country, phoneNumber, email);

        Binder<Person> binder = new Binder<>(Person.class);

        binder.forField(firstName)
                .asRequired("First Name is required")
                .withValidator(nameValue -> nameValue.length() >= 3, "Name must be at least 3 characters long")
                .withValidationStatusHandler(status -> {
                    if (status.isError()) {
                        firstName.setErrorMessage(status.getMessage().orElse(""));
                        firstName.setInvalid(true);
                    } else {
                        firstName.setInvalid(false);
                    }
                })
                .bind(Person::getFirstName, Person::setFirstName);
        binder.forField(lastName)
                .asRequired("Last Name is required")
                .withValidator(nameValue -> nameValue.length() >= 3, "Name must be at least 3 characters long")
                .withValidationStatusHandler(status -> {
                    if (status.isError()) {
                        lastName.setErrorMessage(status.getMessage().orElse(""));
                        lastName.setInvalid(true);
                    } else {
                        lastName.setInvalid(false);
                    }
                })
                .bind(Person::getLastName, Person::setLastName);

        binder.forField(street).asRequired().bind(Person::getStreet, Person::setStreet);
        binder.forField(city).asRequired().bind(Person::getCity, Person::setCity);
        binder.forField(country).asRequired().bind(Person::getCountry, Person::setCountry);
        binder.forField(phoneNumber)
                .asRequired("Phone number is required")
                .withValidator(phone -> phone.matches("\\d{11}"), "Phone number must be 11 digits")
                .withValidator(phone -> isPhoneNumberUnique(crud.getEditor().getItem(), phone), "Phone number must be unique")
                .withValidationStatusHandler(status -> {
                    if (status.isError()) {
                        phoneNumber.setErrorMessage(status.getMessage().orElse(""));
                        phoneNumber.setInvalid(true);
                    } else {
                        phoneNumber.setErrorMessage(null);
                        phoneNumber.setInvalid(false);
                    }
                })
                .bind(Person::getPhoneNumber, Person::setPhoneNumber);

        binder.forField(email)
                .asRequired("Email is required")
                .withValidationStatusHandler(status -> {
                    if (status.isError()) {
                        email.setErrorMessage("Please enter a valid email address");
                        email.setInvalid(true);
                    } else {
                        email.setErrorMessage(null);
                        email.setInvalid(false);
                    }
                })
                .bind(Person::getEmail, Person::setEmail);
        return new BinderCrudEditor<>(binder, form);
    }


    void setupGrid() {
        Grid<Person> grid = crud.getGrid();
        Crud.removeEditColumn(grid);
        grid.addItemDoubleClickListener(event -> crud.edit(event.getItem(),
                Crud.EditMode.EXISTING_ITEM));

        // Only show these columns (all columns shown by default):
        List<String> visibleColumns = Arrays.asList(FIRST_NAME, LAST_NAME, EMAIL);
        grid.getColumns().forEach(column -> {
            String key = column.getKey();
            if (!visibleColumns.contains(key)) {
                grid.removeColumn(column);
            }
        });

        // Reorder the columns (alphabetical by default)
        List<Grid.Column<Person>> columns = Arrays.asList(
                grid.getColumnByKey(FIRST_NAME),
                grid.getColumnByKey(LAST_NAME),
                grid.getColumnByKey(STREET),
                grid.getColumnByKey(CITY),
                grid.getColumnByKey(COUNTRY),
                grid.getColumnByKey(PHONE_NUMBER),
                grid.getColumnByKey(EMAIL),
                grid.getColumnByKey(EDIT_COLUMN)
        );

        // Filter out null columns
        columns = columns.stream().filter(Objects::nonNull).toList();

        grid.setColumnOrder(columns);
    }

private void setupDataProvider() {
    Notification notification = new Notification("Someone else is updating this record", 2000);
    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    crud.setDataProvider(dataProvider);

    crud.addSaveListener(saveEvent -> {
        synchronized (dataProvider) {
            dataProvider.persist(saveEvent.getItem());
            dataProvider.refreshAll();
        }
    });

    crud.addCancelListener(cancelEvent -> {
        synchronized (dataProvider) {
            Person person = cancelEvent.getItem();
            if (personViewed.get(person.getId()) > 0) {
                personViewed.put(person.getId(), personViewed.get(person.getId()) - 1);
            }
            dataProvider.persist(cancelEvent.getItem());
            cancelEvent.getSource().getSaveButton().setEnabled(true);
            cancelEvent.getSource().getDeleteButton().setEnabled(true);
        }
    });

    crud.addEditListener(editEvent -> {
        Person person = editEvent.getItem();
        if (!personViewed.containsKey(person.getId())) {
            personViewed.put(person.getId(), 0);
        }

        synchronized (dataProvider) {
            if (personViewed.get(person.getId()) > 0) {
                System.out.println("Hello");
                notification.open();
                editEvent.getSource().getSaveButton().setEnabled(false);
                editEvent.getSource().getDeleteButton().setEnabled(false);
                personViewed.put(person.getId(), personViewed.get(person.getId()) + 1);
//                dataProvider.refreshAll();
            } else {
                personViewed.put(person.getId(), personViewed.get(person.getId()) + 1);

                editEvent.getSource().addSaveListener(saveEvent -> {
                    synchronized (dataProvider) {
                        if (personViewed.get(person.getId()) > 0) {
                            personViewed.put(person.getId(), personViewed.get(person.getId()) - 1);
                        }
                        System.out.println(person.getFlag());
                        editEvent.getSource().getSaveButton().setEnabled(true);
                        editEvent.getSource().getDeleteButton().setEnabled(true);
                        dataProvider.persist(saveEvent.getItem());
                    }
                });
                editEvent.getSource().addDeleteListener(deleteEvent -> {
                    synchronized (dataProvider) {
                        dataProvider.delete(deleteEvent.getItem());
                    }
                });
            }
        }
    });

    crud.addNewListener(newEvent -> {
        newEvent.getSource().getSaveButton().setEnabled(true);
        newEvent.getSource().getDeleteButton().setEnabled(true);
    });

}


    private boolean isPhoneNumberUnique(Person person, String phoneNumber) {
        CrudFilter filter = new CrudFilter();
        filter.getConstraints().put("phoneNumber", phoneNumber);
        List<Person> persons = dataProvider.fetchFromBackEnd(new Query<>(filter)).toList();
        if(person.getId()==null){
            return persons.stream().noneMatch(p -> Objects.equals(p.getPhoneNumber(), phoneNumber));
        }

        return persons.stream().noneMatch(p -> p.getPhoneNumber().equals(phoneNumber) && !p.getId().equals(person.getId()));
    }



}