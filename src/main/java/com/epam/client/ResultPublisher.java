package com.epam.client;

import com.epam.model.folders.Folders;
import com.epam.model.folders.Values;
import com.epam.model.testcycle.TestCycleBuilder;
import com.epam.utils.PropertiesUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ResultPublisher {

    private final PropertiesUtil propertiesUtil = new PropertiesUtil();
    private final String baseUri = propertiesUtil.read("baseUri");
    private final String uriSuffix = propertiesUtil.read("uriSuffix");
    private final String apiKey = propertiesUtil.read("apiKey");
    private final String projectKey = propertiesUtil.read("projectKey");
    private final String autoCreateTestCases = propertiesUtil.read("autoCreateTestCases");
    private final String resultsFileExtension = propertiesUtil.read("resultsFileExtension");
    private final String resultsFolder = propertiesUtil.read("resultsFolder");
    private final File resultsFile = new File(resultsFolder + "testResults.zip");

    private final boolean customTestCycle = Boolean.parseBoolean(propertiesUtil.read("customTestCycle"));
    private final String testCycleName = propertiesUtil.read("testCycleName");
    private final String testCycleFolderName = propertiesUtil.read("testCycleFolderName");
    private final String testCycleDescription = propertiesUtil.read("testCycleDescription");
    private final Integer testCycleJiraProjectVersion = Integer.parseInt(propertiesUtil.read("testCycleJiraProjectVersion"));

    private final String folderType = propertiesUtil.read("folderType");
    private final String maxResults = propertiesUtil.read("maxResults");

    public String publishResult() {
//        log.info("baseUri " + baseUri);
//        log.info("uriSuffix " + uriSuffix);
//        log.info("apiKey " + apiKey);
//        log.info("projectKey " + projectKey);
//        log.info("autoCreateTestCases " + autoCreateTestCases);
//        log.info("resultsFileExtension " + resultsFileExtension);
//        log.info("resultsFolder " + resultsFolder);
//        log.info("customTestCycle " + customTestCycle);
//        log.info("testCycleName " + testCycleName);
//        log.info("testCycleFolderName " + testCycleFolderName);
//        log.info("testCycleDescription " + testCycleDescription);
//        log.info("testCycleJiraProjectVersion " + testCycleJiraProjectVersion);
//        log.info("folderType " + folderType);
//        log.info("maxResults " + maxResults);

        String testCycleResponse = null;

        try {
            String uri = baseUri + uriSuffix;
            deleteExistingFile(resultsFile);
            File zipFile = createZip(resultsFile, resultsFileExtension);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("projectKey", projectKey));
            params.add(new BasicNameValuePair("autoCreateTestCases", autoCreateTestCases));
            URIBuilder uriBuilder = new URIBuilder(uri);
            uriBuilder.addParameters(params);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost postRequest = new HttpPost(uriBuilder.build());
            postRequest.setHeader("Authorization", "Bearer " + apiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.STRICT);

            builder.addBinaryBody(
                    "file",
                    new FileInputStream(zipFile),
                    ContentType.MULTIPART_FORM_DATA,
                    zipFile.getName()
            );

            if (customTestCycle) {
                String testCycle = generateTestCycle(testCycleName, testCycleDescription, testCycleFolderName,
                        testCycleJiraProjectVersion);

                if (testCycle != null) {
                    builder.addTextBody(
                            "testCycle",
                            testCycle,
                            ContentType.APPLICATION_JSON
                    );
                }
            }

            HttpEntity multipart = builder.build();
            postRequest.setEntity(multipart);

            CloseableHttpResponse response = httpClient.execute(postRequest);
            testCycleResponse = EntityUtils.toString(response.getEntity());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return testCycleResponse;
    }


    private boolean deleteExistingFile(File file) {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    private File createZip(File resultsFile, String fileExtension) throws IOException {

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

    private boolean waitIsFileNotEmpty(String filePath) {
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

    private List<String> findAllFilesWithExtension(Path folderPath, String fileExtension) throws IOException {
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

    private String generateTestCycle(String name, String description, String folderName, int jiraProjectVersion) throws IOException, URISyntaxException {
        Folders folders = getFolders();
        List<Integer> ids = getFoldersIdsByName(folders, folderName);

        if (ids.size() != 1) {
            return null;
        }

        return TestCycleBuilder.builder()
                .name(name)
                .description(description)
                .jiraProjectVersion(jiraProjectVersion)
                .folderId(ids.get(0))
                .build().toString();
    }


    public Folders getFolders() throws IOException, URISyntaxException {
        Folders folders = null;
        String uri = baseUri + "folders";

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("projectKey", projectKey));
        params.add(new BasicNameValuePair("folderType", folderType));
        params.add(new BasicNameValuePair("maxResults", maxResults));

        URIBuilder uriBuilder = new URIBuilder(uri);
        uriBuilder.addParameters(params);

        HttpGet getRequest = new HttpGet(uriBuilder.build());
        getRequest.setHeader("Authorization", "Bearer " + apiKey);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(getRequest);
        int responseCode = response.getStatusLine().getStatusCode();
        log.info("Get folders response code for folder type " + folderType + " is: " + responseCode);

        if (responseCode == 200) {
            String responseEntity = EntityUtils.toString(response.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            folders = objectMapper.readValue(responseEntity, new TypeReference<Folders>() {
            });
        }
        return folders;
    }

    private List<Integer> getFoldersIdsByName(Folders folders, String folderName) {
        return folders.getValues().stream()
                .filter(p -> p.getName().equalsIgnoreCase(folderName))
                .map(Values::getId)
                .collect(Collectors.toList());
    }
}
