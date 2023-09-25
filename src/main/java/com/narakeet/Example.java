package com.narakeet;
import java.util.Objects;

public class Example {


  public static void main(String[] args) throws java.io.FileNotFoundException, java.io.IOException {
    String apiKey = Objects.requireNonNull(System.getenv("NARAKEET_API_KEY"), "NARAKEET_API_KEY environment variable is not set");

    PollingApi api = new PollingApi(apiKey);
    PollingApi.AudioTaskRequest request = new PollingApi.AudioTaskRequest("mp3", "Ronald", "Hello there, this is the voice of the future");

    // start the build task and wait for it to finish
    PollingApi.BuildTask buildTask = api.requestAudioTask(request);
    PollingApi.BuildTaskStatus taskResult = api.pollUntilFinished(buildTask, buildTaskStatus -> {
      // do something more useful here
      System.out.println("Progress: " + buildTaskStatus.getMessage() + " (" + buildTaskStatus.getPercent() + "%)" );
    });

    // grab the results
    if (taskResult.isSucceeded()) {
      String filePath = api.downloadToTempFile(taskResult.getResult(), request.getFormat());
      System.out.println("Downloaded to " + filePath);
    } else {
      System.out.println("Error creating audio " + taskResult.getMessage());
    }
  }
}

