import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

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
    private Table table;
    private Regions REGION = Regions.US_EAST_1;
    
    public Object handleResetRequest(SNSEvent request, Context context) {

      String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
      context.getLogger().log("Invocation started: " + timeStamp);
      context.getLogger().log("Request is NULL: " + (request == null));

      context.getLogger().log("Request number is: " + (request.getRecords().size()));

      context.getLogger().log("Request by: " + request.getRecords().get(0).getSNS().getMessage());
      this.initDynamoDbClient();
      context.getLogger().log("dynamoDb: " + dynamoDb.toString());
      persistData(request);
      timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
      context.getLogger().log("Invocation completed: " + timeStamp);
// 
//        PersonResponse personResponse = new PersonResponse();
//        personResponse.setMessage("Saved Successfully!!!");
//        return personResponse;
      return request;
    }

  private PutItemOutcome persistData(SNSEvent request) throws ConditionalCheckFailedException
  {

        return this.table.putItem(
                        new PutItemSpec().withItem(new Item()
                          .withString("id", request.getRecords().get(0).getSNS().getMessage())
                          .withString("value", request.getRecords().get(0).getSNS().getMessageId())));
    }
 
  private void initDynamoDbClient() {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        this.dynamoDb = new DynamoDB(client);
        this.table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
    }

}

