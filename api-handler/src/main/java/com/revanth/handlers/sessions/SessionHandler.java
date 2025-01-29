package com.revanth.handlers.sessions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revanth.models.Session;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SessionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbTable<Session> sessionTable;

    public SessionHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        // Ensure Global Secondary Index exists
        CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName("Session")
                .keySchema(KeySchemaElement.builder().attributeName("sessionId").keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName("sessionDate").keyType(KeyType.RANGE).build())
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName("ClientId-TherapistId-Index")
                                .keySchema(KeySchemaElement.builder().attributeName("clientId").keyType(KeyType.HASH).build(),
                                        KeySchemaElement.builder().attributeName("therapistId").keyType(KeyType.RANGE).build())
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build()
                )
                .build();

        sessionTable = enhancedClient.table(System.getenv("SESSIONS_TABLE_NAME"), TableSchema.fromBean(Session.class));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String httpMethod = request.getHttpMethod();
        String responseMessage;

        try {
            if ("GET".equals(httpMethod)) {
                responseMessage = getAllSessions();
            } else if ("POST".equals(httpMethod)) {
                responseMessage = addSession(request.getBody());
            } else if ("PATCH".equals(httpMethod)) {
                responseMessage = updateSession(request.getBody());
            } else {
                return createResponse(400, "Unsupported HTTP method.");
            }

            return createResponse(200, responseMessage);
        } catch (Exception e) {
            return createResponse(500, "Error: " + e.getMessage());
        }
    }

    private String getAllSessions() {
        List<Session> sessions = sessionTable.scan().items().stream().collect(Collectors.toList());
        return toJson(sessions);
    }

    private String addSession(String requestBody) {
        Session session = parseRequestBody(requestBody);

        // Generate unique sessionId
        session.setSessionId(UUID.randomUUID().toString());

        // Set default values for sharedNotes and privateNotes
        session.setSharedNotes("");
        session.setPrivateNotes("");

        // Save the session to DynamoDB
        sessionTable.putItem(session);

        return "Session added successfully.";
    }

    private String updateSession(String requestBody) {
        Session session = parseRequestBody(requestBody);

        if (session.getSessionId() == null || session.getSessionDate() == null ||
                session.getTherapistId() == null || session.getClientId() == null) {
            throw new IllegalArgumentException("sessionId, sessionDate, therapistId, and clientId are required.");
        }

        // Update session in DynamoDB
        sessionTable.updateItem(session);

        return "Session updated successfully.";
    }

    private String toJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON: " + e.getMessage());
        }
    }

    private Session parseRequestBody(String requestBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(requestBody, Session.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid request body: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String message) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        response.setBody(message);
        return response;
    }
}
