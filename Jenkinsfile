pipeline {

    agent any
    
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

        stage('Build Spring Boot') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Stop Existing Containers') {
            steps {
                sh '''
                    ${DOCKER_COMPOSE_CMD} version >/dev/null 2>&1 || DOCKER_COMPOSE_CMD="docker-compose"
                    ${DOCKER_COMPOSE_CMD} down --remove-orphans || true
                    docker rm -f portfolio-manager-mysql portfolio-manager-app portfolio-manager-frontend 2>/dev/null || true
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    ${DOCKER_COMPOSE_CMD} version >/dev/null 2>&1 || DOCKER_COMPOSE_CMD="docker-compose"
                    ${DOCKER_COMPOSE_CMD} build --no-cache
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    ${DOCKER_COMPOSE_CMD} version >/dev/null 2>&1 || DOCKER_COMPOSE_CMD="docker-compose"
                    ${DOCKER_COMPOSE_CMD} up -d --force-recreate --remove-orphans
                '''
            }
        }

        stage('Verify') {
            steps {
                sh 'docker ps'
            }
        }
    }
}  

//jenkins need to add 3 containers later