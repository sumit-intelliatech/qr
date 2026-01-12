pipeline {
    agent any

    environment {
        IMAGE_NAME = "sumitpawar28/qr_service" // <-- TODO: Replace with your Docker repo/name (e.g. 'youruser/qr-service')
        IMAGE_TAG  = "${BUILD_NUMBER}"    // <-- Optional: change tagging strategy (e.g. '${GIT_COMMIT}-${BUILD_NUMBER}' or a fixed tag)
        K8S_NAMESPACE = "qr-project"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                        sh './mvnw -B clean package -DskipTests'
                    } else {
                        bat 'mvnw.cmd -B clean package -DskipTests'
                    }
                }
            }
        }

        stage('Unit Tests') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw -B test'
                    } else {
                        bat 'mvnw.cmd -B test'
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    if (isUnix()) {
                        sh "docker build -t $IMAGE_NAME:$IMAGE_TAG ."
                        sh "docker tag $IMAGE_NAME:$IMAGE_TAG $IMAGE_NAME:latest"
                    } else {
                        bat "docker build -t %IMAGE_NAME%:%IMAGE_TAG% ."
                        bat "docker tag %IMAGE_NAME%:%IMAGE_TAG% %IMAGE_NAME%:latest"
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds', // <-- TODO: Replace with your Jenkins credentials ID for Docker registry
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_PASS'
                    )]) {
                        if (isUnix()) {
                            sh 'echo $DOCKERHUB_PASS | docker login -u $DOCKERHUB_USER --password-stdin'
                            sh "docker push $IMAGE_NAME:$IMAGE_TAG"
                            sh "docker push $IMAGE_NAME:latest"
                            sh 'docker logout'
                        } else {
                            bat 'echo %DOCKERHUB_PASS% | docker login -u %DOCKERHUB_USER% --password-stdin'
                            bat "docker push %IMAGE_NAME%:%IMAGE_TAG%"
                            bat "docker push %IMAGE_NAME%:latest"
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig([credentialsId: 'k8s-cred']) { // <-- TODO: Replace with your Jenkins kubeconfig credentials ID
                    script {
                        if (isUnix()) {
                            sh 'kubectl apply -f k8s/namesapce.yaml'
                            sh "kubectl apply -n $K8S_NAMESPACE -f k8s/mysql-deployment.yaml"
                            sh "kubectl apply -n $K8S_NAMESPACE -f k8s/deployment.yaml"
                            sh "kubectl apply -n $K8S_NAMESPACE -f k8s/service.yaml"

                            sh "kubectl set image -n $K8S_NAMESPACE deployment/qr-deployment qr-service=$IMAGE_NAME:$IMAGE_TAG"
                            sh "kubectl rollout status -n $K8S_NAMESPACE deployment/qr-deployment"
                        } else {
                            bat 'kubectl apply -f k8s\\namesapce.yaml'
                            bat "kubectl apply -n %K8S_NAMESPACE% -f k8s\\mysql-deployment.yaml"
                            bat "kubectl apply -n %K8S_NAMESPACE% -f k8s\\deployment.yaml"
                            bat "kubectl apply -n %K8S_NAMESPACE% -f k8s\\service.yaml"

                            bat "kubectl set image -n %K8S_NAMESPACE% deployment/qr-deployment qr-service=%IMAGE_NAME%:%IMAGE_TAG%"
                            bat "kubectl rollout status -n %K8S_NAMESPACE% deployment/qr-deployment"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ CI/CD completed successfully"
        }
        failure {
            echo "❌ Pipeline failed"
        }
        always {
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
            cleanWs()
        }
    }
}
