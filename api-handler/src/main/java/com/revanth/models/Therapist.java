package com.revanth.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;

@DynamoDbBean
public class Therapist {
    private String therapistId;
    private String name;
    private String email;

    private String password;
    private List<String> slotsAvailable;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("therapistId")
    public String getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(String therapistId) {
        this.therapistId = therapistId;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDbAttribute("password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDbAttribute("slotsAvailable")
    public List<String> getSlotsAvailable() {
        return slotsAvailable;
    }

    public void setSlotsAvailable(List<String> slotsAvailable) {
        this.slotsAvailable = slotsAvailable;
    }
}
