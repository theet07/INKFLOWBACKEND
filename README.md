# 🎨 InkFlow — Backend API

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=flat-square&logo=springboot)
![SQL Server](https://img.shields.io/badge/SQL%20Server-produção-blue?style=flat-square&logo=microsoftsqlserver)
![JWT](https://img.shields.io/badge/JWT-Auth-black?style=flat-square&logo=jsonwebtokens)
![Docker](https://img.shields.io/badge/Docker-deploy-2496ED?style=flat-square&logo=docker)
![Render](https://img.shields.io/badge/Render-hospedado-46E3B7?style=flat-square&logo=render)

O **InkFlow** é uma plataforma completa para estúdios de tatuagem que conecta clientes e artistas em um único ecossistema digital. O backend centraliza toda a lógica de negócio da plataforma — da autenticação segura ao acompanhamento pós-tatuagem — resolvendo problemas históricos do setor, como gestão manual de agendas, falta de comunicação entre artista e cliente e ausência de acompanhamento do processo de cicatrização.

---

## 🌟 Funcionalidades e Recursos do Sistema

### 🔐 Autenticação e Segurança Multicamada
Suporte a três perfis de acesso distintos: **Cliente**, **Artista** e **Administrador**, cada um com permissões e experiências personalizadas. Tokens JWT com expiração de 24h e blacklist ativa no banco de dados garantem que sessões invalidadas não possam ser reutilizadas.

O sistema conta com proteção contra força bruta via **Rate Limiting** por IP (Bucket4j) nos endpoints de login, contato e agendamento. IPs suspeitos são registrados automaticamente no `SuspectLog` e podem ser bloqueados via `IpBlacklistService`. Senhas protegidas com **BCrypt** e segredos configurados exclusivamente via variáveis de ambiente.

### 📅 Gestão Completa de Agendamentos
Fluxo de agendamento com controle de status, avaliação pós-sessão e histórico completo por cliente e por artista. Controle de disponibilidade por slots configuráveis, com separação entre o fluxo administrativo (`AgendamentoController`) e o fluxo do cliente (`AppointmentController`).

### 🖼️ Portfólio e Upload de Imagens
Upload de imagens do portfólio diretamente para o **Cloudinary**, com validação de tipo e tamanho de arquivo pelo `FileValidationService`. Limite configurado em **10MB** por arquivo.

### 💬 Chat entre Artista e Cliente
Sistema de mensagens em tempo real entre artistas e clientes com suporte a conversas individuais, contagem de mensagens não lidas e marcação de leitura por remetente.

### 🤖 Chatbot com Inteligência Artificial
Integração com a API do **Groq AI** para responder dúvidas dos clientes sobre tatuagens, estilos e cuidados diretamente pelo chat da plataforma. Controle de uso com `ChatRateLimitService` para evitar abusos.

### 🩹 Módulo de Cicatrização Pós-Tatuagem
Plano de cuidados gerado automaticamente após a realização de uma tatuagem. O cliente acompanha o progresso dia a dia via checklist, responde quizzes diários e registra fotos de evolução. O scheduler `StatusDiasScheduler` atualiza automaticamente o status dos dias conforme o tempo avança.

### 🏆 Sistema de Badges e Gamificação
Conquistas desbloqueadas conforme o cliente cumpre marcos no cuidado com a tatuagem, incentivando a adesão ao plano de cicatrização.

### 📧 E-mail Assíncrono via Brevo
Envio de e-mails transacionais (verificação OTP, confirmações, recuperação de senha) de forma assíncrona via `@Async`, utilizando a API HTTP do **Brevo (SendinBlue)** pelo `BrevoService`.

### 🛡️ Painel Administrativo Completo
Gestão centralizada de usuários, agendamentos, artistas e clientes. Aprovação de requisições de novos artistas, geração de backup do banco de dados e acesso a estatísticas e métricas do sistema.

### 🔒 Verificação de Conta via OTP
Clientes verificam o e-mail no cadastro via código OTP de uso único com expiração controlada, garantindo a autenticidade das contas criadas.

---

## 🛠️ Tecnologias Utilizadas

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.5.6 |
| Segurança | Spring Security, JWT (jjwt 0.11.5), BCrypt |
| Banco de Dados | SQL Server (produção), PostgreSQL (suporte alternativo) |
| ORM | Spring Data JPA / Hibernate |
| Rate Limiting | Bucket4j 8.10.1 |
| Cache | Caffeine Cache (500 entradas, 1h TTL) |
| Connection Pool | HikariCP (15 conexões máx.) |
| Upload | Cloudinary 1.36.0 |
| E-mail | Brevo API HTTP |
| Inteligência Artificial | Groq AI API |
| Build | Maven |
| Deploy | Docker + Render |

---

## 🏗️ Arquitetura do Sistema

```
src/main/java/com/backend/INKFLOW/
├── config/
│   ├── initializer/        # DataMigrationRunner, SuspectLogTableInit
│   ├── AsyncConfig.java    # Configuração de threads assíncronas
│   └── DatabaseConfig.java # Dialect dinâmico SQL Server / PostgreSQL
├── controller/             # 25 controllers REST
├── dto/                    # 12 DTOs de request/response
├── exception/              # GlobalExceptionHandler
├── model/                  # 21 entidades JPA
├── repository/             # 21 repositories Spring Data
├── security/               # JWT, filtros de rate limiting
└── service/                # 17 services de negócio
```

---

## 📡 Módulos da API

| Módulo | Controller | Descrição |
|---|---|---|
| Autenticação | `AuthController` | Login JWT para cliente, artista e admin |
| Clientes | `ClienteController` | CRUD, foto de perfil, verificação OTP, troca de senha |
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

## 🌐 Deploy

A API está hospedada no **Render**:

```
https://inkflowbackend-4w1g.onrender.com
```

---

## 👤 Autores

**Matheus** & **Nathan**

---

## 📄 Licença

Copyright © 2025 InkFlow. Todos os direitos reservados.

Este software e seu código-fonte são propriedade exclusiva dos autores. É proibida a reprodução, distribuição, modificação ou uso comercial, total ou parcial, sem autorização expressa por escrito dos autores.
