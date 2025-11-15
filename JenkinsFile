pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment{
        SONAR_HOST_URL = "http://sonarqube:9000"
        SONAR_AUTH_TOKEN = credentials('sonar-token')
    }

    stages {
        stage ('Checkout'){
            step {
                echo 'Récupération du code depuis Github...'
                checkout scm
            }
        }

        stage ('Build & Test'){
            steps {
                echo 'Lancement de mvn clean verify...'

                script {
                    if (isUnix){
                        sh 'mvn clean verify'
                    } else {
                        bat 'mvn clean verify'
                    }
                }
            }
        }

        stage ('SonarQube Analysis') {
            steps {
                echo 'Lancement de l\'analyse SonarQube...'
                withSonarQubeEnv('sonarqube'){
                    script {
                        if (isUnix){
                            sh 'mvn clean verify'
                        } else {
                            bat 'mvn clean verify'
                        }
                    }
                }
            }
        }

        stage ('Quality Gate Check'){
            step{
                echo 'Vérification du statut du quality Gate...'

                timeout(time : 5, unit: 'MINUTES'){
                    waitForQualityGate  abortPipeline: true
                }
            }
        }
    }
}