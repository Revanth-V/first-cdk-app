package com.revanth.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Session {
    private String sessionId;
    private String sessionDate;
    private String therapistId;
    private String clientId;
    private String sharedNotes;
    private String privateNotes;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("sessionId")
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("sessionDate")
    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    @DynamoDbAttribute("therapistId")
    public String getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(String therapistId) {
        this.therapistId = therapistId;
    }

    @DynamoDbAttribute("clientId")
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @DynamoDbAttribute("sharedNotes")
    public String getSharedNotes() {
        return sharedNotes;
    }

    public void setSharedNotes(String sharedNotes) {
        this.sharedNotes = sharedNotes;
    }

    @DynamoDbAttribute("privateNotes")
    public String getPrivateNotes() {
        return privateNotes;
    }

    public void setPrivateNotes(String privateNotes) {
        this.privateNotes = privateNotes;
    }
}
