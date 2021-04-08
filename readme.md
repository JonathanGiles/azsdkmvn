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

### Running the build tool

There is a testPom.xml file available for testing with. You can run the build tool by firstly installing it with `mvn install`, and then by calling it as such:

```shell
mvn -f build-tool/src/test/resources/testPom.xml azure:run
```

For faster development cycles, it is recommended to chain together the install and azure:run steps, as such:

```shell
mvn install && mvn -f build-tool/src/test/resources/testPom.xml azure:run
```
