Task.Runner(
    name: "gradlwe",
    description: "Gradle Wrapper Executable",
    executable: "gradlew.bat",
    workingDirectory: "%~dp0",
    arguments: "%*",
    environmentVariables: {
        JAVA_HOME: "%JAVA_HOME%"
    },
    outputEncoding: "UTF-8"
)