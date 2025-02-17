package org.umaxcode;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

public class ConnectDisconnectLambdaHandler implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

    private static final String TABLE_NAME = "WebSocketConnections";
    private final DynamoDbClient dynamoDbClient;

    public ConnectDisconnectLambdaHandler() {
        this.dynamoDbClient = DynamoDbClient.create();
    }

    @Override
    public APIGatewayV2WebSocketResponse handleRequest(APIGatewayV2WebSocketEvent event, Context context) {

        context.getLogger().log(event.toString());

        String routeKey = event.getRequestContext().getRouteKey();
        String connectionId = event.getRequestContext().getConnectionId();
        APIGatewayV2WebSocketResponse errorResponse = new APIGatewayV2WebSocketResponse();
        errorResponse.setStatusCode(401);
        errorResponse.setBody("Unsupported route key: " + routeKey);


        APIGatewayV2WebSocketResponse successResponse = new APIGatewayV2WebSocketResponse();
        successResponse.setStatusCode(200);
        successResponse.setBody("Processed: " + routeKey);

        switch (routeKey) {
            case "$connect":
                handleConnect(connectionId);
                break;
            case "$disconnect":
                handleDisconnect(connectionId);
                break;
            default:
                return errorResponse;
        }
        return successResponse;
    }

    private void handleConnect(String connectionId) {
        // Store the connection details in DynamoDB
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("connectionId", AttributeValue.builder().s(connectionId).build());
        item.put("email", AttributeValue.builder().s("email@gmail.com").build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(putItemRequest);
    }

    private void handleDisconnect(String connectionId) {
        // Remove the connectionId from DynamoDB
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("connectionId", AttributeValue.builder().s(connectionId).build());
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        dynamoDbClient.deleteItem(deleteItemRequest);
    }

}


