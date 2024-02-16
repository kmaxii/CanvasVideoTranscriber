package me.kmaxi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CanvasLogic {

    /**
     * Downloads a TS file from a given URL and saves it to a given destination folder with a given file name
     * @param url The URL of the TS file
     * @param destinationFolder The folder to save the file to. The file will be saved in a subfolder called "temp"
     * @param fileName The name that the file should be saved as
     * @return true if the file was downloaded successfully, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public static boolean downloadTSFile(String url, String destinationFolder, String fileName) throws IOException {

        destinationFolder += "\\temp";

        // Combine the destination folder path with the file name
        Path destinationPath = Paths.get(destinationFolder, fileName);

        // Create a connection to the URL and set the request method to GET
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        // Set up an input stream to read from the connection
        InputStream inputStream;

        int statusCode = connection.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
            inputStream = connection.getInputStream();
            // process the input stream
        } else {
            return false;
        }

        // Set up an output stream to write to the destination file
        FileOutputStream outputStream = new FileOutputStream(destinationPath.toFile());

        // Read from the input stream and write to the output stream until the end of the stream is reached
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        // Close the input and output streams
        inputStream.close();
        outputStream.close();

        return true;
    }
}
