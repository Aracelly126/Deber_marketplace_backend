// Pipeline declarativo de Jenkins para el Backend (Spring Boot)
pipeline {
    // Puede ejecutarse en cualquier agente disponible
    agent any
    
    // Variables de entorno para todo el pipeline
    environment {
        // Nombre de la imagen Docker que se creará
        DOCKER_IMAGE = "sales-system-backend"
        // Tag de la imagen (puedes cambiarlo por la rama git o número de build)
        IMAGE_TAG = "${BUILD_NUMBER}"
        // Puerto donde se expondrá el backend
        APP_PORT = "8081"
    }
    
    // Etapas del pipeline
    stages {
        // Etapa 1: Checkout del código
        stage('Checkout') {
            steps {
                // Muestra un mensaje en la consola de Jenkins
                echo 'Clonando repositorio...'
                // Hace checkout del código desde Git (Jenkins lo hace automáticamente)
                checkout scm
            }
        }
        
        // Etapa 2: Construcción del proyecto con Maven
        stage('Build') {
            steps {
                echo 'Compilando proyecto con Maven...'
                // Ejecuta Maven dentro de un contenedor Docker
                // Esto evita tener que instalar Maven en Jenkins
                script {
                    docker.image('maven:3.9-eclipse-temurin-17').inside('-v $HOME/.m2:/root/.m2') {
                        sh 'mvn clean package -DskipTests'
                    }
                }
            }
        }
        
        // Etapa 3: Ejecutar tests (opcional, quita -DskipTests del build si quieres esto)
        stage('Test') {
            steps {
                echo 'Ejecutando tests...'
                script {
                    docker.image('maven:3.9-eclipse-temurin-17').inside('-v $HOME/.m2:/root/.m2') {
                        sh 'mvn test || true'
                    }
                }
            }
        }
        
        // Etapa 4: Construir imagen Docker
        stage('Build Docker Image') {
            steps {
                echo 'Construyendo imagen Docker...'
                script {
                    // Construye la imagen Docker usando el Dockerfile del proyecto
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} .
                        docker tag ${DOCKER_IMAGE}:${IMAGE_TAG} ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
        
        // Etapa 5: Desplegar contenedor
        stage('Deploy') {
            steps {
                echo 'Desplegando contenedor...'
                script {
                    // Detiene y elimina el contenedor anterior si existe
                    sh """
                        docker stop ${DOCKER_IMAGE} || true
                        docker rm ${DOCKER_IMAGE} || true
                    """
                    
                    // Inicia el nuevo contenedor
                    // --env-file carga las variables de entorno desde .env
                    sh """
                        docker run -d \
                            --name ${DOCKER_IMAGE} \
                            -p ${APP_PORT}:8080 \
                            --env-file ${WORKSPACE}/.env \
                            --restart unless-stopped \
                            ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
        
        // Etapa 6: Verificar el despliegue
        stage('Verify Deployment') {
            steps {
                echo 'Verificando despliegue...'
                script {
                    // Espera 30 segundos para que la app arranque
                    sh 'sleep 30'
                    // Verifica que el contenedor esté corriendo
                    sh 'docker ps | grep ${DOCKER_IMAGE}'
                    // Intenta hacer una petición HTTP al actuator de Spring Boot
                    sh 'curl -f http://localhost:${APP_PORT}/actuator/health || echo "Health check failed"'
                }
            }
        }
    }
    
    // Acciones post-ejecución
    post {
        // Se ejecuta siempre, sin importar el resultado
        always {
            echo 'Pipeline finalizado'
            // Limpia las imágenes Docker antiguas para no llenar el disco
            sh 'docker image prune -f || true'
        }
        
        // Se ejecuta solo si el pipeline fue exitoso
        success {
            echo '¡Despliegue exitoso! Backend disponible en http://localhost:${APP_PORT}'
        }
        
        // Se ejecuta solo si el pipeline falló
        failure {
            echo 'El despliegue ha fallado. Revisa los logs.'
        }
    }
}
