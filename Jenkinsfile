pipeline {

    agent any

    parameters {
        string(name: 'ENV_CREDENTIAL_ID', defaultValue: 'portfolio-env-file', description: 'Jenkins Secret File credential ID for .env')
    }
    
    environment {
        GIT_URL = 'https://github.com/Neueda-Learning/11-105-Runtime_Rebels.git'
        BRANCH = 'main'
        DOCKER_COMPOSE_CMD = "docker compose"
    }

    stages {

        stage('Checkout Source') {
            steps {
                git branch: "${BRANCH}",
                    url: "${GIT_URL}"
            }
        }

        stage('Prepare Env File') {
            steps {
                script {
                    try {
                        withCredentials([file(credentialsId: params.ENV_CREDENTIAL_ID, variable: 'ENV_FILE')]) {
                            sh '''
                                cp "$ENV_FILE" .env
                                sed -i 's/\r$//' .env
                                chmod 600 .env
                                test -s .env
                            '''
                        }
                    } catch (Exception e) {
                        error("Unable to load Secret File credential ID '${params.ENV_CREDENTIAL_ID}'. Check credential ID, scope (Folder vs Global), and type (Secret file).")
                    }
                }
            }
        }

        stage('Build Spring Boot') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Stop Existing Containers') {
            steps {
                sh '''
                    ${DOCKER_COMPOSE_CMD} version >/dev/null 2>&1 || DOCKER_COMPOSE_CMD="docker-compose"
                    ${DOCKER_COMPOSE_CMD} --env-file .env down --remove-orphans || true
                    docker rm -f portfolio-manager-mysql portfolio-manager-app portfolio-manager-frontend 2>/dev/null || true
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    ${DOCKER_COMPOSE_CMD} version >/dev/null 2>&1 || DOCKER_COMPOSE_CMD="docker-compose"
                    ${DOCKER_COMPOSE_CMD} --env-file .env build --no-cache
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    ${DOCKER_COMPOSE_CMD} version >/dev/null 2>&1 || DOCKER_COMPOSE_CMD="docker-compose"
                    ${DOCKER_COMPOSE_CMD} --env-file .env up -d --force-recreate --remove-orphans
                '''
            }
        }

    

    }

    post {
        always {
            sh 'rm -f .env || true'
        }
    }
}  

//jenkins need to add 3 containers later
