def login(String cloudProject, String jenkinsCredentialsId) {
   withCredentials([file(credentialsId: jenkinsCredentialsId, variable: 'SA_KEY')]) {
       sh (
         script: "gcloud auth activate-service-account jenkins@unity-firebuild.iam.gserviceaccount.com --key-file=${SA_KEY} --project=${cloudProject}",
         label: 'Google Cloud Activate Service Account'
       )
   }
}

def cache(String url, String projectPath, String... objects) {
    echo 'Pushing to cache ...'
    
    objects.each{ obj ->
        sh (
            script: "gsutil -m -q rsync -d -r \"${projectPath}/${obj}\" \"gs:${url}/${obj}/\"",
            label: 'Google Storage Upload'
        )
    }
    
    echo 'Cache pushed successfully'
}
 
def uncache(String url, String projectPath) {
   echo 'Pulling from cache ...'
   
   def objectStr = sh(
       script: "gsutil ls -l 'gs:${url}/'",
       label: 'Google Storage List Objects',
       returnStdout: true
   )
   
   def objects = objectStr.split(' ')
   objects.each{ obj ->
       sh (
           script: "gsutil -m -q cp -r \"${obj}\" \"${projectPath}\"",
           label: 'Google Storage Download'
       )
   }
   
   echo 'Cache pulled successfully'
}
 
