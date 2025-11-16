pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        SONAR_HOST_URL = "http://sonarqube:9000"
        SONAR_AUTH_TOKEN = credentials('sonar-token')
        TESTCONTAINERS_RYUK_DISABLED  = "true"
        TESTCONTAINERS_HOST_OVERRIDE = 'localhost'
    }

    stages {
        stage('Checkout') {
            // CORRECTION: 'step' a été remplacé par 'steps'
            steps {
                echo 'Récupération du code depuis GitHub...'
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo 'Lancement de mvn clean verify...'
                script {
                    // isUnix() est une fonction, donc les parenthèses sont importantes
                    if (isUnix()) {
                        sh 'mvn clean verify'
                    } else {
                        bat 'mvn clean verify'
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo 'Lancement de l\'analyse SonarQube...'
                withSonarQubeEnv('sonarqube') {
                    script {
                        if (isUnix()) {
                            sh 'mvn sonar:sonar'
                        } else {
                            bat 'mvn sonar:sonar'
                        }
                    }
                }
            }
        }

        stage('Quality Gate Check') {
            steps {
                echo 'Vérification du statut du Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        always {
            echo 'Fin du pipeline.'
        }
    }
}