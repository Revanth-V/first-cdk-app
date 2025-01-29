package com.revanth.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.revanth.models.Client;
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

public class ClientHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private final DynamoDbTable<Client> clientTable;
    private final ObjectMapper objectMapper;

    public ClientHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        clientTable = enhancedClient.table(System.getenv("CLIENTS_TABLE_NAME"), TableSchema.fromBean(Client.class));
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
        if (path.equals("/clients")) {
            return getAllClients();
        } else if (path.startsWith("/clients/")) {
            String clientId = path.split("/")[2];
            return getClient(clientId);
        }
        return createErrorResponse(404, "Not Found", null);
    }

    private APIGatewayProxyResponseEvent handlePostRequest(APIGatewayProxyRequestEvent request) {
        if (request.getPath().equals("/clients")) {
            return createClient(request);
        } else if (request.getPath().equals("/clients/login")) {
            return loginClient(request);
        }
        return createErrorResponse(404, "Not Found", null);
    }

    private APIGatewayProxyResponseEvent handlePutRequest(APIGatewayProxyRequestEvent request) {
        if (request.getPath().startsWith("/clients/")) {
            String clientId = request.getPath().split("/")[2];
            return updateClient(clientId, request);
        }
        return createErrorResponse(404, "Not Found", null);
    }

    private APIGatewayProxyResponseEvent handleDeleteRequest(String path) {
        if (path.startsWith("/clients/")) {
            String clientId = path.split("/")[2];
            return deleteClient(clientId);
        }
        return createErrorResponse(404, "Not Found", null);
    }

    private APIGatewayProxyResponseEvent getAllClients() {
        try {
            return createSuccessResponse(clientTable.scan().items());
        } catch (Exception e) {
            LOGGER.severe("Error fetching clients: " + e.getMessage());
            return createErrorResponse(500, "Error fetching clients", null);
        }
    }

    private APIGatewayProxyResponseEvent createClient(APIGatewayProxyRequestEvent request) {
        try {
            Client client = parseBody(request, Client.class);

            // Validate input
            if (client.getEmail() == null || client.getPassword() == null) {
                return createErrorResponse(400, "Email and password are required", null);
            }

            // Generate unique ID
            client.setClientId(UUID.randomUUID().toString());

            // Hash password
            client.setPassword(BCrypt.hashpw(client.getPassword(), BCrypt.gensalt()));

            clientTable.putItem(client);
            return createSuccessResponse(client);
        } catch (Exception e) {
            LOGGER.severe("Error creating client: " + e.getMessage());
            return createErrorResponse(500, "Error creating client", null);
        }
    }

    private APIGatewayProxyResponseEvent getClient(String clientId) {
        try {
            Client keyClient = new Client();
            keyClient.setClientId(clientId);
            Client client = clientTable.getItem(keyClient);
            return client != null
                    ? createSuccessResponse(client)
                    : createErrorResponse(404, "Client not found", null);
        } catch (Exception e) {
            LOGGER.severe("Error fetching client: " + e.getMessage());
            return createErrorResponse(500, "Error fetching client", null);
        }
    }

    private APIGatewayProxyResponseEvent updateClient(String clientId, APIGatewayProxyRequestEvent request) {
        try {
            Client keyClient = new Client();
            keyClient.setClientId(clientId);
            Client existingClient = clientTable.getItem(keyClient);
            if (existingClient == null) {
                return createErrorResponse(404, "Client not found", null);
            }

            Client updatedClient = parseBody(request, Client.class);
            updatedClient.setClientId(clientId);

            // Only update non-null fields
            if (updatedClient.getEmail() != null) existingClient.setEmail(updatedClient.getEmail());
            if (updatedClient.getPassword() != null) {
                existingClient.setPassword(BCrypt.hashpw(updatedClient.getPassword(), BCrypt.gensalt()));
            }
            // Add more fields as needed

            clientTable.putItem(existingClient);
            return createSuccessResponse(existingClient);
        } catch (Exception e) {
            LOGGER.severe("Error updating client: " + e.getMessage());
            return createErrorResponse(500, "Error updating client", null);
        }
    }

    private APIGatewayProxyResponseEvent deleteClient(String clientId) {
        try {
            Client keyClient = new Client();
            keyClient.setClientId(clientId);
            clientTable.deleteItem(keyClient);
            return createSuccessResponse("Client deleted successfully");
        } catch (Exception e) {
            LOGGER.severe("Error deleting client: " + e.getMessage());
            return createErrorResponse(500, "Error deleting client", null);
        }
    }

    private APIGatewayProxyResponseEvent loginClient(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> credentials = parseBody(request, HashMap.class);
            String email = credentials.get("email");
            String password = credentials.get("password");

            // Find client by email (more efficient than scanning)
            for (Client client : clientTable.scan().items()) {
                if (client.getEmail().equals(email) &&
                        BCrypt.checkpw(password, client.getPassword())) {
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