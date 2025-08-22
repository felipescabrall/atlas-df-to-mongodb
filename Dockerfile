# Use OpenJDK 17 como imagem base
FROM openjdk:17-jdk-slim

# Definir diretório de trabalho
WORKDIR /app

# Instalar Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copiar arquivos de configuração do Maven
COPY pom.xml .

# Baixar dependências (cache layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Compilar a aplicação
RUN mvn clean package -DskipTests

# Criar diretório para logs
RUN mkdir -p /app/logs

# Expor porta da aplicação
EXPOSE 8080

# Comando para executar a aplicação
CMD ["java", "-jar", "target/atlas-df-to-mongodb-0.0.1-SNAPSHOT.jar"]