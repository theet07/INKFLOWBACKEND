$env:JAVA_HOME = "C:\Program Files\RedHat\java-17-openjdk-17.0.18.0.8-1"
$env:DB_URL = "jdbc:sqlserver://INKFLOW.mssql.somee.com:1433;databaseName=INKFLOW;integratedSecurity=false;trustServerCertificate=true;encrypt=true"
$env:DB_USERNAME = "mzx1212_SQLLogin_1"
$env:DB_PASSWORD = "Matheus&NathanS2"
$env:ADMIN_EMAIL = "admin@inkflow.com"
$env:ADMIN_PASSWORD_HASH = '$2a$12$MsLQKFW8hedRAHKbR/xDaeI/f4KGeNUFmbJeuwZzC10awmrdhY6Ry'
$env:JWT_SECRET = "inkflow-tcc-jwt-secret-2025-seguranca-backend-chave"

.\mvnw.cmd spring-boot:run
