# Narakeet Text To Speech Polling (Long content) API example in Java

This repository provides a quick example demonstrating how to access the Narakeet Narakeet [Long Content API](https://www.narakeet.com/docs/automating/text-to-speech-api/#polling) from Java.

The long content API is suitable for large audio conversion tasks, and can
produce professional quality uncompressed WAV files using realistic text to
speech.

Note that Narakeet also has a simpler text to speech API, suitable for smaller conversion tasks, that directly streams back the results. 
See the [Streaming Text to Speech API Example](https://github.com/narakeet/text-to-speech-api-java-example) for more information on how to use that.

The example sends a request to generate an audio file, then downloads the resulting audio into a local file. 

## Prerequisites

To use this example, you will need Java 11 or later, and Maven 3 or later, and an API key for Narakeet.

The example uses the [`org.apache.http`](https://hc.apache.org/) library to execute HTTPS requests.

## Running the example

1. Set a local environment variable called `NARAKEET_API_KEY`, containing your API key (or modify [src/main/java/com/narakeet/Example.java](src/main/java/com/narakeet/Example.java) line 8 to include your API key).
2. optionally modify [src/main/java/com/narakeet/Example.java](src/main/java/com/narakeet/Example.java) lines 11 to change the voice, format and text sent to the text to speech engine.
2. run `mvn install` to download the dependencies
3. run `mvn compile exec:java` to run the conversion.

## Running the example from Docker

For easy development, this repository also includes a Docker container with Amazon Correto 8, which can be used to run the conversion without installing Java locally. To use Docker:

1. run `make init` to create the Docker image locally
2. run `make bootstrap` to download Maven dependencies
3. run `make execute NARAKEET_API_KEY=<YOUR API KEY>` to produce the result file.
4. check the `output` subdirectory to play the downloaded file

## More information

Check out <https://www.narakeet.com/docs/automating/rest/> for more information on the Narakeet Markdown to Video API. 

