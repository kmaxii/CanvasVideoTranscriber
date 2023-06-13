package me.kmaxi;

import okhttp3.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Whisper {

    private static final int UPLOAD_TIMEOUT_SECONDS = 200; // Set the desired upload timeout value in seconds
    private static final int CONNECTION_TIMEOUT_SECONDS = 300; // Set the desired connection timeout value in seconds


    public static void main(String[] args) throws IOException {
        transcribeAndSaveToFile(Config.openAiAPIKey, "C:\\Users\\esst9\\Desktop\\Downloads\\test.mp3");
    }

    public static void transcribeAndSaveToFile(String apiKey, String audioFilePath) throws IOException {
        String transcription = transcribeAudio(apiKey, audioFilePath);

        File audioFile = new File(audioFilePath);
        String outputFilePath = audioFile.getParent() + File.separator + audioFile.getName() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(transcription);
        }
    }
    public static String transcribeAudio(String apiKey, String audioFilePath) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .connectTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .callTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "audio.wav",
                        RequestBody.create(mediaType, new File(audioFilePath)))
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("response_format", "text")
                .build();

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .post(body)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                return responseBody;
            } else {
                throw new IOException("Request failed with code: " + response.code()
                        + "\nError message: " + responseBody);
            }
        } finally {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }
}