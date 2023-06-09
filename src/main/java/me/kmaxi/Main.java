package me.kmaxi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class Main {



    private static final String link = "https://vod-cache.kaltura.nordu.net/hls/p/322/sp/32200/serveFlavor/entryId/0_zs02ezm0/v/12/ev/4/flavorId/0_5d17h95m/name/a.mp4/seg-";
    private static String fileName = "Test";

    /**
     * Set this to wherever you have installed FFMPEG which is needed for the video combination, and conversion. You can download it <a href="https://ffmpeg.org/">here</a>
     */
    private static final String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";

    /**
     * The max amount of segments. 1000 should be more the enough for almost everything, but if you only want the video until a specific part, feel free to lower this
     */
    private static final int maxSegments = 1000;

    private static final String destinationFolder = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "CanvasTranscriber";



    public static void main(String[] args) {

        fileName = fileName.replace(" ", "-");
        fileName = fileName.replace("\n", "");
        fileName = fileName.replaceAll("[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-]", "");


        try {


            for (int i = 1; i < maxSegments; i++) {
                String url = link + i + "-v1-a1.ts";
                if (!downloadTSFile(url, destinationFolder, (i + 999) + ".ts"))
                    break;
            }
            combineVideos(destinationFolder);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean downloadTSFile(String url, String destinationFolder, String fileName) throws IOException {

        destinationFolder += "\\temp";


        // Extract the file name from the URL
        //String fileName = url.substring(url.lastIndexOf('/') + 1);

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

    public static void combineVideos(String directoryPath) {
        String outputPath = directoryPath + "\\outputs" + File.separator + fileName + ".mp4";

        directoryPath += "\\temp";

        try {
            File directory = new File(directoryPath);
            if (!directory.isDirectory()) {
                System.err.println("Error: " + directoryPath + " is not a directory.");
                return;
            }

            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".ts"));
            if (files == null || files.length == 0) {
                System.err.println("Error: no .ts files found in directory " + directoryPath);
                return;
            }

            Arrays.sort(files, Comparator.comparing(File::getName)); // sort files by name

            String inputFiles = " -f concat -safe 0 -i \"" + directoryPath + File.separator + "input.txt\"";
            String inputTxtPath = directoryPath + File.separator + "input.txt";
            PrintWriter writer = new PrintWriter(inputTxtPath);

            for (File file : files) {
                writer.println("file '" + file.getAbsolutePath() + "'");
            }
            writer.close();

            String ffmpegCommand = ffmpegPath + inputFiles + " -c:v copy -c:a copy \"" + outputPath + "\"";

            Process process = Runtime.getRuntime().exec(ffmpegCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            System.out.println("Video successfully combined and saved as " + outputPath);

            // get a list of all files in the directory
            File[] allFiles = directory.listFiles();

            // delete all files that are not named "video.mp4"
            assert allFiles != null;
            for (File file : allFiles) {
                if (file.getName().equals(fileName + ".mp4")) {
                    convertMp4ToMp3(file.getAbsolutePath());
                }
                if (!file.delete()) {
                    System.err.println("Error deleting file: " + file.getAbsolutePath());
                }
            }

            File outPutVideo = new File(outputPath);
            convertMp4ToMp3(outPutVideo.getAbsolutePath());


        } catch (Exception e) {
            System.err.println("Error combining videos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Converts an mp4 video file to a mp3 file using FFMPEG
     *
     * @param mp4FilePath Path to a mp4 file
     */
    public static void convertMp4ToMp3(String mp4FilePath) {
        try {
            File mp4File = new File(mp4FilePath);
            if (!mp4File.exists() || !mp4File.isFile() || !mp4FilePath.toLowerCase().endsWith(".mp4")) {
                System.err.println("Error: " + mp4FilePath + " is not a valid MP4 file path.");
                return;
            }

            String mp3FilePath = mp4File.getParent() + File.separator + mp4File.getName().replace(".mp4", ".mp3");
            String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";
            String ffmpegCommand = ffmpegPath + " -i \"" + mp4FilePath + "\" -vn -acodec libmp3lame -q:a 4 \"" + mp3FilePath + "\"";

            Process process = Runtime.getRuntime().exec(ffmpegCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            System.out.println("MP3 file successfully created and saved as " + mp3FilePath);
            Whisper.transcribeAndSaveToFile(Config.openAiAPIKey, mp3FilePath);

        } catch (Exception e) {
            System.err.println("Error converting MP4 to MP3: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

