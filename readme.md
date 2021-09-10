# Azure SDK Maven Tooling

## Useful commands:

### Generating a new project

Use the archetype functionality. Before this can be done (because the Maven archetype is not published to Mave Central yet), you must firstly install locally using the `mvn install` command. Once installed, run as follows:

```shell
 mvn archetype:generate                        \
   -DarchetypeGroupId=com.azure.tools          \
   -DarchetypeArtifactId=azure-maven-archetype \
   -DarchetypeVersion=1.0.0-SNAPSHOT           \
   -DgroupId=<your.groupId>                    \
   -DartifactId=<your.artifactId>
```

#### Update the local archetype catalog

If the above command doesn't work because the archetype could not be found, try running the following commands to
 update your local archetype catalog.

```shell
mvn install archetype:update-local-catalog
mvn archetype:crawl
```

#### Issue with maven-archetype-plugin version 3.0.0
If you see the following error, replace `mvn archetype:generate ` in the above command with `mvn org.apache.maven.plugins:maven-archetype-plugin:2.4:generate`. 
For more details, please look at [this issue](https://github.com/adobe/aem-project-archetype/issues/76#issuecomment-282680691).
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-archetype-plugin:3.2.0:generate (default-cli) on project standalone-pom: The desired archetype does not exist (com.azure.tools:azure-maven-archetype:1.0.0-SNAPSHOT) -> [Help 1]

```

### Running the build tool

There is a testPom.xml file available for testing with. You can run the build tool by firstly installing it with `mvn install`, and then by calling it as such:

```shell
mvn -f tests/appconfig/pom.xml azure:run
```

For faster development cycles, it is recommended to chain together the install and azure:run steps, as such:

```shell
mvn install && mvn -f tests/appconfig/pom.xml azure:run
```
