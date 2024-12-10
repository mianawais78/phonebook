package org.vaadin.example.utility;

import java.util.Map;

public class AppConfig {
    private String dbType;
    private Map<String, String> dbCredentials;

    // Getters and setters
    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public Map<String, String> getDbCredentials() {
        return dbCredentials;
    }

    public void setDbCredentials(Map<String, String> dbCredentials) {
        this.dbCredentials = dbCredentials;
    }
}