package com.narakeet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class PollingApi {
  private final String apiKey;
  private final String apiUrl;
  private final int pollingIntervalSeconds;

  public static class AudioTaskRequest {
    private String voice;
    private String text;
    private String format;

    public AudioTaskRequest(String format, String voice, String text) {
      this.voice = voice;
      this.text = text;
      this.format = format;
    }
    public String getVoice() {
      return voice;
    }
    public String getText() {
      return text;
    }
    public String getFormat() {
      return format;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BuildTask {
    private String statusUrl;
    private String taskId;

    public String getStatusUrl() {
      return statusUrl;
    }
    public void setStatusUrl(String statusUrl) {
      this.statusUrl = statusUrl;
    }
    public String getTaskId() {
      return taskId;
    }
    public void setTaskId(String taskId) {
      this.taskId = taskId;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BuildTaskStatus {
    private String message;
    private int percent;
    private boolean succeeded;
    private boolean finished;
    private String result;

    public String getMessage() {
      return message;
    }
    public void setMessage(String message) {
      this.message = message;
    }
    public int getPercent() {
      return percent;
    }
    public void setPercent(int percent) {
      this.percent = percent;
    }
    public boolean isSucceeded() {
      return succeeded;
    }
    public void setSucceeded(boolean succeeded) {
      this.succeeded = succeeded;
    }
    public boolean isFinished() {
      return finished;
    }
    public void setFinished(boolean finished) {
      this.finished = finished;
    }
    public String getResult() {
      return result;
    }
    public void setResult(String result) {
      this.result = result;
    }
  }


  public PollingApi(String apiKey, String apiUrl, int pollingIntervalSeconds) {
    this.apiKey = apiKey;
    this.apiUrl = apiUrl;
    this.pollingIntervalSeconds = pollingIntervalSeconds;
  }

  public PollingApi(String apiKey) {
    this(apiKey, "https://api.narakeet.com", 5);
  }
  public BuildTask requestAudioTask(AudioTaskRequest audioTaskRequest) throws IOException {

    String url = String.format("%s/text-to-speech/%s?voice=%s", this.apiUrl, audioTaskRequest.getFormat(), audioTaskRequest.getVoice());
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost request = new HttpPost(url);
    request.setHeader("Content-Type", "text/plain");
    request.setHeader("x-api-key", apiKey);

    byte[] utf8Bytes = audioTaskRequest.getText().getBytes(StandardCharsets.UTF_8);
    ByteArrayEntity requestBody = new ByteArrayEntity(utf8Bytes);
    request.setEntity(requestBody);

    HttpResponse response = httpClient.execute(request);
    ObjectMapper mapper = new ObjectMapper();
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= 200 && statusCode < 300) {
      HttpEntity responseEntity = response.getEntity();
      String responseJson = EntityUtils.toString(responseEntity);
      return mapper.readValue(responseJson, BuildTask.class);
    } else {
      String responseString = EntityUtils.toString(response.getEntity());
      throw new IOException("HTTP error: " + statusCode + " " + responseString);
    }
  }
  public BuildTaskStatus pollUntilFinished(BuildTask buildTask, Consumer<BuildTaskStatus> progressCallback) throws IOException {
    while (true) {
      HttpClient httpClient = HttpClientBuilder.create().build();
      try {
        HttpGet httpGet = new HttpGet(buildTask.getStatusUrl());
        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 200 && statusCode < 300) {
          String responseContent = EntityUtils.toString(response.getEntity());
          ObjectMapper objectMapper = new ObjectMapper();
          BuildTaskStatus buildTaskStatus = objectMapper.readValue(responseContent, BuildTaskStatus.class);
          if (buildTaskStatus.isFinished()) {
            return buildTaskStatus;
          }
          if (progressCallback != null) {
            progressCallback.accept(buildTaskStatus);
          }
        } else {
          String responseString = EntityUtils.toString(response.getEntity());
          throw new IOException("HTTP error: " + statusCode + " " + responseString);
        }
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
      try {
        Thread.sleep(pollingIntervalSeconds * 1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
  public BuildTaskStatus pollUntilFinished(BuildTask buildTask) throws IOException {
    return pollUntilFinished(buildTask, null);
  }
  public String downloadToTempFile(String url, String extension) throws IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet httpGet = new HttpGet(url);
    HttpResponse response = httpClient.execute(httpGet);
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= 200 && statusCode < 300) {
      String tempPath = System.getProperty("java.io.tmpdir");
      File tempFile = new File(tempPath, UUID.randomUUID().toString() + "." + extension);
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      response.getEntity().writeTo(outputStream);
      outputStream.close();
      return tempFile.getAbsolutePath();
    } else {
      String responseString = EntityUtils.toString(response.getEntity());
      throw new IOException("HTTP error: " + statusCode + " " + responseString);
    }
  }
}
