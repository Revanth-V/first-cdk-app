package com.myorg.infrastructure;

import java.util.Map;

public class EnvironmentConfig {
    public static final Map<String, String> CLIENTS_ENV = Map.of("CLIENTS_TABLE_NAME", "ClientsTable");
    public static final Map<String, String> THERAPISTS_ENV = Map.of("THERAPISTS_TABLE_NAME", "TherapistsTable");
    public static final Map<String, String> MESSAGES_ENV = Map.of("MESSAGES_TABLE_NAME", "MessagesTable");
    public static final Map<String, String> JOURNALS_ENV = Map.of("JOURNALS_TABLE_NAME", "JournalsTable");
    public static final Map<String, String> SESSIONS_ENV = Map.of("SESSIONS_TABLE_NAME", "SessionsTable");
    public static final Map<String, String> MAPPINGS_ENV = Map.of("MAPPINGS_TABLE_NAME", "ClientTherapistMappingsTable");
    public static final Map<String, String> APPOINTMENTS_ENV = Map.of("APPOINTMENTS_TABLE_NAME", "AppointmentsTable");

}
