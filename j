pipeline {
    agent any
    tools{
        maven 'maven_3_8_6'
    }
    environment {
        aws_credential = "AWS_Access"
        region = "us-east-1"
        api_res_url = "https://${bucket}.s3.${region}.amazonaws.com/${TAG_NAME}/${api_imagename}-${TAG_NAME}.tar.gz"
        auth_res_url = "https://${bucket}.s3.${region}.amazonaws.com/${TAG_NAME}/${auth_imagename}-${TAG_NAME}.tar.gz"
        notify_text = "image upload to s3 <br>${api_imagename}: <${api_res_url}><br> ${auth_imagename}: <${auth_res_url}><br>tag by ${TAG_NAME}"
    }
    stages{
        stage('checkout') {
            steps {
                script {
                    checkout([$class: 'GitSCM', branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/Dev-Gopi/smtpmailserviceproject.git']]])
                    commitId = sh (script: 'git rev-parse --short HEAD ${GIT_COMMIT}', returnStdout: true).trim()
                }
            }

        }
        stage("Maven build"){
            steps {
                // Run Maven on a Unix agent.
                sh "mvn clean install package shade:shade"
            }
        }
        stage("Upload"){
            steps{
                withAWS(region:"${region}", credentials:"${aws_credential}"){
                    s3Upload(file:'smtpmailserviceproject.jar', bucket:'s3://mys3bucket993261', path:'target/smtpmailserviceproject.jar')
                }
            }
        // post {
        //         success{
        //             office365ConnectorSend message: "${notify_text}<br>commit id: ${commitId}", status:"Success Upload", webhookUrl:"${webHook_url}"
        //         }
        //         failure{
        //             office365ConnectorSend message: "Fail build,<br> see (<${env.BUILD_URL}>)", status:"Fail Upload", webhookUrl:"${webHook_url}"
        //         }
        //     }
        }
    }
}