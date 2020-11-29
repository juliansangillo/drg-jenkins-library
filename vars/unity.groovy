def init(String dockerImage) {
    docker pull dockerImage
}

def build(String localRepoPath, String dockerImage, String projectPath, String platform, String fileExtensions, String buildName, String version, boolean isDevelopmentBuild) {
    def extensions = [:]
    fileExtensions.split(',').each {pair ->
        def nameAndValue = pair.split(':')
        extensions[nameAndValue[0]] = nameAndValue[1]
    }

    def extension = extensions[platform]
    def fileExtensionArg = ""
    if(extension) {
        fileExtensionArg = "-fileExtension ${extension}"
    }

    def developmentBuildFlag = ""
    if(isDevelopmentBuild) {
        developmentBuildFlag = "-developmentBuild"
    }

    sh """
    docker container run \
    --mount type=bind,source=${localRepoPath},target=/var/unity-home \
    ${dockerImage} \
    -projectPath "${projectPath}" \
    -platform ${platform} \
    ${fileExtensionArg} \
    -buildName "${buildName}" \
    -version ${version} \
    ${developmentBuildFlag}
    """
}
