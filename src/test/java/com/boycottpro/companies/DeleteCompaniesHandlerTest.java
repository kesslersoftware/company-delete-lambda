package com.boycottpro.companies;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCompaniesHandlerTest {

    @Mock
    private DynamoDbClient dynamoDb;

    @Mock
    private Context context;

    @InjectMocks
    private DeleteCompaniesHandler handler;

    @Test
    void testDeleteCompany_Success() {
        String testCompanyId = "c123";

        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("company_id", testCompanyId));

        // Stub deleteItem call
        when(dynamoDb.deleteItem(any(DeleteItemRequest.class)))
                .thenReturn(DeleteItemResponse.builder().build());

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("compay deleted = true"));
        ArgumentCaptor<DeleteItemRequest> captor = ArgumentCaptor.forClass(DeleteItemRequest.class);
        verify(dynamoDb, times(1)).deleteItem(captor.capture());

        DeleteItemRequest capturedRequest = captor.getValue();
        assertEquals("companies", capturedRequest.tableName());
        assertEquals(testCompanyId, capturedRequest.key().get("company_id").s());
    }

    @Test
    void testDeleteCompany_MissingCompanyId() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent()
                .withPathParameters(null);  // Simulate missing path params

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing company_id"));
        verify(dynamoDb, never()).deleteItem(any(DeleteItemRequest.class));
    }

    @Test
    void testDeleteCompany_Exception() {
        String testCompanyId = "c456";

        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("company_id", testCompanyId));

        when(dynamoDb.deleteItem(any(DeleteItemRequest.class)))
                .thenThrow(RuntimeException.class);

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Unexpected server error"));
        verify(dynamoDb, times(1)).deleteItem(any(DeleteItemRequest.class));
    }
}
