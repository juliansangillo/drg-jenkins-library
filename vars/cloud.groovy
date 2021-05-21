def login(String cloudProject, String jenkinsCredentialsId) {
   withCredentials([file(credentialsId: jenkinsCredentialsId, variable: 'SA_KEY')]) {
       sh (
         script: "gcloud auth activate-service-account jenkins@unity-firebuild.iam.gserviceaccount.com --key-file=${SA_KEY} --project=${cloudProject}",
         label: 'Google Cloud Activate Service Account'
       )
   }
}

def configureDocker() {
    sh (
        script: "gcloud auth configure-docker",
        label: "Google Cloud Configure Docker"
    )
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
       script: "gsutil ls -l 'gs:${url}/' || exit 0",
       label: 'Google Storage List Objects',
       returnStdout: true
   ).trim()
   
   if(objectStr != '') {
    def objects = objectStr.split(' ')
    objects.each{ obj ->
        sh (
            script: "gsutil -m -q cp -r \"${obj}\" \"${projectPath}\"",
            label: 'Google Storage Download'
        )
    }
   }
   
   echo 'Cache pulled successfully'
}

def deployToRun(String serviceName, String region, String imageName, String version,
        String env, String port, String serviceAccount, String memory,
        String cpu, String timeout, String maximumRequests, String maxInstances,
        String dbInstance = "", String vpcConnector = "", String vpcEgress = "") {
    echo 'Deploying to google cloud run ...'
    
    def envVars = "ASPNETCORE_ENVIRONMENT=${env},HOST=0.0.0.0"
    def secretNames = sh (script: "gcloud secrets list --filter='labels.env_var:true labels:${env.toLowerCase()}' --format='(name:sort=1:label=)'", returnStdout: true).split('\n')
    for(secretName in secretNames) {
        def varName = secretName.substring(0, secretName.lastIndexOf('-')).replaceAll('__', ':')
        def varValue = sh (script: "gcloud secrets versions access latest --secret=${secretName}", returnStdout: true)
        
        envVars += ",${varName}=${varValue}"
    }
    
    def db_config = ""
    if(dbInstance) {
        db_config = "--set-cloudsql-instances=${dbInstance}"
    }
    
    def vpc_connector = ""
    def vpc_egress = ""
    if(vpcConnector) {
        vpc_connector = "--vpc-connector=${vpcConnector}"
        if(vpcEgress) {
            vpc_egress = "--vpc-egress=${vpcEgress}"
        }
    }
    
    sh (
        script: "gcloud run deploy ${serviceName} --quiet --platform=managed --region=${region} --image=${imageName}:${version} --set-env-vars=${envVars} --port=${port} --service-account=${serviceAccount} --memory=${memory} --cpu=${cpu} --timeout=${timeout} --concurrency=${maximumRequests} --max-instances=${maxInstances} ${db_config} ${vpc_connector} ${vpc_egress}",
        label: 'Google cloud run deploy'
    )
}
