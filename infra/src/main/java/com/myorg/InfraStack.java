package com.myorg;

import com.myorg.infrastructure.APIGatewayStack;
import com.myorg.infrastructure.DynamoDBStack;
import com.myorg.infrastructure.LambdaStack;
import software.amazon.awscdk.App;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class InfraStack extends Stack {
    public InfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        // example resource
        // final Queue queue = Queue.Builder.create(this, "InfraQueue")
        //         .visibilityTimeout(Duration.seconds(300))
        //         .build();

        // Instantiate DynamoDBStack
        new DynamoDBStack(this, "DynamoDBStack", StackProps.builder().build());

        // Instantiate LambdaStack
        new LambdaStack(this, "LambdaStack", StackProps.builder().build());

        // Instantiate APIGatewayStack
        new APIGatewayStack(this, "APIGatewayStack", StackProps.builder().build());
    }

    public static void main(final String[] args) {
        App app = new App();

        // Create the InfraStack
        new InfraStack(app, "InfraStack", StackProps.builder().build());

        app.synth();
    }
}