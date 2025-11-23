pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        TESTCONTAINERS_RYUK_DISABLED = "true"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Récupération du code depuis GitHub...'
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo "Lancement de l'analyse SonarQube..."
                withSonarQubeEnv('sonarqube') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN_CRED')]) {
                        script {
                            if (SONAR_TOKEN_CRED == null || SONAR_TOKEN_CRED.isEmpty()) {
                                error "Le credential 'sonar-token' est vide ou n'a pas pu être chargé."
                            }
                        }
                        sh "mvn sonar:sonar -Dsonar.login=${SONAR_TOKEN_CRED}"
                    }
                }
            }
        }

        stage('Quality Gate Check') {
            steps {
                waitForQualityGate abortPipeline: true
            }
        }
    }

    post {
        always {
            echo 'Fin du pipeline.'
            cleanWs()
        }
    }
}