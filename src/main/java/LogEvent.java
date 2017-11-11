
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.UUID;

public class LogEvent implements RequestHandler<SNSEvent, Object> {

  public Object handleRequest(SNSEvent request, Context context) {

    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

    context.getLogger().log("Invocation started: " + timeStamp);

    context.getLogger().log("Request is NULL: " + (request == null));

    context.getLogger().log("Request number is: " + (request.getRecords().size()));

    context.getLogger().log(request.getRecords().get(0).getSNS().getMessage());


    timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

    context.getLogger().log("Invocation completed: " + timeStamp);

    return null;
  }

    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "csye6225-template";
    private Regions REGION = Regions.US_EAST_1;
    
    public Object handleResetRequest(SNSEvent request, Context context) {

      String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
      context.getLogger().log("Invocation started: " + timeStamp);
      context.getLogger().log("Request is NULL: " + (request == null));

      context.getLogger().log("Request number is: " + (request.getRecords().size()));

      context.getLogger().log("Request by: " + request.getRecords().get(0).getSNS().getMessage());
      context.getLogger().log("Request ID: " + request.getRecords().get(0).getSNS().getMessageId());
      this.initDynamoDbClient();
      context.getLogger().log("dynamoDb: " + dynamoDb.toString());
      String id = request.getRecords().get(0).getSNS().getMessage();
      String tolken  = request.getRecords().get(0).getSNS().getMessageId();
//      if(checkData(id)){
        persistData(id, tolken);
        resetPost(id, tolken,context);
//      }

      timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
      context.getLogger().log("Invocation completed: " + timeStamp);
// 
//        PersonResponse personResponse = new PersonResponse();
//        personResponse.setMessage("Saved Successfully!!!");
//        return personResponse;
      return request;
    }

  private PutItemOutcome persistData(String id, String tolken) throws ConditionalCheckFailedException
  {
        return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
          .putItem(
                        new PutItemSpec().withItem(new Item()
                          .withString("id", id)
                          .withString("value", tolken)));
    }

//  private boolean checkData(String id) throws ConditionalCheckFailedException
//  {
//    return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem(new GetItemSpec().withAttributesToGet("id")) == null;
//  }
//
  private void initDynamoDbClient() {
    AmazonDynamoDBClient client = new AmazonDynamoDBClient();
    client.setRegion(Region.getRegion(REGION));
    this.dynamoDb = new DynamoDB(client);
  }

//  @RequestMapping(value = "/user/resetPassword", method = RequestMethod.POST, produces = "application/json")
//  @ResponseBody
  public String resetPost(String id, String tolken, Context context) {
//    JsonObject jsonObject = new JsonObject();
//    Gson gson = new Gson();
//    User user = gson.fromJson(sUser, User.class);
//    String email = user.getEmail();
//
//    //validate email
//    int index = email.indexOf("@");
//    if (index <= 0 || index >= email.length() - 1) {
//      jsonObject.addProperty("message", "Invalid Email!");
//      return jsonObject.toString();
//    }

    //validate user exist
//    User user_db = userRepository.findByEmail(email);

    //generate token
//    UUID uuid = UUID.randomUUID();

    //generate url
    String resetUrl="init url";
//    if (user_db != null) {
      resetUrl = "http://localhost:8080/csye6225app/user/reset?email="+id+"&token="+tolken;
//      jsonObject.addProperty("message",  email + ", password reset email generate successfully! "+" url: "+resetUrl);
//    } else {
//      jsonObject.addProperty("message", "Password reset failure!  " + email + " not exists! ");
//    }

    //send email with reset url
    final String FROM = "reset_password@csye6225-fall2017-mawenhe.me";
//    final String TO = "jing.yu@husky.neu.edu";
    final String TO = id;
//    final String CONFIGSET = "set1";
    final String SUBJECT = "Password Reset Confirm";
    final String HTMLBODY = "Password Reset URL: "+resetUrl;

    final String TEXTBODY = "This email was sent through Amazon SES "
            + "using the AWS SDK for Java.";
    try {
      AmazonSimpleEmailService client =
              AmazonSimpleEmailServiceClientBuilder.standard()
                      // Replace US_WEST_2 with the AWS Region you're using for
                      // Amazon SES.
                      .withRegion(Regions.US_EAST_1).build();
      SendEmailRequest request = new SendEmailRequest()
              .withDestination(
                      new Destination().withToAddresses(TO))
              .withMessage(new Message()
                      .withBody(new Body()
                              .withHtml(new Content()
                                      .withCharset("UTF-8").withData(HTMLBODY))
                              .withText(new Content()
                                      .withCharset("UTF-8").withData(TEXTBODY)))
                      .withSubject(new Content()
                              .withCharset("UTF-8").withData(SUBJECT)))
              .withSource(FROM);
      // Comment or remove the next line if you are not using a
      // configuration set
//              .withConfigurationSetName(CONFIGSET);
      client.sendEmail(request);
      context.getLogger().log("status: " + "Email sent!");
//      jsonObject.addProperty("status", "Email sent!");
    } catch (Exception ex) {
      context.getLogger().log("status: " + "Email sent failure!" + ex.getMessage());
//      jsonObject.addProperty("status", "Email sent failure!"
//              + ex.getMessage());
    }

    return "success";
  }
}

