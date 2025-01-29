package com.revanth.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class Appointment {
    private String appointmentId;
    private String timestamp;
    private String clientId;
    private String therapistId;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("appointmentId")
    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("timestamp")
    public String getDateTimeSlot() {
        return timestamp;
    }

    public void setDateTimeSlot(String dateTimeSlot) {
        this.timestamp = timestamp;
    }

    @DynamoDbAttribute("clientId")
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @DynamoDbAttribute("therapistId")
    public String getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(String therapistId) {
        this.therapistId = therapistId;
    }
}
