 def login(String cloudProject, String jenkinsCredentialsId) {
    withCredentials([file(credentialsId: jenkinsCredentialsId, variable: 'SA_KEY')]) {
        sh (
          script: "gcloud auth activate-service-account jenkins@unity-firebuild.iam.gserviceaccount.com --key-file=${SA_KEY} --project=${cloudProject}",
          label: 'Google Cloud Activate Service Account'
        )
    }
 }
 
