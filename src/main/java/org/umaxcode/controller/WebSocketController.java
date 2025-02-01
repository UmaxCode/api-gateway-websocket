package org.umaxcode.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umaxcode.dto.Message;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.GoneException;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

import java.net.URI;

@RestController
@RequestMapping("/wss")
public class WebSocketController {

    @Value("${aws.api-websocket.endpoint}")
    private String websocketEndpoint;

    @PostMapping("/send")
    public String SendMessage(@RequestBody Message message) {

        ApiGatewayManagementApiClient client = ApiGatewayManagementApiClient.builder()
                .endpointOverride(URI.create(websocketEndpoint))
                .build();

        PostToConnectionRequest postRequest = PostToConnectionRequest.builder()
                .connectionId(message.connectionId())
                .data(SdkBytes.fromUtf8String(message.message()))
                .build();

        try {
            client.postToConnection(postRequest);
            return "Message sent successfully!";
        } catch (GoneException e) {
            System.err.println("Connection is stale: " + message.connectionId());
            return "Failed: Connection is stale.";
        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
            return  "Error sending message.";
        }
    }
}
