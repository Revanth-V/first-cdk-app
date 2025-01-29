package com.revanth.handlers.journals;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revanth.models.ClientTherapistMapping;
import com.revanth.models.Journal;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JournalHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbTable<Journal> journalTable;
    private final DynamoDbTable<ClientTherapistMapping> mappingTable;

    public JournalHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        journalTable = enhancedClient.table(System.getenv("JOURNALS_TABLE_NAME"), TableSchema.fromBean(Journal.class));
        mappingTable = enhancedClient.table(System.getenv("MAPPINGS_TABLE_NAME"), TableSchema.fromBean(ClientTherapistMapping.class));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String httpMethod = request.getHttpMethod();
        String responseMessage;

        try {
            if ("GET".equals(httpMethod) && request.getPath().equals("/journal")) {
                Map<String, String> queryParams = request.getQueryStringParameters();
                if (queryParams.containsKey("therapistId")) {
                    responseMessage = getJournalEntriesForTherapist(queryParams);
                } else {
                    responseMessage = getJournalEntries(queryParams);
                }
            } else if ("POST".equals(httpMethod) && request.getPath().equals("/journal")) {
                responseMessage = addJournalEntry(request.getBody());
            } else {
                return createResponse(400, "Unsupported HTTP method.");
            }

            return createResponse(200, responseMessage);
        } catch (Exception e) {
            return createResponse(500, "Error: " + e.getMessage());
        }
    }

    private String getJournalEntries(Map<String, String> queryParams) {
        String clientId = queryParams.get("clientId");
        String feeling = queryParams.get("feeling");
        String intensity = queryParams.get("intensity");

        List<Journal> journals = journalTable.scan().items().stream()
                .filter(journal -> clientId.equals(journal.getClientId()) &&
                        (feeling == null || feeling.equals(journal.getFeeling())) &&
                        (intensity == null || intensity.equals(journal.getIntensity())))
                .collect(Collectors.toList());

        return toJson(journals);
    }

    private String getJournalEntriesForTherapist(Map<String, String> queryParams) {
        String clientId = queryParams.get("clientId");
        String therapistId = queryParams.get("therapistId");

        ClientTherapistMapping key = new ClientTherapistMapping();
        key.setClientId(clientId);
        key.setTherapistId(therapistId);

        ClientTherapistMapping mapping = mappingTable.getItem(key);


        if (mapping == null || !"Yes".equals(mapping.getJournalAccess())) {
            throw new IllegalArgumentException("Therapist does not have access to the journal.");
        }

        List<Journal> journals = journalTable.scan().items().stream()
                .filter(journal -> clientId.equals(journal.getClientId()))
                .collect(Collectors.toList());

        return toJson(journals);
    }

    private String addJournalEntry(String requestBody) {
        Journal journal = parseRequestBody(requestBody);
        journalTable.putItem(journal);
        return "Journal entry added successfully.";
    }

    private String toJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON: " + e.getMessage());
        }
    }

    private Journal parseRequestBody(String requestBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(requestBody, Journal.class);
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
