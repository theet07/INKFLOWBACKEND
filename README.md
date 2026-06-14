# InkFlow — Backend

API REST da plataforma **InkFlow**, um sistema completo para estúdios de tatuagem. Responsável por autenticação, agendamentos, portfólio, cicatrização e comunicação entre artistas e clientes.

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.6 |
| Spring Security | — |
| Spring Data JPA | — |
| SQL Server | (produção) |
| PostgreSQL | (suporte alternativo) |
| JWT (jjwt) | 0.11.5 |
| Cloudinary | 1.36.0 |
| Bucket4j (Rate Limiting) | 8.10.1 |
| Caffeine Cache | — |
| Brevo (e-mail) | API REST |
| Groq AI (chatbot) | API REST |
| Docker | — |
| Maven | — |

---

## Arquitetura

```
src/main/java/com/backend/INKFLOW/
├── config/
│   ├── initializer/        # DataMigrationRunner, SuspectLogTableInit
│   ├── AsyncConfig.java
│   └── DatabaseConfig.java
├── controller/             # 25 controllers REST
├── dto/                    # 12 DTOs de request/response
├── exception/              # GlobalExceptionHandler
├── model/                  # 21 entidades JPA
├── repository/             # 21 repositories Spring Data
├── security/               # JWT, filtros de rate limiting
└── service/                # 17 services de negócio
```

---

## Módulos da API

| Módulo | Controller | Descrição |
|---|---|---|
| Autenticação | `AuthController` | Login JWT para cliente, artista e admin |
| Clientes | `ClienteController` | CRUD, foto de perfil, verificação OTP |
| Artistas | `ArtistaController` | CRUD, aprovação, disponibilidade, bio |
| Agendamentos | `AgendamentoController` | Criação, status, avaliação |
| Appointments | `AppointmentController` | Fluxo de agendamento do cliente |
| Portfólio | `PortfolioController` | Upload e gerenciamento de imagens |
| Disponibilidade | `DisponibilidadeController` | Slots de agenda por artista |
| Mensagens | `MensagemController` | Chat entre artista e cliente |
| Notificações | `NotificacaoController` | Preferências de notificação |
| Cicatrização | `CicatrizacaoController` | Plano de cuidados pós-tatuagem |
| Fotos de Evolução | `FotoEvolucaoController` | Registro fotográfico da cicatrização |
| Quiz | `QuizController` | Quiz diário de acompanhamento |
| Badges | `BadgeController` | Sistema de conquistas do cliente |
| Dicas | `DicaController` | Dicas de cuidado por dia |
| Chat IA | `ChatController` | Chatbot via Groq AI |
| Admin | `AdminController` | Painel administrativo completo |
| Upload | `UploadController` | Upload para Cloudinary |
| Backup | `BackupController` | Backup do banco de dados |
| Contato | `ContatoController` | Formulário de contato da landing page |
| Leads | `LeadController` | Captação de leads de artistas |
| Landing Page | `LandingPageController` | Dados públicos da landing |
| Estatísticas | `EstatisticasController` | Métricas do sistema |
| Health | `HealthController` | Health check da aplicação |
| Diagnóstico | `DiagnosticController` | Diagnóstico interno |

---

## Segurança

- **JWT** com expiração de 24h (`86400000ms`) e blacklist de tokens via banco
- **Rate Limiting** com Bucket4j:
  - Login: limite por IP para evitar brute force
  - Contato: limite por IP
  - Agendamentos: limite por usuário
- **IP Blacklist** com `IpBlacklistService` e log de suspeitos (`SuspectLog`)
- **BCrypt** para hash de senhas
- Senhas e segredos nunca expostos — configurados via variáveis de ambiente

---

## Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
DB_URL=jdbc:sqlserver://<host>:1433;databaseName=<db>;integratedSecurity=false;trustServerCertificate=true;encrypt=true
DB_USERNAME=<usuario>
DB_PASSWORD=<senha>

JWT_SECRET=<chave-secreta-longa>

CLOUDINARY_CLOUD_NAME=<cloud_name>
CLOUDINARY_API_KEY=<api_key>
CLOUDINARY_API_SECRET=<api_secret>

BREVO_API_KEY=<chave_brevo>
BREVO_SENDER_EMAIL=<email_remetente>

GROQ_API_KEY=<chave_groq>

ADMIN_EMAIL=<email_admin>
ADMIN_PASSWORD_HASH=<bcrypt_hash>

DEFAULT_CLIENT_PASS=<senha_padrao_cliente>
```

---

## Como Executar

### Localmente

```bash
# Clonar o repositório
git clone <url-do-repo>
cd INKFLOWBACKEND

# Configurar o .env com as variáveis acima

# Executar
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### Com Docker

```bash
docker build -t inkflow-backend .
docker run -p 8080:8080 --env-file .env inkflow-backend
```

---

## Banco de Dados

- **DDL:** `spring.jpa.hibernate.ddl-auto=none` — o schema é gerenciado manualmente
- **Scripts de migração** disponíveis em `/migration`:
  - `002_schema_completo_sqlserver.sql` — schema completo
  - `migration-admins.sql`, `migration-portfolio.sql`, `migration-otp-verificacao.sql`, etc.
- **Connection Pool (HikariCP):** máximo 15 conexões, mínimo 5 idle

---

## Cache

Caffeine Cache configurado com:
- Tamanho máximo: 500 entradas
- Expiração: 1 hora após escrita

---

## Upload de Imagens

Upload feito via **Cloudinary**. Tipos aceitos e limite de tamanho configurados em:

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

Validação de tipo de arquivo realizada pelo `FileValidationService`.

---

## E-mail

Integração com **Brevo (SendinBlue)** via API REST através do `BrevoService` e `EmailAsyncService`. E-mails são enviados de forma assíncrona usando `@Async`.

---

## Deploy

A aplicação está hospedada no **Render**:

```
https://inkflowbackend-4w1g.onrender.com
```

---

## Estrutura de Pacotes — Resumo

```
model/          Admin, Artista, Cliente, Agendamento, Cicatrizacao,
                Badge, BadgeUsuario, ChecklistItem, CheckpointDia,
                Dica, DisponibilidadeArtista, FotoEvolucao,
                LeadArtista, Mensagem, NotificacaoPreferencia,
                PortfolioItem, QuizOpcao, QuizPergunta,
                SuspectLog, TokenBlacklist

security/       JwtUtil, JwtFilter, SecurityConfig,
                LoginRateLimitFilter, AgendamentoRateLimitFilter,
                ContatoRateLimitFilter

service/        AdminService, AgendamentoService, ArtistaService,
                BackupService, BrevoService, ChatRateLimitService,
                CicatrizacaoService, ClienteService,
                DatabaseCleanupTask, DisponibilidadeService,
                EmailAsyncService, EmailService,
                FileValidationService, FotoService,
                IpBlacklistService, PortfolioService,
                StatusDiasScheduler
```

---

## Licença

Distribuído sob a licença presente no arquivo [LICENSE](./LICENSE).
