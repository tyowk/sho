package sho.structs;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

public class Config {
    public String token;
    public String prefix;
    public String database;
    public String[] tables;

    public static Config load() {
        Yaml yaml = new Yaml();
        Config fileConfig = new Config();

        try {
            InputStream input = getConfigInputStream();
            if (input != null) {
                fileConfig = yaml.loadAs(input, Config.class);
            }
        } catch (Exception ignored) {
        }

        Config config = new Config();
        config.token = getEnvOr("TOKEN", fileConfig.token);
        config.prefix = Optional.ofNullable(getEnvOr("PREFIX", fileConfig.prefix)).orElse("sho");
        config.database =
                Optional.ofNullable(getEnvOr("DATABASE", fileConfig.database))
                        .orElse("database.sqlite");
        config.tables =
                Optional.ofNullable(fileConfig.tables)
                        .orElse(new String[] {"guilds", "economy", "cooldowns"});

        return config;
    }

    private static InputStream getConfigInputStream() {
        try {
            return new FileInputStream("sho.yml");
        } catch (Exception e) {
            return Config.class.getClassLoader().getResourceAsStream("sho.yml");
        }
    }

    private static String getEnvOr(String key, String fallback) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : fallback;
    }

    public String getToken() {
        return token;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDatabasePath() {
        return database;
    }

    public String[] getTables() {
        return tables;
    }
}
