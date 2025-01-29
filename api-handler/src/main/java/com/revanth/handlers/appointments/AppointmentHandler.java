package com.revanth.handlers.appointments;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revanth.models.Appointment;
import com.revanth.models.ClientTherapistMapping;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AppointmentHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbTable<Appointment> appointmentTable;
    private final DynamoDbTable<ClientTherapistMapping> mappingTable;

    public AppointmentHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        appointmentTable = enhancedClient.table(System.getenv("APPOINTMENTS_TABLE_NAME"), TableSchema.fromBean(Appointment.class));
        mappingTable = enhancedClient.table(System.getenv("MAPPINGS_TABLE_NAME"), TableSchema.fromBean(ClientTherapistMapping.class));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String httpMethod = request.getHttpMethod();
        String responseMessage;

        try {
            if ("POST".equals(httpMethod) && request.getPath().equals("/appointment/request")) {
                responseMessage = requestAppointment(request.getBody());
            } else {
                return createResponse(400, "Unsupported HTTP method.");
            }

            return createResponse(200, responseMessage);
        } catch (Exception e) {
            return createResponse(500, "Error: " + e.getMessage());
        }
    }

    private String requestAppointment(String requestBody) {
        Appointment appointment = parseRequestBody(requestBody);

        if (appointment.getClientId() == null || appointment.getTherapistId() == null) {
            throw new IllegalArgumentException("clientId and therapistId are required.");
        }

        ClientTherapistMapping key = new ClientTherapistMapping();
        key.setClientId(appointment.getClientId());
        key.setTherapistId(appointment.getTherapistId());

        ClientTherapistMapping mapping = mappingTable.getItem(key);
        if (mapping == null) {
            return "Appointment not possible as client and therapist are not mapped.";
        }

        // Generate unique appointmentId
        appointment.setAppointmentId(UUID.randomUUID().toString());

        // Save the appointment to DynamoDB
        appointmentTable.putItem(appointment);

        return "Appointment successfully set.";
    }

    private Appointment parseRequestBody(String requestBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(requestBody, Appointment.class);
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
