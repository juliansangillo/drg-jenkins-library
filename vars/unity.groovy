def init(String dockerImage) {
    sh (
        script: "docker pull ${dockerImage}",
        label: 'Pull Unity docker image'
    )
}

def build(String localRepoPath, String dockerImage, String projectPath, String platform, String fileExtensions, String buildName, String version, String isDevelopmentBuild) {
    def extensions = [:]
    fileExtensions.split(' ').each {pair ->
        def nameAndValue = pair.split(':')
        extensions[nameAndValue[0]] = nameAndValue[1]
    }

    def extension = extensions[platform]
    def fileExtensionArg = ""
    if(extension) {
        fileExtensionArg = "-fileExtension ${extension}"
    }
    
    def versionArg = ""
    if(version) {
        versionArg = "-version ${version}"
    }

    def developmentBuildFlag = ""
    if(isDevelopmentBuild == 'true') {
        developmentBuildFlag = '-developmentBuild';
    }

    sh (
        script: """
    docker container run \
    --mount type=bind,source=${localRepoPath},target=/var/unity-home \
    ${dockerImage} \
    -projectPath "${projectPath}" \
    -platform ${platform} \
    ${fileExtensionArg} \
    -buildName "${buildName}" \
    ${versionArg} ${developmentBuildFlag}
    """,
        label: 'Unity docker container build'
    )
}
