package org.dgawlik;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.List;

@EnableMongoRepositories
@SpringBootApplication
public class App
        extends AbstractMongoClientConfiguration {

    @Value("${mongo.server.host:localhost}")
    private String host;

    @Value("${mongo.server.port:27017}")
    private Integer port;

    public static void main(String[] args) {

        SpringApplication.run(App.class, args);
    }

    @Override
    protected String getDatabaseName() {

        return "test";
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {

        builder
                .credential(MongoCredential.createCredential("admin",
                        "admin", "pass".toCharArray()))
                .applyToClusterSettings(settings -> {
                    settings.hosts(List.of(new ServerAddress(host, port)));
                });
    }
}
