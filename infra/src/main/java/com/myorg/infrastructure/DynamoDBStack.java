package com.myorg.infrastructure;

import software.amazon.awscdk.StackProps;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps;

public class DynamoDBStack extends Stack {

    public DynamoDBStack(final Construct scope, final String id, StackProps build) {
        super(scope, id);

        createClientsTable();
        createTherapistsTable();
        createMessagesTable();
        createJournalsTable();
        createSessionsTable();
        createClientTherapistMappingsTable();
        createAppointmentsTable();
    }

    private void createClientsTable() {
        Table.Builder.create(this, "ClientsTable")
                .partitionKey(Attribute.builder()
                        .name("ClientId")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
    }

    private void createTherapistsTable() {
        Table.Builder.create(this, "TherapistsTable")
                .partitionKey(Attribute.builder()
                        .name("TherapistId")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
    }

    private void createMessagesTable() {
        Table messagesTable = Table.Builder.create(this, "MessagesTable")
                .partitionKey(Attribute.builder()
                        .name("ConversationId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("Timestamp")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        // Add GSI for ClientId and Timestamp
        messagesTable.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
                .indexName("ClientMessagesIndex")
                .partitionKey(Attribute.builder()
                        .name("ClientId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("Timestamp")
                        .type(AttributeType.STRING)
                        .build())
                .build());

        // Add GSI for TherapistId and Timestamp
        messagesTable.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
                .indexName("TherapistMessagesIndex")
                .partitionKey(Attribute.builder()
                        .name("TherapistId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("Timestamp")
                        .type(AttributeType.STRING)
                        .build())
                .build());
    }

    private void createJournalsTable() {
        Table.Builder.create(this, "JournalsTable")
                .partitionKey(Attribute.builder()
                        .name("ClientId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("Timestamp")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
    }

    private void createSessionsTable() {
        Table sessionsTable = Table.Builder.create(this, "SessionsTable")
                .partitionKey(Attribute.builder()
                        .name("SessionId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("SessionDate")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        // Add GSI for ClientId and TherapistId
        sessionsTable.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
                .indexName("ClientSessionsIndex")
                .partitionKey(Attribute.builder()
                        .name("ClientId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("TherapistId")
                        .type(AttributeType.STRING)
                        .build())
                .build());
    }

    private void createClientTherapistMappingsTable() {
        Table.Builder.create(this, "ClientTherapistMappingsTable")
                .partitionKey(Attribute.builder()
                        .name("ClientId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("TherapistId")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
    }

    private void createAppointmentsTable() {
        Table.Builder.create(this, "AppointmentsTable")
                .partitionKey(Attribute.builder()
                        .name("TherapistId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("Date#TimeSlot")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
    }
}
