pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        SONAR_HOST_URL = "http://sonarqube:9000"
        SONAR_AUTH_TOKEN = credentials('sonar-token')
        TESTCONTAINERS_RYUK_DISABLED = "true"
        SONAR_QUBE_CONFIG = 'sonarqube'
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
                script {
                    if (fileExists("$HOME/.testcontainers.properties")) {
                        echo "Nettoyage de l'ancienne configuration Testcontainers..."
                        sh "rm -f $HOME/.testcontainers.properties"
                    } else {
                        echo "Aucun fichier de configuration Testcontainers à nettoyer."
                    }
                }
                    echo 'Lancement de mvn clean verify...'
                    sh 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo "Lancement de l'analyse SonarQube..."
                withSonarQubeEnv(SONAR_QUBE_CONFIG) {
                sh 'mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN'
                }
            }
        }

        stage('Quality Gate Check') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
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