## Zephyr Scale API - Automation Results Publisher
The tool provides an opportunity to update test cases status and publish automation test results into Zephyr Scale Cloud.  
Detailed Automation API requests and properties can be found here: [Zephyr Scale API](https://support.smartbear.com/zephyr-scale-cloud/api-docs/#tag/Automations)  
How to generate API KEY: [Generating API Access Tokens](https://support.smartbear.com/zephyr-scale-cloud/docs/rest-api/generating-api-access-tokens.html)  

## Prerequisites
1. Create zephyr.properties file in the resources folder of your project<br/>
2. Add the following properties:
    #### Zephyr Scale connection details. E.g. for Cucumber report:
   >baseUri=https://api.zephyrscale.smartbear.com/v2/  
    uriSuffix=automations/executions/cucumber  
    apiKey=<AUTHORIZATION_API_KEY>  
    projectKey=<PROJECT_KEY>  
    autoCreateTestCases=true
    
    #### Results file details. E.g. for Cucumber report:
   >resultsFolder=target/cucumber-report/  
    resultsFileExtension=json
    
    #### Customize test cycle details (optional). If customTestCycle=false, testCycle params are ignored. 
   >customTestCycle=false  
    testCycleName=Test Cycle Name  
    testCycleDescription=Test Cycle Description  
    testCycleFolderName=Test Cycle Folder (make sure the folder name exists in Zephyr Scale)  
    testCycleJiraProjectVersion=1  
    customFields={}
   
    #### Get folders for TEST_CASE, TEST_PLAN or TEST_CYCLE. Used only when customTestCycle=true. Use TEST_CYCLE as default.
   >folderType=TEST_CYCLE  
    maxResults=20
3. Security Note: Such parameters as ***apiKey*** and ***projectKey*** should be provided as environment variables (e.g. from CI, command line or Vault).

## Usage of the tool in the project:
> Build jar file of this zephyrscaleapi project

### Gradle project:
1. Create ***libs*** folder in the root directory of your project
2. Copy ***zephyrapiaccess-1.0.jar*** into 'libs' folder
3. In ***build.gradle***:
   - Add dependency to build.gradle:  
      ``` scheduleRuntime files("libs/zephyrscaleapi-1.0.jar") ```
   - Add configuration:  
      ```
      configurations {
          scheduleRuntime {
              extendsFrom implementation
          }
      }
     ```
   - Add task:  
      ```
      task runScheduleReader(type: JavaExec) {  
          workingDir("libs")  
          mainClass.set("com.epam.Main")   
          classpath = configurations.scheduleRuntime  
      }
     ```
   - Make "runScheduleReader" task be executed after the main task:  
    E.g.:
     ``` test.finalizedBy(runScheduleReader) ```

### Maven project
1. Create ***libs*** folder in the root directory of your project  
2. Copy ***zephyrapiaccess-1.0.jar*** into 'libs' folder  
3. In ***pom.xml***:
   - Add ***build*** block:  
      ```
      <build>
          <plugins>
              <plugin>
                  <groupId>org.apache.maven.plugins</groupId>
                  <artifactId>maven-surefire-plugin</artifactId>
                  <version>${maven-surefire-plugin.version}</version>
              </plugin>
              <plugin>
                  <groupId>org.codehaus.mojo</groupId>
                  <artifactId>exec-maven-plugin</artifactId>
                  <version>${exec-maven-plugin.version}</version>
                  <executions>
                      <execution>
                          <phase>test</phase>
                          <goals>
                              <goal>java</goal>
                          </goals>
                          <configuration>
                              <mainClass>com.epam.Main</mainClass>
                              <additionalClasspathElements>
                                  <additionalClasspathElement>
                                      libs/zephyrscaleapi-1.0.jar
                                  </additionalClasspathElement>
                              </additionalClasspathElements>
                          </configuration>
                      </execution>
                  </executions>
              </plugin>
          </plugins>
      </build>
      ```