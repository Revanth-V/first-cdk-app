package com.revanth.handlers.mappings;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revanth.models.ClientTherapistMapping;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.Map;

public class ClientTherapistMappingHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbTable<ClientTherapistMapping> mappingTable;

    public ClientTherapistMappingHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        mappingTable = enhancedClient.table(System.getenv("MAPPINGS_TABLE_NAME"), TableSchema.fromBean(ClientTherapistMapping.class));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String httpMethod = request.getHttpMethod();
        String responseMessage;

        try {
            if ("POST".equals(httpMethod) && request.getPath().equals("/mappings")) {
                responseMessage = createMapping(request.getBody());
            } else if ("POST".equals(httpMethod) && request.getPath().equals("/mappings/journal-access")) {
                responseMessage = updateJournalAccess(request.getQueryStringParameters());
            } else if ("POST".equals(httpMethod) && request.getPath().equals("/mappings/revoke-journal-access")) {
                responseMessage = revokeJournalAccess(request.getQueryStringParameters());
            } else if ("DELETE".equals(httpMethod)) {
                responseMessage = deleteMapping(request.getQueryStringParameters());
            } else {
                return createResponse(400, "Unsupported HTTP method.");
            }

            return createResponse(200, responseMessage);
        } catch (Exception e) {
            return createResponse(500, "Error: " + e.getMessage());
        }
    }

    private String createMapping(String requestBody) {
        ClientTherapistMapping mapping = parseRequestBody(requestBody);

        // Set default value for journalAccess
        mapping.setJournalAccess("No");

        // Save the mapping to DynamoDB
        mappingTable.putItem(mapping);

        return "Mapping created successfully.";
    }

    private String updateJournalAccess(Map<String, String> queryParams) {
        if (queryParams == null || !queryParams.containsKey("clientId") || !queryParams.containsKey("therapistId")) {
            throw new IllegalArgumentException("clientId and therapistId are required query parameters.");
        }

        String clientId = queryParams.get("clientId");
        String therapistId = queryParams.get("therapistId");

        ClientTherapistMapping key = new ClientTherapistMapping();
        key.setClientId(clientId);
        key.setTherapistId(therapistId);

        ClientTherapistMapping existingMapping = mappingTable.getItem(key);
        if (existingMapping == null) {
            throw new IllegalArgumentException("Mapping does not exist.");
        }

        // Update journalAccess to "Yes"
        existingMapping.setJournalAccess("Yes");
        mappingTable.updateItem(existingMapping);

        return "Journal access updated successfully.";
    }

    private String revokeJournalAccess(Map<String, String> queryParams) {
        if (queryParams == null || !queryParams.containsKey("clientId") || !queryParams.containsKey("therapistId")) {
            throw new IllegalArgumentException("clientId and therapistId are required query parameters.");
        }

        String clientId = queryParams.get("clientId");
        String therapistId = queryParams.get("therapistId");

        ClientTherapistMapping key = new ClientTherapistMapping();
        key.setClientId(clientId);
        key.setTherapistId(therapistId);

        ClientTherapistMapping existingMapping = mappingTable.getItem(key);
        if (existingMapping == null) {
            throw new IllegalArgumentException("Mapping does not exist.");
        }

        if (!"Yes".equals(existingMapping.getJournalAccess())) {
            throw new IllegalArgumentException("Journal access is already revoked.");
        }

        // Update journalAccess to "No"
        existingMapping.setJournalAccess("No");
        mappingTable.updateItem(existingMapping);

        return "Journal access revoked successfully.";
    }

    private String deleteMapping(Map<String, String> queryParams) {
        if (queryParams == null || !queryParams.containsKey("clientId") || !queryParams.containsKey("therapistId")) {
            throw new IllegalArgumentException("clientId and therapistId are required query parameters.");
        }

        String clientId = queryParams.get("clientId");
        String therapistId = queryParams.get("therapistId");

        ClientTherapistMapping key = new ClientTherapistMapping();
        key.setClientId(clientId);
        key.setTherapistId(therapistId);

        // Delete the mapping from DynamoDB
        mappingTable.deleteItem(key);

        return "Mapping deleted successfully.";
    }

    private ClientTherapistMapping parseRequestBody(String requestBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(requestBody, ClientTherapistMapping.class);
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
        response.setBody("{\"message\": \"" + message + "\"}");
        return response;
    }
}
