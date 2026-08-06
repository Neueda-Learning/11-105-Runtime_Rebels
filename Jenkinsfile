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

                stage('Seed Mock Data') {
                        steps {
                                sh '''
                                        set -e

                                        if command -v python3 >/dev/null 2>&1; then
                                            PYTHON_BIN="python3"
                                        elif command -v python >/dev/null 2>&1; then
                                            PYTHON_BIN="python"
                                        else
                                            echo "Python is not available on Jenkins agent."
                                            exit 1
                                        fi

                                        set -a
                                        . ./.env
                                        set +a

                                        for i in $(seq 1 30); do
                                            if docker exec portfolio-manager-mysql mysql -uroot -p"$DB_ROOT_PASSWORD" -D "$DB_NAME" -e "SELECT 1" >/dev/null 2>&1; then
                                                break
                                            fi
                                            if [ "$i" -eq 30 ]; then
                                                echo "MySQL did not become ready in time."
                                                exit 1
                                            fi
                                            sleep 5
                                        done

                                        "$PYTHON_BIN" -m pip install -r scripts/requirements.txt
                                        "$PYTHON_BIN" scripts/seed_mock_data.py \
                                            --host localhost \
                                            --port "${DB_PORT:-3306}" \
                                            --database "$DB_NAME" \
                                            --user "$DB_USERNAME" \
                                            --password "$DB_PASSWORD"
                                '''
                        }
                }

        stage('Verify') {
            steps {
                sh '''
                    set -e
                    set -a
                    . ./.env
                    set +a

                    docker ps
                    docker exec portfolio-manager-mysql mysql -uroot -p"$DB_ROOT_PASSWORD" -D "$DB_NAME" -e "SELECT installed_rank,version,description,script,success FROM flyway_schema_history ORDER BY installed_rank;"
                    docker exec portfolio-manager-mysql mysql -uroot -p"$DB_ROOT_PASSWORD" -D "$DB_NAME" -e "SELECT COUNT(*) AS investments_count FROM investments; SELECT COUNT(*) AS transactions_count FROM transactions;"
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
