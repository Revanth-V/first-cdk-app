package com.revanth.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class ClientTherapistMapping {
    private String clientId;
    private String therapistId;
    private String journalAccess;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("clientId")
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("therapistId")
    public String getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(String therapistId) {
        this.therapistId = therapistId;
    }

    @DynamoDbAttribute("journalAccess")
    public String getJournalAccess() {
        return journalAccess;
    }

    public void setJournalAccess(String journalAccess) {
        this.journalAccess = journalAccess;
    }
}
