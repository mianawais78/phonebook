package org.vaadin.example.utility;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;

public class ConfigLoader {
    public static AppConfig loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("application.yaml")) {
            return yaml.loadAs(inputStream, AppConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}