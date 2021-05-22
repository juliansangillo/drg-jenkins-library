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
    //def secretRegex = []
    def secretNames = sh (script: "gcloud secrets list --filter='labels.env_var:true labels:${env.toLowerCase()}' --format='(name:sort=1:label=)'", returnStdout: true).split('\n')
    for(secretName in secretNames) {
        def formattedSecretName = secretName.substring(0, secretName.lastIndexOf('-')).replaceAll('__', ':')
        def secret = sh (script: "gcloud secrets versions access latest --secret=${secretName}", returnStdout: true)
        
        envVars += ",${formattedSecretName}=${secret}"
        //secretRegex += "/(?<=CloudinarySettings:ApiKey=).+?(?=,|\$)/gm"
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
    
    wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [], varMaskRegexes: [[regex: "(?<=CloudinarySettings:ApiKey=).+?(?=,| |\$)"]]]) {
        withEnv(["SERVICE_NAME=${serviceName}", "REGION=${region}", "ENV_VARS=${envVars}", "PORT=${port}", "SERVICE_ACCOUNT=${serviceAccount}", "MEMORY=${memory}", "CPU=${cpu}", "TIMEOUT=${timeout}", "MAX_REQUESTS=${maximumRequests}", "MAX_INSTANCES=${maxInstances}", "DB_CONFIG=${db_config}", "VPC_CONNECTOR=${vpc_connector}", "VPC_EGRESS=${vpc_egress}", "IMAGE_NAME=${imageName}", "VERSION=${version}"]) {
            sh (
                script: '''
                gcloud run deploy $SERVICE_NAME \
                    --platform=managed \
                    --region=$REGION \
                    --set-env-vars=$ENV_VARS \
                    --port=$PORT \
                    --service-account=$SERVICE_ACCOUNT \
                    --memory=$MEMORY \
                    --cpu=$CPU \
                    --timeout=$TIMEOUT \
                    --concurrency=$MAX_REQUESTS \
                    --max-instances=$MAX_INSTANCES \
                    $DB_CONFIG \
                    $VPC_CONNECTOR \
                    $VPC_EGRESS \
                    --image=$IMAGE_NAME:$VERSION
                ''',
                label: 'Google cloud run deploy'
            )
        }
    }
}
