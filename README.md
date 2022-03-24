## Zephyr Scale API - Automation Results Update 
Detailed Automation API requests can be found here: https://support.smartbear.com/zephyr-scale-cloud/api-docs/#tag/Automations
<br/>
How to generate API KEY: https://support.smartbear.com/zephyr-scale-cloud/docs/rest-api/generating-api-access-tokens.html
<br/>

## Prerequisites
1. Create zephyr.properties file in the resources folder of your project<br/>
2. Add the following properties:
    #### Zephyr Scale connection details. E.g. for Cucumber report:
    baseUri=https://api.zephyrscale.smartbear.com/v2/ <br/>
    uriSuffix=automations/executions/cucumber <br/>
    apiKey=<AUTHORIZATION_API_KEY> <br/>
    projectKey=<PROJECT_KEY> <br/>
    autoCreateTestCases=true <br/>
    
    #### Results file details. E.g. for Cucumber report:
    resultsFolder=target/cucumber-report/ <br/>
    resultsFileExtension=json <br/>
    
    #### Customize test cycle details (optional). If customTestCycle=false, testCycle params are ignored. 
    customTestCycle=false <br/>
    testCycleName=Test Cycle Name <br/>
    testCycleDescription=Test Cycle Description <br/>
    testCycleFolderName=Test Cycle Folder (make sure the folder name exists in Zephyr Scale)<br/>
    testCycleJiraProjectVersion=1 <br/>
3. Security Note: Such parameters as apiKey and projectKey should be provided as environment variables (e.g. from CI, command line or Vault).

## Usage as lib in an external project:
    Build jar file of this zephyrscaleapi project

### Gradle project:
1. Create 'libs' folder in the root directory of your project
2. Copy 'zephyrapiaccess-1.0.jar' into 'libs' folder
3. In build.gradle:
   1. Add dependency to build.gradle: <br/>
      scheduleRuntime files("libs/zephyrscaleapi-1.0.jar")
   2. Add configuration: <br/>
       configurations {
         scheduleRuntime {
            extendsFrom implementation
          }
        }
   3. Add task: <br/>
      task runScheduleReader(type: JavaExec) {
      workingDir("libs")
      mainClass.set("com.epam.Main")
      classpath = configurations.scheduleRuntime
      }
   4. Make "runScheduleReader" task be executed after the main task: <br/>
    e.g. test.finalizedBy(runScheduleReader)
