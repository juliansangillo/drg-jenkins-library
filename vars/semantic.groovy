def version(String githubToken) {

    env.GITHUB_TOKEN = githubToken

    def version = sh (
        script: 'semantic-release -d | grep -oP "Published release \K.*? " | xargs',
        returnStdout: true
    )

    return version
} 

def release() {

}
