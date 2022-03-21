package com.epam.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class FileUtil {

    public boolean deleteExistingFile(File file) {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public File createZip(File resultsFile, String fileExtension) throws IOException {
        Path folderPath = Paths.get(resultsFile.getParent());
        List<String> filePaths = findAllFilesWithExtension(folderPath, fileExtension);

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(resultsFile.getAbsolutePath()))) {
            for (String file : filePaths) {
                waitIsFileNotEmpty(file);
                File fileToZip = new File(file);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }

            return new File(resultsFile.getAbsolutePath());
        }
    }

    public boolean waitIsFileNotEmpty(String filePath) {
        int counter = 10;
        File file = new File(filePath);

        while (file.length() == 0) {
            if (counter == 0) {
                return false;
            }

            log.warn("File " + file.getAbsolutePath() + " is empty. Waiting 2 sec before retry...");
            wait(2);
            file = new File(filePath);

            if (file.length() > 0) {
                return true;
            }
            counter--;
        }
        return false;
    }

    private void wait(int sec){
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public List<String> findAllFilesWithExtension(Path folderPath, String fileExtension) throws IOException {
        if (!Files.isDirectory(folderPath)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<String> result;

        try (Stream<Path> walk = Files.walk(folderPath)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension))
                    .collect(Collectors.toList());
        }
        return result;
    }
}
