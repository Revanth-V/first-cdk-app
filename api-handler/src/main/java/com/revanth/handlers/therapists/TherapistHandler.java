package com.revanth.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.revanth.models.Therapist;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class TherapistHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER = Logger.getLogger(TherapistHandler.class.getName());
    private final DynamoDbTable<Therapist> therapistTable;
    private final ObjectMapper objectMapper;

    public TherapistHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        therapistTable = enhancedClient.table(System.getenv("THERAPISTS_TABLE_NAME"), TableSchema.fromBean(Therapist.class));
        objectMapper = new ObjectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");

        try {
            switch (request.getHttpMethod()) {
                case "GET":
                    return handleGetRequest(request.getPath());
                case "POST":
                    return handlePostRequest(request);
                case "PUT":
                    return handlePutRequest(request);
                case "DELETE":
                    return handleDeleteRequest(request.getPath());
                default:
                    return createErrorResponse(405, "Method Not Allowed", headers);
            }
        } catch (Exception e) {
            LOGGER.severe("Error processing request: " + e.getMessage());
            return createErrorResponse(500, "Internal Server Error: " + e.getMessage(), headers);
        }
    }

    private APIGatewayProxyResponseEvent handleGetRequest(String path) {
        if (path.equals("/therapists")) {
            return getAllTherapists();
        } else if (path.startsWith("/therapists/")) {
            String therapistId = path.split("/")[2];
            return getTherapist(therapistId);
        }
        return createErrorResponse(404, "Not Found", null);
    }

    private APIGatewayProxyResponseEvent handlePostRequest(APIGatewayProxyRequestEvent request) {
        if (request.getPath().equals("/therapists")) {
            return createTherapist(request);
        } else if (request.getPath().equals("/therapists/login")) {
            return loginTherapist(request);
        }
        return createErrorResponse(404, "Not Found", null);
    }

    private APIGatewayProxyResponseEvent handlePutRequest(APIGatewayProxyRequestEvent request) {
        if (request.getPath().startsWith("/therapists/")) {
            String therapistId = request.getPath().split("/")[2];
            return updateTherapist(therapistId, request);
        }
        return createErrorResponse(404, "Not Found", null);
    }

    private APIGatewayProxyResponseEvent handleDeleteRequest(String path) {
        if (path.startsWith("/therapists/")) {
            String therapistId = path.split("/")[2];
            return deleteTherapist(therapistId);
        }
        return createErrorResponse(404, "Not Found", null);
    }

    private APIGatewayProxyResponseEvent getAllTherapists() {
        try {
            return createSuccessResponse(therapistTable.scan().items());
        } catch (Exception e) {
            LOGGER.severe("Error fetching therapists: " + e.getMessage());
            return createErrorResponse(500, "Error fetching therapists", null);
        }
    }

    private APIGatewayProxyResponseEvent createTherapist(APIGatewayProxyRequestEvent request) {
        try {
            Therapist therapist = parseBody(request, Therapist.class);

            if (therapist.getEmail() == null || therapist.getPassword() == null) {
                return createErrorResponse(400, "Email and password are required", null);
            }

            therapist.setTherapistId(UUID.randomUUID().toString());
            therapist.setPassword(BCrypt.hashpw(therapist.getPassword(), BCrypt.gensalt()));

            therapistTable.putItem(therapist);
            return createSuccessResponse(therapist);
        } catch (Exception e) {
            LOGGER.severe("Error creating therapist: " + e.getMessage());
            return createErrorResponse(500, "Error creating therapist", null);
        }
    }

    private APIGatewayProxyResponseEvent getTherapist(String therapistId) {
        try {
            Therapist keyTherapist = new Therapist();
            keyTherapist.setTherapistId(therapistId);
            Therapist therapist = therapistTable.getItem(keyTherapist);

            return therapist != null
                    ? createSuccessResponse(therapist)
                    : createErrorResponse(404, "Therapist not found", null);
        } catch (Exception e) {
            LOGGER.severe("Error fetching therapist: " + e.getMessage());
            return createErrorResponse(500, "Error fetching therapist", null);
        }
    }

    private APIGatewayProxyResponseEvent updateTherapist(String therapistId, APIGatewayProxyRequestEvent request) {
        try {
            Therapist keyTherapist = new Therapist();
            keyTherapist.setTherapistId(therapistId);
            Therapist existingTherapist = therapistTable.getItem(keyTherapist);

            if (existingTherapist == null) {
                return createErrorResponse(404, "Therapist not found", null);
            }

            Therapist updatedTherapist = parseBody(request, Therapist.class);
            updatedTherapist.setTherapistId(therapistId);

            if (updatedTherapist.getName() != null) existingTherapist.setName(updatedTherapist.getName());
            if (updatedTherapist.getEmail() != null) existingTherapist.setEmail(updatedTherapist.getEmail());
            if (updatedTherapist.getPassword() != null) {
                existingTherapist.setPassword(BCrypt.hashpw(updatedTherapist.getPassword(), BCrypt.gensalt()));
            }
            if (updatedTherapist.getSlotsAvailable() != null) {
                existingTherapist.setSlotsAvailable(updatedTherapist.getSlotsAvailable());
            }

            therapistTable.putItem(existingTherapist);
            return createSuccessResponse(existingTherapist);
        } catch (Exception e) {
            LOGGER.severe("Error updating therapist: " + e.getMessage());
            return createErrorResponse(500, "Error updating therapist", null);
        }
    }

    private APIGatewayProxyResponseEvent deleteTherapist(String therapistId) {
        try {
            Therapist keyTherapist = new Therapist();
            keyTherapist.setTherapistId(therapistId);
            therapistTable.deleteItem(keyTherapist);
            return createSuccessResponse("Therapist deleted successfully");
        } catch (Exception e) {
            LOGGER.severe("Error deleting therapist: " + e.getMessage());
            return createErrorResponse(500, "Error deleting therapist", null);
        }
    }

    private APIGatewayProxyResponseEvent loginTherapist(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> credentials = parseBody(request, HashMap.class);
            String email = credentials.get("email");
            String password = credentials.get("password");

            for (Therapist therapist : therapistTable.scan().items()) {
                if (therapist.getEmail().equals(email) &&
                        BCrypt.checkpw(password, therapist.getPassword())) {
                    return createSuccessResponse("Login successful");
                }
            }
            return createErrorResponse(401, "Invalid email or password", null);
        } catch (Exception e) {
            LOGGER.severe("Login error: " + e.getMessage());
            return createErrorResponse(500, "Login error", null);
        }
    }

    private <T> T parseBody(APIGatewayProxyRequestEvent request, Class<T> clazz) {
        try {
            return objectMapper.readValue(request.getBody(), clazz);
        } catch (Exception e) {
            LOGGER.severe("Error parsing request body: " + e.getMessage());
            throw new RuntimeException("Invalid request body", e);
        }
    }

    private APIGatewayProxyResponseEvent createSuccessResponse(Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            LOGGER.severe("Error creating success response: " + e.getMessage());
            return createErrorResponse(500, "Error creating response", null);
        }
    }

    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message, Map<String, String> headers) {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("Access-Control-Allow-Origin", "*");

        if (headers != null) {
            defaultHeaders.putAll(headers);
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(defaultHeaders)
                .withBody(String.format("{\"message\": \"%s\"}", message));
    }
}