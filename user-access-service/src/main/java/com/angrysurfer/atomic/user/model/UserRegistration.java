package com.angrysurfer.atomic.user.model;

import java.io.Serializable;

import com.angrysurfer.atomic.user.UserRegistrationDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRegistration implements Serializable {

    private static final long serialVersionUID = 2747813660378401172L;

    private String mongoId;

    // Client-facing ID - for compatibility with existing web clients
    private Long id;

    private String identifier;

    private boolean admin = false;

    @NotBlank(message = "Alias is required")
    private String alias;

    @Email(message = "Email should be valid")
    private String email;

    private String avatarUrl = "https://picsum.photos/50/50";

    public UserRegistrationDTO toDTO() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setId(String.valueOf(getId())); // Convert Long to String for DTO
        dto.setAlias(getAlias());
        dto.setEmail(getEmail());
        dto.setIdentifier(getIdentifier());
        dto.setAdmin(isAdmin());
        dto.setAvatarUrl(getAvatarUrl());
        return dto;
    }

    public UserRegistration() {

    }

    public UserRegistration(String alias, String email, String avatarUrl) {
        setAlias(alias);
        setEmail(email);
        setAvatarUrl(avatarUrl);
    }

    public UserRegistration(String alias, String email, String avatarUrl, String identifier) {
        setAlias(alias);
        setEmail(email);
        setAvatarUrl(avatarUrl);
        setIdentifier(identifier);
    }

    public String getMongoId() {
        return mongoId;
    }

    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

}