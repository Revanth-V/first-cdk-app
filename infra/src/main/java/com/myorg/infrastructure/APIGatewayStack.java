package com.myorg.infrastructure;

import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class APIGatewayStack extends Stack {

    public APIGatewayStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create the REST API
        RestApi api = RestApi.Builder.create(this, "ApiHandlerRestApi")
                .restApiName("ApiHandlerService")
                .description("API Gateway for the api-handler module.")
                .build();

        // Lambda Functions
        Function clientHandler = createLambda("ClientHandler", "com.revanth.handlers.clients.ClientHandler::handleRequest");
        Function therapistHandler = createLambda("TherapistHandler", "com.revanth.handlers.therapists.TherapistHandler::handleRequest");
        Function messageHandler = createLambda("MessageHandler", "com.revanth.handlers.messages.MessageHandler::handleRequest");
        Function appointmentHandler = createLambda("AppointmentHandler", "com.revanth.handlers.appointments.AppointmentHandler::handleRequest");
        Function sessionHandler = createLambda("SessionHandler", "com.revanth.handlers.sessions.SessionHandler::handleRequest");
        Function mappingHandler = createLambda("MappingHandler", "com.revanth.handlers.mappings.ClientTherapistMappingHandler::handleRequest");
        Function journalHandler = createLambda("JournalHandler", "com.revanth.handlers.journals.JournalHandler::handleRequest");

        // /clients resource
        Resource clientsResource = api.getRoot().addResource("clients");
        clientsResource.addMethod("GET", LambdaIntegration.Builder.create(clientHandler).build());
        clientsResource.addMethod("POST", LambdaIntegration.Builder.create(clientHandler).build());
        clientsResource.addResource("{clientId}")
                .addMethod("GET", LambdaIntegration.Builder.create(clientHandler).build());
        clientsResource.addResource("login")
                .addMethod("POST", LambdaIntegration.Builder.create(clientHandler).build());

        // /therapists resource
        Resource therapistsResource = api.getRoot().addResource("therapists");
        therapistsResource.addMethod("GET", LambdaIntegration.Builder.create(therapistHandler).build());
        therapistsResource.addMethod("POST", LambdaIntegration.Builder.create(therapistHandler).build());
        therapistsResource.addResource("{therapistId}")
                .addMethod("GET", LambdaIntegration.Builder.create(therapistHandler).build());
        therapistsResource.addResource("login")
                .addMethod("POST", LambdaIntegration.Builder.create(therapistHandler).build());

        // /messages resource
        Resource messagesResource = api.getRoot().addResource("messages");
        messagesResource.addResource("send").addMethod("POST", LambdaIntegration.Builder.create(messageHandler).build());
        messagesResource.addResource("history").addMethod("GET", LambdaIntegration.Builder.create(messageHandler).build());

        // /appointments resource
        Resource appointmentsResource = api.getRoot().addResource("appointment");
        appointmentsResource.addResource("request").addMethod("POST", LambdaIntegration.Builder.create(appointmentHandler).build());

        // /sessions resource
        Resource sessionsResource = api.getRoot().addResource("session");
        sessionsResource.addMethod("GET", LambdaIntegration.Builder.create(sessionHandler).build());
        sessionsResource.addMethod("POST", LambdaIntegration.Builder.create(sessionHandler).build());
        sessionsResource.addMethod("PATCH", LambdaIntegration.Builder.create(sessionHandler).build());

        // /mappings resource
        Resource mappingsResource = api.getRoot().addResource("mappings");
        mappingsResource.addMethod("POST", LambdaIntegration.Builder.create(mappingHandler).build());
        mappingsResource.addMethod("DELETE", LambdaIntegration.Builder.create(mappingHandler).build());

        // /journals resource
        Resource journalsResource = api.getRoot().addResource("journal");
        journalsResource.addMethod("GET", LambdaIntegration.Builder.create(journalHandler).build());
        journalsResource.addMethod("POST", LambdaIntegration.Builder.create(journalHandler).build());
    }

    private Function createLambda(String name, String handler) {
        return Function.Builder.create(this, name + "Lambda")
                .runtime(Runtime.JAVA_17)
                .code(Code.fromAsset("../assets/api-handler.jar")) // Adjust the jar path if needed
                .handler(handler)
                .build();
    }
}
