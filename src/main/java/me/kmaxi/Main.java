package me.kmaxi;

import java.io.File;
import java.io.IOException;

import static me.kmaxi.CanvasLogic.downloadTSFile;
import static me.kmaxi.FFMPEGLogic.*;

public class Main {


    private static String link = "https://vod-cache.kaltura.nordu.net/hls/p/322/sp/32200/serveFlavor/entryId/0_wxxap85v/v/2/ev/2/flavorId/0_vssz0qt1/name/a.mp4/seg-1-sdsdsdsds";
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


    public static void main(String[] args) {

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


        } catch (IOException e) {
            e.printStackTrace();
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
                    if (file.isFile()){
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

