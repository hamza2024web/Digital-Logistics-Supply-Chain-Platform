pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        SONAR_HOST_URL = "http://sonarqube:9000"
        SONAR_AUTH_TOKEN = credentials('sonar-token')
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
                script {
                    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                        echo 'Lancement de l\'analyse SonarQube...'
                        withSonarQubeEnv('sonarqube') {
                            sh 'mvn sonar:sonar'
                        }
                    } else {
                        echo 'Tests échoués, on saute l\'analyse SonarQube.'
                    }
                }
            }
        }

        stage('Quality Gate Check') {
            steps {
                script {
                    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                        echo 'Vérification du statut du Quality Gate...'
                        timeout(time: 5, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true
                        }
                    } else {
                        echo 'Tests échoués, on saute le Quality Gate.'
                    }
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