def login(String cloudProject, String jenkinsCredentialsId) {
   withCredentials([file(credentialsId: jenkinsCredentialsId, variable: 'SA_KEY')]) {
       sh (
         script: "gcloud auth activate-service-account jenkins@unity-firebuild.iam.gserviceaccount.com --key-file=${SA_KEY} --project=${cloudProject}",
         label: 'Google Cloud Activate Service Account'
       )
   }
}
 
def uncache(String cacheBucket, String jobName, String platform, String projectPath) {
   echo 'Pulling from cache ...'
   
   def objects = sh(
       script: "gsutil ls -l 'gs://${cacheBucket}/${jobName}/${platform}/'",
       label: "Google Storage List Objects BUCKET=${cacheBucket} JOB=${jobName} PLATFORM=${platform}",
       returnStdout: true
   )
   
   objects.split(' ').each{ obj ->
       sh (
           script: "gsutil -m -q cp -r \"${obj}\" \"${projectPath}\""
           label: 'Google Storage Download'
       )
   }
   
   echo 'Cache pulled successfully'
}
 
