package me.kmaxi;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;

import static me.kmaxi.Main.ffmpegPath;

public class FFMPEGLogic {
    public static File combineVideos(String directoryPath, String fileName) {
        String outputPath = directoryPath + "\\outputs" + File.separator + fileName + ".mp4";

        directoryPath += "\\temp";

        try {
            File directory = new File(directoryPath);
            if (!directory.isDirectory()) {
                System.err.println("Error: " + directoryPath + " is not a directory.");
                return null;
            }

            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".ts"));
            if (files == null || files.length == 0) {
                System.err.println("Error: no .ts files found in directory " + directoryPath);
                return null;
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

                if (!file.delete()) {
                    System.err.println("Error deleting file: " + file.getAbsolutePath());
                }
            }

            File outPutVideo = new File(outputPath);
            return outPutVideo;
        } catch (Exception e) {
            System.err.println("Error combining videos: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts an mp4 video file to a mp3 file using FFMPEG
     *
     * @param mp4FilePath Path to a mp4 file
     * @return The path to the newly created mp3 file. Empty string if the conversion failed
     */
    public static String convertMp4ToMp3(String mp4FilePath, boolean deleteMp4) {
        try {
            File mp4File = new File(mp4FilePath);
            if (!mp4File.exists() || !mp4File.isFile() || !mp4FilePath.toLowerCase().endsWith(".mp4")) {
                System.err.println("Error: " + mp4FilePath + " is not a valid MP4 file path.");
                return "";
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

            //  Whisper.transcribeAndSaveToFile(Config.openAiAPIKey, mp3FilePath);

            if (deleteMp4) {
                if (!mp4File.delete()) {
                    System.err.println("Error deleting MP4 file: " + mp4FilePath);
                }
            }

            return mp3FilePath;


        } catch (Exception e) {
            System.err.println("Error converting MP4 to MP3: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public static void splitMp3(String filePath, boolean deleteMp3) {
        try {
            File mp3File = new File(filePath);
            if (!mp3File.exists() || !mp3File.isFile() || !filePath.toLowerCase().endsWith(".mp3")) {
                System.err.println("Error: " + filePath + " is not a valid MP3 file path.");
                return;
            }
            String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";
            String outputFolder = mp3File.getParent() + File.separator + "split";

            String ffmpegCommand = ffmpegPath + " -i \"" + filePath + "\" -f segment -segment_time 1800 -c copy \"" + outputFolder + File.separator + "output%03d.mp3\"";
            Process process = Runtime.getRuntime().exec(ffmpegCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            System.out.println("MP3 file successfully split into 1800 second segments and saved in " + outputFolder);

            if (deleteMp3) {
                if (!mp3File.delete()) {
                    System.err.println("Error deleting MP3 file: " + filePath);
                }
            }

        } catch (Exception e) {
            System.err.println("Error splitting MP3: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
