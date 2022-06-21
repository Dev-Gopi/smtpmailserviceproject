package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import org.example.mail.Request;
import org.example.mail.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
{
    "name": "",
    "email": {
        "to": "",
        "from": ""
    },
    "content": {
        "subject": "Test Email",
        "body": "This is a test email"
    }
}
*/
public class MailApp implements RequestHandler<Request, Response>
{
    @Override
    public Response handleRequest(Request request, Context context) {
        Response response = new Response();
        if (dataValid(request, response)) {
            try {
                sendMessage(request);
                response.setMessageId("200");
                response.setMessage("Send");
            } catch (Exception exception) {
                response.setMessageId("999");
                response.setMessage("Unable to send " + exception);
            }
        }
        return response;
    }
    private String sendMessage(Request request){
        String QUEUE_NAME = "myfifoqueue";
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
//        try {
//            CreateQueueResult create_result = sqs.createQueue(QUEUE_NAME);
//        } catch (AmazonSQSException e) {
//            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
//                throw e;
//            }
//        }
        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("name", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(request.getName()));

        messageAttributes.put("to", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(request.email.getTo()));

        messageAttributes.put("from", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(request.email.getFrom()));

        messageAttributes.put("subject", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(request.content.getSubject()));


        // Send a message with an attribute.

        final SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(request.content.getBody())
                .withDelaySeconds(5)
                .withMessageAttributes(messageAttributes);
        sqs.sendMessage(sendMessageRequest);
        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
        System.out.println(messages);
        return null;
    }



    private Boolean dataValid(Request request, Response response){
        boolean status = false;
        Pattern pattern = Pattern.compile("[^A-Za-z\\d ]");
        Pattern email_pattern = Pattern.compile("^[a-z\\d+_&.-]+@[a-z\\d.-]+$");
        if (request.getName() == null && request.email.getTo() == null && request.email.getFrom() == null && request.content.getSubject() == null && request.content.getBody() == null) {
            String massageId = "404";
            String massage = " Except Only This JSON Sample Format <Required All>";
            response.setMessageId(massageId);
            response.setMessage(massage);
        } else {
            if (request.getName() == null) {
                response.setMessageId("404");
                response.setMessage("{ 'name' : 'Is Null' }");
            }
            else if (request.email.getTo() == null) {
                response.setMessageId("404");
                response.setMessage("{ 'to' : 'Is Null' }");
            }
            else if (request.email.getFrom() == null) {
                response.setMessageId("404");
                response.setMessage("{ 'from' : 'Is Null' }");
            }
            else if (request.content.getSubject() == null) {
                response.setMessageId("404");
                response.setMessage("{ 'subject' : 'Is Null' }");
            }
            else if (request.content.getBody() == null) {
                response.setMessageId("404");
                response.setMessage("{ 'body' : 'Is Null' }");
            }
            else {
                Matcher name = pattern.matcher(request.getName());
                Matcher toEmail = email_pattern.matcher(request.email.getTo().toLowerCase().replace(" ",""));
                Matcher formEmail = email_pattern.matcher(request.email.getFrom().toLowerCase().replace(" ",""));
                if (name.find()) {
                    response.setMessageId("404");
                    response.setMessage("{ 'name' : 'Dose Not Content Special Character' }");
                }
                else if (!toEmail.find()) {
                    response.setMessageId("404");
                    response.setMessage("{ 'to' : 'Email was not valid' }");
                }
                else if (!formEmail.find()) {
                    response.setMessageId("404");
                    response.setMessage("{ 'to' : 'Email was not valid' }");
                }
                else {
                    status = true;
                }
            }
        }
        return status;
    }
}
