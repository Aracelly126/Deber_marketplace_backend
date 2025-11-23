# Etapa 1: Construcción
# Utilizamos una imagen con Maven y JDK 17 para compilar el proyecto
FROM maven:3.9-eclipse-temurin-17 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el archivo pom.xml primero (para aprovechar el cache de Docker)
# Si solo cambian los archivos de código, no se volverá a descargar las dependencias
COPY pom.xml .

# Descargamos las dependencias del proyecto
RUN mvn dependency:go-offline -B

# Copiamos el código fuente completo
COPY src ./src

# Construimos el proyecto (compilamos y empaquetamos en un JAR)
# -DskipTests omite los tests para acelerar el build (puedes quitarlo si quieres ejecutar tests)
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
# Utilizamos una imagen más ligera solo con el JRE para ejecutar la aplicación
FROM eclipse-temurin:17-jre

# Directorio de trabajo para la aplicación
WORKDIR /app

# Copiamos el JAR generado desde la etapa de build
# El archivo se llama sales-system-0.0.1-SNAPSHOT.jar según tu pom.xml
COPY --from=build /app/target/sales-system-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto 8080 (puerto por defecto de Spring Boot)
EXPOSE 8080

# Comando para ejecutar la aplicación
# -Djava.security.egd=file:/dev/./urandom acelera el inicio de la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
