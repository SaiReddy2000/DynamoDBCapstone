package com.aws.capstone;

import software.amazon.awscdk.App;
import software.amazon.awscdk.DefaultStackSynthesizer;
import software.amazon.awscdk.StackProps;

public class DynamoDBCapstoneApp {

	public static void main(final String[] args) {

		App app = new App();

		DefaultStackSynthesizer synthesizer = DefaultStackSynthesizer.Builder.create()
				.generateBootstrapVersionRule(false).build();
		new DynamoDBCapstoneStack(app, "dynamo-db-capstone-stack",
				StackProps.builder().stackName("dynamo-db-capstone-stack").synthesizer(synthesizer).build());

		app.synth();

	}

}