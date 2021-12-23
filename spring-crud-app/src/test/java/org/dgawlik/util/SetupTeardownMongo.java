package org.dgawlik.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * It was quicker this way than using Testcontainers.
 */
public class SetupTeardownMongo implements BeforeAllCallback, AfterAllCallback {

    public static final String START_COMMAND = "docker-compose" +
            " -f mongo-for-tests.yml up -d";
    public static final String STOP_COMMAND = "docker-compose" +
            " -f mongo-for-tests.yml down && " +
            "docker-compose -f mongo-for-tests.yml rm";

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        Runtime.getRuntime().exec(STOP_COMMAND).waitFor();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Runtime.getRuntime().exec(START_COMMAND).waitFor();
    }
}
