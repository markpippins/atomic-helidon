package com.angrysurfer.atomic.user.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MongoConfig {

    @ConfigProperty(name = "mongodb.connection.string", defaultValue = "mongodb://localhost:27017")
    String connectionString;

    @ConfigProperty(name = "mongodb.database.name", defaultValue = "atomic")
    String databaseName;

    @Produces
    @ApplicationScoped
    public MongoClient mongoClient() {
        return MongoClients.create(connectionString);
    }
}