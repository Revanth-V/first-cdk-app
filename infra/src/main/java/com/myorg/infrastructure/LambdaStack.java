package com.myorg.infrastructure;

import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Code;

import java.util.Map;

public class LambdaStack extends Stack {

    public LambdaStack(final Construct scope, final String id, StackProps build) {
        super(scope, id);

        createClientsLambda();
        createTherapistsLambda();
        createMessagesLambda();
        createJournalsLambda();
        createSessionsLambda();
        createClientTherapistMappingsLambda();
        createAppointmentsLambda();
    }

    private void createClientsLambda() {
        ITable clientsTable = Table.fromTableName(this, "ClientsTable", "ClientsTable");

        Function clientsLambda = Function.Builder.create(this, "ClientsLambda")
                .runtime(Runtime.JAVA_17)
                .handler("com.revanth.handlers.clients.ClientHandler::handleRequest")
                .code(Code.fromAsset("../assets/api-handler.jar"))
                .functionName("ClientsFunction")
                .environment(EnvironmentConfig.CLIENTS_ENV)
                .build();

        clientsTable.grantReadWriteData(clientsLambda);
    }

    private void createTherapistsLambda() {
        ITable therapistsTable = Table.fromTableName(this, "TherapistsTable", "TherapistsTable");

        Function therapistsLambda = Function.Builder.create(this, "TherapistsLambda")
                .runtime(Runtime.JAVA_17)
                .handler("com.revanth.handlers.therapists.TherapistHandler::handleRequest")
                .code(Code.fromAsset("../assets/api-handler.jar"))
                .functionName("TherapistsFunction")
                .environment(EnvironmentConfig.THERAPISTS_ENV)
                .build();

        therapistsTable.grantReadWriteData(therapistsLambda);
    }

    private void createMessagesLambda() {
        ITable messagesTable = Table.fromTableName(this, "MessagesTable", "MessagesTable");

        Function messagesLambda = Function.Builder.create(this, "MessagesLambda")
                .runtime(Runtime.JAVA_17)
                .handler("com.revanth.handlers.messages.MessageHandler::handleRequest")
                .code(Code.fromAsset("../assets/api-handler.jar"))
                .functionName("MessagesFunction")
                .environment(EnvironmentConfig.MESSAGES_ENV)
                .build();

        messagesTable.grantReadWriteData(messagesLambda);
    }

    private void createJournalsLambda() {
        ITable journalsTable = Table.fromTableName(this, "JournalsTable", "JournalsTable");

        Function journalsLambda = Function.Builder.create(this, "JournalsLambda")
                .runtime(Runtime.JAVA_17)
                .handler("com.revanth.handlers.journals.JournalHandler::handleRequest")
                .code(Code.fromAsset("../assets/api-handler.jar"))
                .functionName("JournalsFunction")
                .environment(EnvironmentConfig.JOURNALS_ENV)
                .build();

        journalsTable.grantReadWriteData(journalsLambda);
    }

    private void createSessionsLambda() {
        ITable sessionsTable = Table.fromTableName(this, "SessionsTable", "SessionsTable");

        Function sessionsLambda = Function.Builder.create(this, "SessionsLambda")
                .runtime(Runtime.JAVA_17)
                .handler("com.revanth.handlers.sessions.SessionHandler::handleRequest")
                .code(Code.fromAsset("../assets/api-handler.jar"))
                .functionName("SessionsFunction")
                .environment(EnvironmentConfig.SESSIONS_ENV)
                .build();

        sessionsTable.grantReadWriteData(sessionsLambda);
    }

    private void createClientTherapistMappingsLambda() {
        ITable mappingsTable = Table.fromTableName(this, "ClientTherapistMappingsTable", "ClientTherapistMappingsTable");

        Function mappingsLambda = Function.Builder.create(this, "ClientTherapistMappingsLambda")
                .runtime(Runtime.JAVA_17)
                .handler("com.revanth.handlers.mappings.ClientTherapistMappingHandler::handleRequest")
                .code(Code.fromAsset("../assets/api-handler.jar"))
                .functionName("ClientTherapistMappingsFunction")
                .environment(EnvironmentConfig.MAPPINGS_ENV)
                .build();

        mappingsTable.grantReadWriteData(mappingsLambda);
    }

    private void createAppointmentsLambda() {
        ITable appointmentsTable = Table.fromTableName(this, "AppointmentsTable", "AppointmentsTable");

        Function appointmentsLambda = Function.Builder.create(this, "AppointmentsLambda")
                .runtime(Runtime.JAVA_17)
                .handler("com.revanth.handlers.appointments.AppointmentHandler::handleRequest")
                .code(Code.fromAsset("../assets/api-handler.jar"))
                .functionName("AppointmentsFunction")
                .environment(EnvironmentConfig.APPOINTMENTS_ENV)
                .build();

        appointmentsTable.grantReadWriteData(appointmentsLambda);
    }
}
