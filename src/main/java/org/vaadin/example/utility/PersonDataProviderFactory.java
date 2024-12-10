package org.vaadin.example.utility;

import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import org.vaadin.example.Person;
import org.vaadin.example.PersonDataProviderDb;
import org.vaadin.example.PersonDataProviderInMemory;

import java.util.Map;

public class PersonDataProviderFactory {

    public static AbstractBackEndDataProvider<Person, CrudFilter> createDataProvider() {
        AppConfig config = ConfigLoader.loadConfig();
        if (config == null) {
            throw new RuntimeException("Failed to load configuration");
        }

        String dbType = config.getDbType();
        Map<String, String> dbCredentials = config.getDbCredentials();

        switch (dbType.toLowerCase()) {
            case "db":
                return new PersonDataProviderDb(
                    dbCredentials.get("url"),
                    dbCredentials.get("user"),
                    dbCredentials.get("password")
                );
            case "inmemory":
            default:
                return new PersonDataProviderInMemory();
        }
    }
}