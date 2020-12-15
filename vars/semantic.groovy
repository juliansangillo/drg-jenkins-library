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
    
    sh (
        script: 'cat .releaserc',
        label: 'Print .releaserc'
    )
}

def version(String githubCredentialsId) {

    def version = ''

    withCredentials([usernamePassword(credentialsId: githubCredentialsId, usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_TOKEN')]) {
        version = sh (
            script: 'semantic-release -d | grep -oP "Published release \\K.*? " | xargs',
            label: 'Get next version',
            returnStdout: true
        )
    }

    return version
} 

def release(String githubCredentialsId) {

    withCredentials([usernamePassword(credentialsId: githubCredentialsId, usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_TOKEN')]) {
        def status = sh (
            script: 'semantic-release',
            label: 'Release',
            returnStatus: true
        )
        
        if(status != 1) {
            def num = sh (
                script: '''
                    OWNER=$(cat .git/config | grep "url" | grep -oP "https://github.com/\\K.*/" | tr -d '/');
                    REPO=$(cat .git/config | grep "url" | grep -oP "https://github.com/.*/\\K.*." | tr -d '.git');
                
                    RELEASE_ID=$(curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/$OWNER/$REPO/releases/tags/v$VERSION | jq -r '.id');
                
                    curl -X DELETE -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/$OWNER/$REPO/releases/$RELEASE_ID;
                
                    git remote rm origin;
                    git remote add origin https://$OWNER:$GITHUB_TOKEN@github.com/$OWNER/$REPO.git;
                    git tag -d v$VERSION;
                    git push origin :v$VERSION;
                    
                    MESSAGE="$(git log -1 --pretty=%B | grep "chore(release): " | sed "s/chore(release): //g")";
                    git revert -n HEAD;
                    git commit -m "revert(release): $MESSAGE";
                    git push
                ''',
                label: 'Release rollback',
                returnStatus: true
            )
            error "Release failed with exit code: ${status}"
        }
    }

}
