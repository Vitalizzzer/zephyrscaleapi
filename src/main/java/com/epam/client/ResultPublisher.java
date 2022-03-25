package com.epam.client;

import com.epam.model.folders.Folders;
import com.epam.model.folders.Values;
import com.epam.model.testcycle.TestCycleBuilder;
import com.epam.utils.FileUtil;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ResultPublisher {

    private final FileUtil fileUtil;
    private final String baseUri;
    private final String uriSuffix;
    private final String apiKey;
    private final String projectKey;
    private final String autoCreateTestCases;
    private final String resultsFileExtension;
    private final File resultsFile;
    private final boolean customTestCycle;
    private final String testCycleName;
    private final String testCycleFolderName;
    private final String testCycleDescription;
    private final Integer testCycleJiraProjectVersion;
    private final String customFields;
    private final String folderType;
    private final String maxResults;

    public ResultPublisher(FileUtil fileUtil, PropertiesUtil propertiesUtil) throws IOException {
        this.fileUtil = fileUtil;
        baseUri = propertiesUtil.readProperties("baseUri");
        uriSuffix = propertiesUtil.readProperties("uriSuffix");
        apiKey = propertiesUtil.readProperties("apiKey");
        projectKey = propertiesUtil.readProperties("projectKey");
        autoCreateTestCases = propertiesUtil.readProperties("autoCreateTestCases");
        resultsFileExtension = propertiesUtil.readProperties("resultsFileExtension");

        Path resultsPath = Paths.get(propertiesUtil.readProperties("resultsFolder"));
        Path projectRootPath = fileUtil.getProjectRootPath();
        resultsFile = new File(projectRootPath + "/" + resultsPath + "/testResults.zip");

        customTestCycle = Boolean.parseBoolean(propertiesUtil.readProperties("customTestCycle"));
        testCycleName = propertiesUtil.readProperties("testCycleName");
        testCycleFolderName = propertiesUtil.readProperties("testCycleFolderName");
        testCycleDescription = propertiesUtil.readProperties("testCycleDescription");
        testCycleJiraProjectVersion = Integer.parseInt(propertiesUtil.readProperties("testCycleJiraProjectVersion"));
        customFields = propertiesUtil.readProperties("customFields");

        folderType = propertiesUtil.readProperties("folderType");
        maxResults = propertiesUtil.readProperties("maxResults");
    }

    public String publishResult() throws IOException, URISyntaxException {
        String testCycleResponse;
        String uri = baseUri + uriSuffix;
        fileUtil.deleteExistingFile(resultsFile);
        log.info("Expected results file: " + resultsFile);
        File zipFile = fileUtil.createZip(resultsFile, resultsFileExtension);

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
                    testCycleJiraProjectVersion, customFields);

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
        int responseCode = response.getStatusLine().getStatusCode();
        log.info("Response status code: " + responseCode);
        testCycleResponse = EntityUtils.toString(response.getEntity());

        if (responseCode != 200) {
            log.warn("Response body: " + testCycleResponse);
            log.warn("Note: response code 400 doesn't mean the results were not published. " +
                    "Please check Zephyr Scale Tests and Cycles.");
        }

        return testCycleResponse;
    }

    private String generateTestCycle(String name, String description, String folderName,
                                     int jiraProjectVersion, String customFields) throws IOException, URISyntaxException {
        Folders folders = getZephyrFolders();
        List<Integer> ids = getZephyrFoldersIdsByName(folders, folderName);

        if (ids.size() != 1) {
            return null;
        }

        String s = TestCycleBuilder.builder()
                .name(name)
                .description(description)
                .jiraProjectVersion(jiraProjectVersion)
                .folderId(ids.get(0))
                .customFields(customFields)
                .build().toString();

        log.info("TestCycleBuilder: "+s);
        return s;
    }

    public Folders getZephyrFolders() throws IOException, URISyntaxException {
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
        log.info("Get Zephyr Scale folders response status code: " + responseCode);

        if (responseCode == 200) {
            String responseEntity = EntityUtils.toString(response.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            folders = objectMapper.readValue(responseEntity, new TypeReference<>() {
            });
        }
        return folders;
    }

    private List<Integer> getZephyrFoldersIdsByName(Folders folders, String folderName) {
        return folders.getValues().stream()
                .filter(p -> p.getName().equalsIgnoreCase(folderName))
                .map(Values::getId)
                .collect(Collectors.toList());
    }
}
