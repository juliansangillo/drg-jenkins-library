def init(String prodBranch, String testBranch, String devBranch, String prodIsPrerelease, String testIsPrerelease, String devIsPrerelease, String changelogFileName, String changelogTitle) {

    def releaserc = libraryResource('com/deltarenegadegames/unityci/releaserc.json')
    
    releaserc = releaserc.replaceAll('%PROD_BRANCH%', prodBranch)
    releaserc = releaserc.replaceAll('%TEST_BRANCH%', testBranch)
    releaserc = releaserc.replaceAll('%DEV_BRANCH%', devBranch)
    releaserc = releaserc.replaceAll('%PROD_IS_PRERELEASE%', prodIsPrerelease)
    releaserc = releaserc.replaceAll('%TEST_IS_PRERELEASE%', testIsPrerelease)
    releaserc = releaserc.replaceAll('%DEV_IS_PRERELEASE%', devIsPrerelease)
    releaserc = releaserc.replaceAll('%CHANGELOG_FILE_NAME%', changelogFileName)
    releaserc = releaserc.replaceAll('%CHANGELOG_TITLE%', changelogTitle)
    
    File releasercFile = new File('.releaserc')
    releasercFile.write releaserc

    echo releasercFile.text
}

def version(String githubToken) {

    env.GITHUB_TOKEN = githubToken

    def version = sh (
        script: 'semantic-release -d | grep -oP "Published release \\K.*? " | xargs',
        returnStdout: true
    )

    return version
} 

def release() {

}
