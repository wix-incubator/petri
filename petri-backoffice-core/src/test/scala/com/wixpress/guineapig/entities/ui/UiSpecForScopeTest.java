package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class UiSpecForScopeTest {

    private static final String TEST_SCOPE = "test-scope";

    @Test
    public void canBeSerialized() throws IOException {
        UiSpecForScope sourceSpec = UiSpecForScopeBuilder.anUiSpec()
                .withScope(TEST_SCOPE)
                .withStartDate(-1)
                .withEndDate(-1)
                .withKey("spec1")
                .withGroups(asList(new UiTestGroup(0, "group1", 0), new UiTestGroup(0, "group2", 0)))
                .withForRegisteredUsers(true)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(sourceSpec);
        UiSpecForScope serializedSpec = objectMapper.readValue(json, new TypeReference<UiSpecForScope>() {
        });
        assertEquals(serializedSpec, sourceSpec);

    }

    @Test
    public void canBeSerializedWithoutUsers() throws IOException {
        UiSpecForScope sourceSpec = UiSpecForScopeBuilder.anUiSpec()
                .withScope(TEST_SCOPE)
                .withStartDate(-1)
                .withEndDate(-1)
                .withKey("spec1")
                .withGroups(asList(new UiTestGroup(0, "group1", 0), new UiTestGroup(0, "group2", 0)))
                .build();
        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(sourceSpec);
        UiSpecForScope serializedSpec = objectMapper.readValue(json, new TypeReference<UiSpecForScope>() {
        });
        assertEquals(serializedSpec, sourceSpec);
    }
}
