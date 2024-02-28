package com.aws.capstone;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.*;
import software.amazon.awscdk.services.s3.Bucket;

public class DynamoDBCapstoneStack extends Stack {

    public DynamoDBCapstoneStack(final App parent, final String name, final StackProps props) {
        
    	super(parent, name, props);

        Table dynamoDBTable = Table.Builder.create(this, "DynamoDBTable")
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .stream(StreamViewType.NEW_AND_OLD_IMAGES)
                .build();

        Function crudLambda = Function.Builder.create(this, "CrudLambdaFunction")
                .runtime(Runtime.PYTHON_3_12)
                .handler("lambda.lambda_handler")
                .code(Code.fromBucket(Bucket.fromBucketName(this, "DeploymentBucket1", "deployment-bucket"), "lambda/deployment/crud-lambda.zip"))
                .environment(java.util.Map.of(
                        "DYNAMODB_TABLE_NAME", dynamoDBTable.getTableName()
                ))
                .build();

        dynamoDBTable.grantReadWriteData(crudLambda);

        Function streamLambda = Function.Builder.create(this, "StreamLambdaFunction")
                .runtime(Runtime.PYTHON_3_12)
                .handler("lambda.lambda_handler")
                .code(Code.fromBucket(Bucket.fromBucketName(this, "DeploymentBucket2", "deployment-bucket"), "lambda/deployment/stream-lambda.zip"))
                .build();

        dynamoDBTable.grantStreamRead(streamLambda);

        streamLambda.addEventSource(new DynamoEventSource(dynamoDBTable, DynamoEventSourceProps.builder()
                .startingPosition(StartingPosition.LATEST)
                .batchSize(10)
                .build()));

        RestApi restApi = RestApi.Builder.create(this, "RestAPI")
                .restApiName("RestAPI")
                .build();

        LambdaIntegration crudIntegration = new LambdaIntegration(crudLambda);
        Resource crudResource1 = restApi.getRoot().addResource("crud");
        crudResource1.addMethod("POST", crudIntegration);
        Resource crudResource2 = crudResource1.addResource("{id}");
        crudResource2.addMethod("GET", crudIntegration);
        crudResource2.addMethod("PUT", crudIntegration);
        crudResource2.addMethod("DELETE", crudIntegration);

    }

}