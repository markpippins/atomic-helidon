package com.angrysurfer.atomic.user.repository;

import com.angrysurfer.atomic.user.model.UserRegistration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Optional;

@ApplicationScoped
public class UserRegistrationRepository {

    @Inject
    private MongoClient mongoClient;

    private MongoCollection<Document> getCollection() {
        MongoDatabase database = mongoClient.getDatabase("atomic");
        return database.getCollection("users", Document.class);
    }

    public Optional<UserRegistration> findByAlias(String alias) {
        Document doc = getCollection().find(new Document("alias", alias)).first();
        if (doc != null) {
            return Optional.of(documentToUserRegistration(doc));
        }
        return Optional.empty();
    }

    public Optional<UserRegistration> findByEmail(String email) {
        Document doc = getCollection().find(new Document("email", email)).first();
        if (doc != null) {
            return Optional.of(documentToUserRegistration(doc));
        }
        return Optional.empty();
    }

    public UserRegistration save(UserRegistration userRegistration) {
        MongoCollection<Document> collection = getCollection();
        Document doc = userRegistrationToDocument(userRegistration);
        
        if (userRegistration.getMongoId() != null) {
            // Update existing
            collection.replaceOne(new Document("_id", new ObjectId(userRegistration.getMongoId())), doc);
        } else {
            // Insert new
            collection.insertOne(doc);
            userRegistration.setMongoId(doc.getObjectId("_id").toHexString());
        }
        return userRegistration;
    }

    private UserRegistration documentToUserRegistration(Document doc) {
        UserRegistration user = new UserRegistration();
        user.setMongoId(doc.getObjectId("_id").toHexString());
        if (doc.containsKey("id")) {
            user.setId(doc.getLong("id"));
        }
        if (doc.containsKey("identifier")) {
            user.setIdentifier(doc.getString("identifier"));
        }
        if (doc.containsKey("admin")) {
            user.setAdmin(doc.getBoolean("admin"));
        }
        if (doc.containsKey("alias")) {
            user.setAlias(doc.getString("alias"));
        }
        if (doc.containsKey("email")) {
            user.setEmail(doc.getString("email"));
        }
        if (doc.containsKey("avatarUrl")) {
            user.setAvatarUrl(doc.getString("avatarUrl"));
        }
        return user;
    }

    private Document userRegistrationToDocument(UserRegistration user) {
        Document doc = new Document();
        if (user.getMongoId() != null) {
            doc.put("_id", new ObjectId(user.getMongoId()));
        }
        if (user.getId() != null) {
            doc.put("id", user.getId());
        }
        doc.put("identifier", user.getIdentifier());
        doc.put("admin", user.isAdmin());
        doc.put("alias", user.getAlias());
        doc.put("email", user.getEmail());
        doc.put("avatarUrl", user.getAvatarUrl());
        return doc;
    }
}