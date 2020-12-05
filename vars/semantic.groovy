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
    
    writeFile file: '.releaserc', text: releaserc
    
    sh 'cat .releaserc'
}

def version(String githubCredentialsId) {

    withCredentials([usernamePassword(credentialsId: githubCredentialsId, passwordVariable: 'GITHUB_TOKEN')]) {
        def version = sh (
            script: 'semantic-release -d | grep -oP "Published release \\K.*? " | xargs',
            returnStdout: true
        )
    }

    return version
} 

def release(String githubCredentialsId) {

    withCredentials([usernamePassword(credentialsId: githubCredentialsId, passwordVariable: 'GITHUB_TOKEN')]) {
        def status = sh (
            script: 'semantic-release',
            returnStatus: true
        )
        
        if(status != 0) {
            def num = sh (
                script: '''
                    OWNER=$(cat .git/config | grep "url" | grep -oP "https://github.com/\K.*/" | tr -d '/');
                    REPO=$(cat .git/config | grep "url" | grep -oP "https://github.com/.*/\K.*." | tr -d '.git');
                
                    RELEASE_ID=$(curl -H "Authorization: token $GITHUB_TOKEN" "https://api.github.com/repos/$OWNER/$REPO/releases/tags/v$VERSION" 2> /dev/null | jq -r '.id');
                
                    curl -X DELETE -H "Authorization: token $GITHUB_TOKEN" "https://api.github.com/repos/$OWNER/$REPO/releases/$RELEASE_ID";
                
                    git config user.password "$GITHUB_TOKEN";
                    git tag -d v$VERSION;
                    git push origin :v$VERSION
                ''',
                returnStatus: true
            )
            error "Release failed with exit code: ${status}"
        }
    }

}
