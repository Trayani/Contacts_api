package cz.fio;


import com.fasterxml.jackson.databind.ObjectMapper;
import cz.fio.config.ContactsApiConfig;
import cz.fio.dto.ContactStorageResult;
import cz.fio.dto.StorageResultType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.RequestEntity.post;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class TestBase {

    @LocalServerPort
    int randomServerPort;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected ContactsApiConfig config;
    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void clearTestData() {
        var file = new java.io.File(config.getContactsFilePath(), "rw");
        if (file.exists())
            file.delete();
    }

    String getRequestUri(String firstName, String lastName, String email) {
        var stringBuilder = new StringBuilder("http://localhost:")
                .append(randomServerPort)
                .append("/contacts/storeContact");

        var queryStarted = false;


        if (firstName != null) {
            stringBuilder.append("?firstName=").append(firstName);
            queryStarted = true;
        }
        if (lastName != null) {
            stringBuilder.append((queryStarted ? '&' : '?') + "lastName=").append(lastName);
            queryStarted = true;
        }
        if (email != null) {
            stringBuilder.append((queryStarted ? '&' : '?') + "email=").append(email);
        }
        return stringBuilder.toString();
    }


    protected void testContactStore(String firstName,
                                    String lastName,
                                    String email,
                                    int expectedStatus
    ) {
        var uri = getRequestUri(firstName, lastName, email);
        int responseStatus;
        try {
            responseStatus = mockMvc.perform(MockMvcRequestBuilders.post(uri)).andReturn().getResponse().getStatus();
        } catch (Throwable e) {
            throw new AssertionError("POST request to contacts API failed", e);
        }
        assertEquals(expectedStatus, responseStatus);
    }

    protected void testContactStore(String firstName,
                                    String lastName,
                                    String email,
                                    boolean assertOkStatus
    ) {
        var uri = getRequestUri(firstName, lastName, email);
        int responseStatus;
        try {
            responseStatus = mockMvc.perform(MockMvcRequestBuilders.post(uri)).andReturn().getResponse().getStatus();
        } catch (Throwable e) {
            throw new AssertionError("POST request to contacts API failed", e);
        }

        assertTrue(assertOkStatus == (responseStatus == 200 || responseStatus == 201));
    }

    protected void testContactStore(String firstName,
                                    String lastName,
                                    String email,
                                    int expectedStatus,
                                    StorageResultType storageResultType
    ) {
        int responseStatus;
        ContactStorageResult responseBody;

        try {
            var uri = getRequestUri(firstName, lastName, email);
            var response = mockMvc.perform(MockMvcRequestBuilders.post(uri)).andReturn().getResponse();
            responseStatus = response.getStatus();
            responseBody = objectMapper.readValue(response.getContentAsString(), ContactStorageResult.class);
        } catch (Throwable e) {
            throw new AssertionError("POST request to contacts API failed", e);
        }

        assertEquals(expectedStatus, responseStatus);
        assertEquals(storageResultType, responseBody.type());
    }
}
