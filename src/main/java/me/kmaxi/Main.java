package me.kmaxi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static me.kmaxi.CanvasLogic.downloadTSFile;
import static me.kmaxi.FFMPEGLogic.*;

public class Main {


    private static String link = "https://vod-cache.kaltura.nordu.net/hls/p/322/sp/32200/serveFlavor/entryId/0_bovg59dj/v/2/ev/2/flavorId/0_l7256o16/name/a.mp4/seg-1-v1-a1.ts";
    //   private static final String link = "https://vod-cache.kaltura.nordu.net/hls/p/322/sp/32200/serveFlavor/entryId/0_qkkoglf8/v/2/ev/2/flavorId/0_tip1bup6/name/a.mp4/seg-";
    //   private static final String link = "https://vod-cache.kaltura.nordu.net/hls/p/322/sp/32200/serveFlavor/entryId/0_x1rt6pla/v/2/ev/2/flavorId/0_8kkqflz6/name/a.mp4/seg-";
    private static String fileName = "Test";

    /**
     * Set this to wherever you have installed FFMPEG which is needed for the video combination, and conversion. You can download it <a href="https://ffmpeg.org/">here</a>
     */
    public static final String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";

    /**
     * The max amount of segments. 1000 should be more the enough for almost everything, but if you only want the video until a specific part, feel free to lower this
     */
    private static final int maxSegments = 1000;

    private static final String destinationFolder = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "CanvasTranscriber";


    public static void main(String[] args) throws IOException {


        fileName = fileName.replace(" ", "-");
        fileName = fileName.replace("\n", "");
        fileName = fileName.replaceAll("[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-]", "");


        CleanDir(destinationFolder);
        CleanDir(destinationFolder + "\\temp");
        CleanDir(destinationFolder + "\\outputs");
        CleanDir(destinationFolder + "\\outputs\\split");

        File folder = new File(destinationFolder + "\\final");
        if (!folder.exists()) {
            folder.mkdir();
        }
        link = cleanLink(link);

        try {

            for (int i = 1; i < maxSegments; i++) {
                String url = link + i + "-v1-a1.ts";
                if (!downloadTSFile(url, destinationFolder, (i + 999) + ".ts"))
                    break;
            }
            File combinedFile = combineVideos(destinationFolder, fileName);
            if (combinedFile == null) {
                System.err.println("Error combining videos");
                return;
            }
            String mp3FilePath = convertMp4ToMp3(combinedFile.getAbsolutePath(), true);

            if (mp3FilePath.isEmpty()) {
                System.err.println("Error converting MP4 to MP3");
                return;
            }

            splitMp3(mp3FilePath, true);

            //  Whisper.transcribeAndSaveToFile(Config.openAiAPIKey, mp3FilePath);
            TranscribeSplitFiles();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void TranscribeSplitFiles() throws IOException {
        File finalFile = new File(destinationFolder + "\\final\\" + fileName + ".txt");
        if (!finalFile.exists()) {
            finalFile.createNewFile();
        }

        //For each file in the split folder, transcribe it and save it to a file
        File[] files = new File(destinationFolder + "\\outputs\\split").listFiles();
        System.out.println("Transcribing " + files.length + " files");
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String filePath = file.getAbsolutePath();
                    if (Whisper.transcribeAndSaveToFile(Config.openAiAPIKey, filePath)) {

                        //Deltete the mp3 audio file
                        if (!file.delete()) {
                            System.err.println("Error deleting file: " + file.getAbsolutePath());
                        }

                        //Read the .txt file that was created from the transcribeAndSaveToFile method
                        File transcriptFile = new File(filePath + ".txt");
                        //Add the transcribed .txt files to the final folder with a file name that matches the variable fileName. The  Whisper.transcribeAndSaveToFile creates a .txt file with the same name as the audio file in the same directory
                        // This file will contain the combination of all .txt files that are created
                        StringBuilder transcript = new StringBuilder();
                        try (Scanner scanner = new Scanner(transcriptFile)) {
                            while (scanner.hasNextLine()) {
                                transcript.append(scanner.nextLine()).append("\n");
                            }
                        } catch (FileNotFoundException e) {
                            System.err.println("Error reading file: " + transcriptFile.getAbsolutePath());
                        }

                        //Add the Transcript to the final file in the final using a buffered reader
                        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(finalFile, true))) {
                            writer.write(transcript.toString());
                        } catch (IOException e) {
                            System.err.println("Error writing to file: " + finalFile.getAbsolutePath());
                        }
                    }
                }
            }
        }

    }

    private static void CleanDir(String dir) {
        File folder = new File(dir);
        if (!folder.exists()) {
            folder.mkdir();
        } else {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    //Only delete files and not directories
                    if (file.isFile()) {
                        file.delete();
                        System.out.println("Deleting: " + file.getAbsolutePath());

                    }
                }
            }
        }
    }

    private static String cleanLink(String link) {
        String[] linkParts = link.split("seg-");
        return linkParts[0] + "seg-";
    }


}

