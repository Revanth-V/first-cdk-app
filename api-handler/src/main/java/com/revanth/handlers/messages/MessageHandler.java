package com.revanth.handlers.messages;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revanth.models.Message;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbTable<Message> messageTable;

    public MessageHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        messageTable = enhancedClient.table(System.getenv("MESSAGES_TABLE_NAME"), TableSchema.fromBean(Message.class));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String httpMethod = request.getHttpMethod();
        String responseMessage;

        try {
            if ("POST".equals(httpMethod) && request.getPath().equals("/messages/send")) {
                responseMessage = sendMessage(request.getBody());
            } else if ("GET".equals(httpMethod) && request.getPath().equals("/messages/history")) {
                Map<String, String> queryParams = request.getQueryStringParameters();
                responseMessage = getMessageHistory(queryParams);
            } else {
                return createResponse(400, "Unsupported HTTP method.");
            }

            return createResponse(200, responseMessage);
        } catch (Exception e) {
            return createResponse(500, "Error: " + e.getMessage());
        }
    }

    private String sendMessage(String requestBody) {
        Message message = parseRequestBody(requestBody);

        // Generate a unique conversationId
        message.setConversationId(UUID.randomUUID().toString());

        // Save the message to DynamoDB
        messageTable.putItem(message);

        return "Message sent successfully.";
    }

    private String getMessageHistory(Map<String, String> queryParams) {
        if (queryParams == null) {
            throw new IllegalArgumentException("Query parameters are required.");
        }

        String sender = queryParams.get("sender");
        String receiver = queryParams.get("receiver");

        List<Message> messages;
        if (sender != null && receiver == null) {
            messages = messageTable.query(QueryConditional.keyEqualTo(k -> k.partitionValue(sender)))
                    .items()
                    .stream()
                    .collect(Collectors.toList());
        } else if (receiver != null && sender == null) {
            messages = messageTable.query(QueryConditional.keyEqualTo(k -> k.partitionValue(receiver)))
                    .items()
                    .stream()
                    .collect(Collectors.toList());
        } else if (sender != null) {
            messages = messageTable.scan().items().stream()
                    .filter(message -> sender.equals(message.getSender()) || receiver.equals(message.getReceiver()))
                    .collect(Collectors.toList());
        } else {
            messages = messageTable.scan().items().stream().collect(Collectors.toList());
        }

        return toJson(messages);
    }

    private String toJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON: " + e.getMessage());
        }
    }

    private Message parseRequestBody(String requestBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(requestBody, Message.class);
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
