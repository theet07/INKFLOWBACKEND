# InkFlow — Documentação Técnica do Sistema

## Visão Geral
InkFlow é um sistema completo de agendamento de tatuagens com React + Vite (frontend) e Spring Boot (backend). O sistema permite que clientes agendem sessões com artistas, gerenciem seus perfis, troquem mensagens e acompanhem o status de seus agendamentos.

## Regras de Comportamento para IA
1. Antes de qualquer alteração: descreva o arquivo, linha exata e o que será modificado
2. Use apenas fsReplace com trechos exatos de código
3. Nunca use PowerShell, scripts .ps1 ou manipulação por índice de caractere
4. Se fsReplace falhar, oriente edição manual no VS Code
5. Sempre teste alterações críticas antes de commitar

## Estrutura de Repositórios

### Diretórios Locais
- **Frontend**: `c:\Users\DMJ\OneDrive\Documentos\INKFLOWFRONTEND-LIMPO`
- **Backend**: `c:\Users\DMJ\OneDrive\Documentos\INKFLOWBACKEND`

### Git Configuration
- **Branch ativa**: `teste` (frontend e backend)
- **Frontend remotes**: 
  - `origin` → theet07/INKFLOWFRONTEND
  - `netelinriquen` → netelinriquen/INKFLOWFRONTEND
- **Backend remote**: `origin` → theet07/INKFLOWBACKEND

### Padrão de Commits
- **Formato**: `<tipo>: <mensagem em português>`
- **Tipos**: `feat`, `fix`, `refactor`, `style`, `chore`, `docs`
- **Exemplo**: `feat: adiciona validação de senha forte no cadastro`
- **Ordem de commit**: Backend primeiro, depois frontend (quando ambos são afetados)

### Comandos Git
```bash
# Frontend (push para ambos os remotes)
git push origin teste && git push netelinriquen teste

# Backend (push para origin)
git push origin teste
```

## Stack Tecnológica

### Frontend
- **Framework**: React 18 + Vite
- **Roteamento**: React Router v6
- **Estilização**: CSS Modules + Tailwind CDN
- **HTTP Client**: Axios
- **Deploy**: Vercel
- **Autenticação**: JWT armazenado em localStorage

### Backend
- **Framework**: Spring Boot 3.x
- **Linguagem**: Java 17+
- **Segurança**: Spring Security + JWT + BCrypt (strength 12)
- **Validação**: Jakarta Validation
- **Email**: Spring Boot Mail (Gmail SMTP)
- **Deploy**: Render
- **Rate Limiting**: Bucket4j

### Infraestrutura
- **Banco de Dados**: SQL Server (somee.com)
- **Storage**: Cloudinary (imagens)
- **IA**: Groq API (llama-3.3-70b-versatile)
- **Auth**: JWT com blacklist em memória

## Funcionalidades Implementadas

### Autenticação e Autorização
- JWT com blacklist em memória
- BCrypt com strength 12
- Rate limiting em login (5 tentativas/15min por IP)
- Proteção de rotas por userType (CLIENT, ARTIST, ADMIN)
- Validação de senha forte (8+ chars, maiúscula, número, especial)

### Sistema de Agendamento
- Agendamento para clientes logados e não logados
- Validação anti double-booking
- Whitelist de status válidos (PENDENTE, CONFIRMADO, CANCELADO, REALIZADO)
- Upload de imagem de referência via Cloudinary
- Campos opcionais: região, tamanho (largura/altura), tags
- Disponibilidade semanal configurável por artista
- Navegação de calendário com bloqueio de meses passados

### Dashboard do Artista
- **Tabs**: Dashboard, Solicitações, Agenda, Mensagens, Portfólio, Configurações
- Gerenciamento de agendamentos (aprovar, recusar, cancelar)
- Configuração de disponibilidade semanal
- Upload de trabalhos para portfólio
- Edição de perfil (bio, especialidades, foto)

### Dashboard do Admin
- Gerenciamento de usuários (clientes, artistas, admins)
- Backup automático do banco via email
- Visualização de todos os agendamentos

### Sistema de Mensagens
- Chat cliente ↔ artista
- Polling de 10s para novas mensagens
- Notificação sonora configurável (beep 880Hz)
- Validação de ownership (apenas usuários com agendamento podem conversar)
- Marcação de mensagens como lidas

### Sistema de Notificações
- Badge no sino para agendamentos novos
- Badge para mensagens não lidas
- Preferências salvas em localStorage (sino, mensagens, som)
- Polling de 10s (cliente e artista)

### Chatbot IA
- Integração com Groq API (llama-3.3-70b-versatile)
- System prompt: InkFlow opera em "todo o Brasil"
- Rate limiting (10 mensagens/10min por IP)

### Sistema de Contato
- Formulário público enviando emails via Spring Boot Mail
- Rate limiting (3 envios/10min por IP)
- Validação de campos (nome, email, telefone, mensagem)
- Formatação automática de telefone brasileiro

### Landing Page para Artistas
- Captura de leads com validação
- Prevenção de duplicatas por WhatsApp
- Email de confirmação automático
- Rate limiting (3 submissões/10min por IP)

### Perfil Público do Artista
- Visualização de bio, especialidades, portfólio
- Botão de agendamento direto
- Galeria de trabalhos

### Upload de Imagens
- Cloudinary para storage
- Validação de magic bytes (segurança)
- Suporte: PNG, JPG, WEBP
- Aceito por: CLIENTE (referências), ARTISTA (portfólio, foto perfil), ADMIN

## Arquitetura do Sistema

### Frontend (React + Vite)
```
src/
├── components/          # Componentes reutilizáveis
│   ├── Header.jsx      # Header com notificações
│   ├── Chatbot.jsx     # Chatbot com Groq
│   └── ProtectedRoute.jsx
├── contexts/
│   └── AuthContext.jsx # Gerenciamento de autenticação
├── pages/              # Páginas principais
│   ├── Home.jsx
│   ├── Login.jsx
│   ├── Booking.jsx     # Sistema de agendamento
│   ├── Profile.jsx     # Perfil do cliente
│   ├── ArtistDashboard.jsx
│   ├── AdminDashboard.jsx
│   ├── Artists.jsx
│   ├── ArtistProfile.jsx
│   ├── Portfolio.jsx
│   ├── Contact.jsx
│   └── ArtistLandingPage/
├── services/
│   └── inkflowApi.js   # Centralização de APIs
└── utils/
    └── formatPhone.js  # Utilitários
```

### Backend (Spring Boot)
```
com.backend.INKFLOW/
├── config/
│   ├── SecurityConfig.java
│   ├── CloudinaryConfig.java
│   └── JwtAuthenticationFilter.java
├── controller/
│   ├── AuthController.java
│   ├── AgendamentoController.java
│   ├── MensagemController.java
│   ├── ArtistaController.java
│   ├── ClienteController.java
│   ├── ContatoController.java
│   ├── LeadController.java
│   ├── ChatController.java
│   └── UploadController.java
├── model/
│   ├── Cliente.java
│   ├── Artista.java
│   ├── Agendamento.java
│   ├── Mensagem.java
│   ├── Disponibilidade.java
│   └── Lead.java
├── repository/
│   └── [JpaRepository interfaces]
├── service/
│   ├── AgendamentoService.java
│   ├── EmailService.java
│   ├── UploadService.java
│   ├── ChatService.java
│   └── BackupService.java
├── dto/
│   ├── AgendamentoUpdateRequest.java
│   ├── ContatoRequest.java
│   └── [outros DTOs]
└── filter/
    ├── LoginRateLimitFilter.java
    ├── AgendamentoRateLimitFilter.java
    └── ContatoRateLimitFilter.java
```

---

## Autenticação e Contextos
- **AuthContext**: gerencia `user`, `token`, `userType`, `loading`
- **Token**: armazenado em `localStorage` como `'token'`
- **User**: armazenado em `localStorage` como `'user'` (JSON)
- **UserType**: armazenado em `localStorage` como `'userType'` (`'client'`, `'artist'`, `'admin'`)
- **Token JWT**: `payload.role` pode ser `ROLE_CLIENTE`, `ROLE_ARTISTA`, `ROLE_ADMIN`
- **Mapeamento**: `ROLE_CLIENTE` → `'client'`, `ROLE_ARTISTA` → `'artist'`, `ROLE_ADMIN` → `'admin'`

---

## Estrutura de Dados Importantes

### User Object (localStorage)
```json
{
  "id": 1,
  "artistaId": 1,
  "clienteId": 1,
  "nome": "Nome do Usuário",
  "fullName": "Nome Completo",
  "email": "email@example.com",
  "bio": "Biografia do artista",
  "fotoUrl": "https://cloudinary.com/...",
  "especialidades": "Realismo,Blackwork,Aquarela",
  "aceitandoAgendamentos": true
}
```

### Mensagem Object
```json
{
  "id": 1,
  "conteudo": "Texto da mensagem",
  "createdAt": "2024-01-15T10:30:00",
  "remetenteId": 1,
  "remetenteNome": "Nome do Remetente",
  "destinatarioId": 2,
  "lida": false
}
```

### Agendamento Object
```json
{
  "id": 1,
  "status": "AGENDADO",
  "dataHora": "2024-01-20T14:00:00",
  "servico": "Tatuagem",
  "cliente": { "id": 1, "nome": "Cliente", "fullName": "Cliente Completo", "email": "cliente@example.com" },
  "artista": { "id": 1, "nome": "Artista" },
  "imagemReferenciaUrl": "https://cloudinary.com/...",
  "descricao": "Descrição do pedido",
  "observacoes": "Observações adicionais",
  "largura": 10,
  "altura": 15,
  "regiao": "Braço",
  "tags": "Realismo,Preto e Cinza",
  "createdAt": "2024-01-15T10:00:00"
}
```

### Disponibilidade Object
```json
{
  "id": 1,
  "diaSemana": 0,
  "horaInicio": "10:00",
  "horaFim": "18:00",
  "duracaoSlotMinutos": 60,
  "ativo": true
}
```
**Nota**: `diaSemana` → 0=Seg, 1=Ter, 2=Qua, 3=Qui, 4=Sex, 5=Sáb, 6=Dom

---

## Endpoints Principais

### Mensagens
- `GET /api/mensagens/nao-lidas` → `Array<Mensagem>` (requer token)
- `PATCH /api/mensagens/marcar-todas-lidas` → void (requer token)
- `GET /api/mensagens/conversa/{artistaId}/{clienteId}` → `Array<Mensagem>`
- `POST /api/mensagens` → `{ remetenteId, destinatarioId, conteudo }`

### Agendamentos
- `GET /api/agendamentos/cliente/{id}` → `Array<Agendamento>`
- `GET /api/agendamentos/artista/{id}` → `Array<Agendamento>`
- `PATCH /api/agendamentos/{id}/status` → `{ status: "CONFIRMADO" | "CANCELADO" | "REALIZADO" }`
- `POST /api/agendamentos` → Agendamento completo

### Artistas
- `GET /api/artistas` → `Array<Artista>`
- `GET /api/artistas/{id}` → `Artista`
- `PUT /api/artistas/{id}` → `{ nome, bio, especialidades, aceitandoAgendamentos }`
- `POST /api/artistas/{id}/foto` → `FormData` (multipart)

### Disponibilidade
- `GET /api/disponibilidades/artista/{id}` → `Array<Disponibilidade>`
- `POST /api/disponibilidades/artista/{id}` → `{ diaSemana, horaInicio, horaFim, duracaoSlotMinutos }`
- `DELETE /api/disponibilidades/{id}` → void

### Chatbot
- `POST /api/chat` → `{ message }` (system prompt: InkFlow opera em "todo o Brasil")

### Contato
- `POST /api/contato` → `{ nome, email, telefone, mensagem }` (público, sem autenticação)
  - Envia e-mail para `inkflowstudios07@gmail.com`
  - Validação: nome, email e mensagem obrigatórios
  - Limite: mensagem até 2000 caracteres
  - Subject: "Novo contato via InkFlow — {nome}"
  - ReplyTo: email do remetente

### Leads de Artistas
- `POST /api/leads/artista` → `{ nomeCompleto, nomeEstudio, email, whatsapp, especialidade }` (público)
  - Validações: nome min 3 chars, email válido, WhatsApp 11 dígitos
  - Prevenção de duplicatas por WhatsApp
  - Envia email de confirmação para o artista
  - Envia notificação para `inkflowstudios07@gmail.com` com link WhatsApp
  - Salva no banco com status "PENDENTE"

---

## Sistema de Notificações

### LocalStorage Keys
- `notif_sino_ativo`: `'true'` | `'false'` (padrão: `'true'`) - Badge de agendamentos
- `notif_msg_ativo`: `'true'` | `'false'` (padrão: `'true'`) - Badge de mensagens
- `notif_som_ativo`: `'true'` | `'false'` (padrão: `'false'`) - Som de beep
- `notif_cliente_lastSeen`: ISO timestamp - Última vez que cliente viu notificações
- `notif_artista_lastSeen`: ISO timestamp - Última vez que artista viu notificações

### Polling de Notificações
- **Intervalo**: 10 segundos
- **Cliente** (`Header.jsx`): busca agendamentos + mensagens não lidas
- **Artista** (`ArtistDashboard.jsx`): busca agendamentos + mensagens não lidas
- **Som**: toca beep (880Hz, 0.3s) quando detecta AUMENTO em mensagens não lidas

### Função tocarBeep()
```javascript
const tocarBeep = () => {
  const ctx = new AudioContext()
  const osc = ctx.createOscillator()
  const gain = ctx.createGain()
  osc.connect(gain)
  gain.connect(ctx.destination)
  osc.frequency.value = 880
  gain.gain.setValueAtTime(0.1, ctx.currentTime)
  gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3)
  osc.start(ctx.currentTime)
  osc.stop(ctx.currentTime + 0.3)
}
```

### Lógica de Badge
```javascript
// Cliente (Header.jsx)
const sinoAtivo = localStorage.getItem('notif_sino_ativo') !== 'false'
const msgAtivo = localStorage.getItem('notif_msg_ativo') !== 'false'
badge = (sinoAtivo && clienteHasNew) || (msgAtivo && mensagensNaoLidas.length > 0)

// Artista (ArtistDashboard.jsx)
badge = artistaHasNew || mensagensNaoLidas.length > 0
```

### Marcar como Lidas
- **Quando**: Ao FECHAR o sino (não ao abrir)
- **Ação**: Chama `PATCH /api/mensagens/marcar-todas-lidas` + limpa estados

---

## Rotas e Navegação

### Rotas Públicas
- `/` → Home
- `/artistas` → Lista de artistas
- `/artista/:id` → Perfil público do artista
- `/portfolio` → Portfólio geral
- `/agendamento` → Agendamento (logado ou não)
- `/contato` → Contato
- `/para-tatuadores` → Landing page para artistas
- `/login` → Login
- `/cadastro` → Cadastro

### Rotas Protegidas
- `/perfil` → Cliente (`Profile.jsx`)
- `/artist-dashboard` → Artista (`ArtistDashboard.jsx`)
- `/admin` → Admin

### Tabs do ArtistDashboard
- `dashboard` → `DashboardTab`
- `requests` → `RequestsTab`
- `schedule` → `ScheduleTab`
- `messages` → `MessagesTab`
- `portfolio` → `PortfolioTab`
- `settings` → `SettingsTab`

### Navegação com Estado (location.state)
- `Profile.jsx`: `{ abrirChatComId, abrirChatNome }` → abre chat automaticamente com artista específico

---

## Padrões de Código

### useEffect com Polling
```javascript
useEffect(() => {
  if (!token || !user?.id) return
  
  const fetchData = () => {
    // fetch logic
  }
  
  fetchData() // executa imediatamente
  const interval = setInterval(fetchData, 10000) // repete a cada 10s
  return () => clearInterval(interval) // cleanup
}, [token, user])
```

### setState com Callback (evitar stale closure)
```javascript
// ❌ ERRADO
if (novasMsgs.length > prevMsgCount) { ... }
setPrevMsgCount(novasMsgs.length)

// ✅ CORRETO
setPrevMsgCount(prev => {
  if (novasMsgs.length > prev && prev > 0) {
    tocarBeep()
  }
  return novasMsgs.length
})
```

### Fetch com Token
```javascript
fetch(`${API_URL}/endpoint`, {
  method: 'GET',
  headers: { Authorization: `Bearer ${token}` }
})
```

### Toast Pattern
```javascript
showToast('Mensagem de sucesso')
showToast('Mensagem de erro', true) // segundo parâmetro = isError
```

---

## Bugs Resolvidos (Não Repetir)

### 1. Circular Reference em Profile.jsx
- **Problema**: useEffect usando variável definida depois
- **Solução**: Definir `artistasUnicos` ANTES de usar em useEffect

### 2. Bio não persistindo após refresh
- **Problema**: `SettingsTab` só lia de localStorage, nunca da API
- **Solução**: useEffect para buscar dados da API ao montar + atualizar localStorage

### 3. prevMsgCount stale closure
- **Problema**: `prevMsgCount` não atualizava corretamente no polling
- **Solução**: Usar `setPrevMsgCount(prev => ...)` com callback funcional

### 4. Mensagens marcadas como lidas ao abrir sino
- **Problema**: UX ruim - usuário não conseguia revisar antes de marcar
- **Solução**: Marcar como lidas ao FECHAR sino, não ao abrir

### 5. EmailJS removido do frontend
- **Problema**: Dependência externa desnecessária, credenciais expostas no frontend
- **Solução**: Implementado Spring Boot Mail no backend com endpoint `/api/contato`

### 6. Contact.jsx com autocomplete branco
- **Problema**: Browser aplicava background branco em inputs com autocomplete
- **Solução**: Adicionar `colorScheme: 'dark'` inline em todos os inputs/textareas

---

## Variáveis de Ambiente

### Backend (application.properties)
```properties
spring.datasource.url=jdbc:sqlserver://...
spring.datasource.username=...
spring.datasource.password=...
jwt.secret=...
jwt.expiration=86400000
cloudinary.cloud-name=...
cloudinary.api-key=...
cloudinary.api-secret=...
groq.api.key=...
GMAIL_USER=inkflowstudios07@gmail.com
GMAIL_APP_PASSWORD=<senha de app do Gmail>
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${GMAIL_USER}
spring.mail.password=${GMAIL_APP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Instruções para gerar senha de app do Gmail:**
1. Acessar `myaccount.google.com`
2. Segurança → Verificação em duas etapas (ativar se não estiver)
3. Segurança → Senhas de app → Gerar nova senha para "Mail"
4. Copiar a senha gerada (16 caracteres) e colocar no Render como `GMAIL_APP_PASSWORD`

### Frontend (.env)
```
VITE_API_URL=https://inkflowbackend-4w1g.onrender.com/api/v1
VITE_GROQ_API_KEY=<key>
VITE_CLOUDINARY_CLOUD_NAME=<name>
VITE_CLOUDINARY_UPLOAD_PRESET=<preset>
```

### API_URL Pattern (Frontend)
```javascript
// Remover /v1 para endpoints customizados (mensagens, chat)
const API_URL = import.meta.env.VITE_API_URL?.replace('/v1', '') || 'https://inkflowbackend-4w1g.onrender.com/api'
```

---

## Design System

### Cores Principais
- **Background Principal**: `#0a0a0a` (preto profundo)
- **Background Secundário**: `#1a1a1a` (cards, inputs)
- **Vermelho Primário**: `#E21B3C` (botões, títulos, estados ativos)
- **Texto Branco**: `#ffffff`
- **Texto Secundário**: `rgba(255,255,255,0.7)`
- **Borda Sutil**: `rgba(255,255,255,0.1)`
- **Input Background**: `rgba(255,255,255,0.04)`
- **WhatsApp Verde**: `#25D366`
- **Sucesso Verde**: `#22c55e`
- **Aviso Vermelho**: `#E21B3C`

### Padrões de Estilo
- **Labels**: uppercase, `letter-spacing: 1px`, `font-size: 0.75rem`
- **Inputs**: `colorScheme: 'dark'` obrigatório para prevenir autocomplete branco
- **Botões Primários**: gradient vermelho `linear-gradient(135deg, #E21B3C 0%, #8B0000 100%)`
- **Botões Secundários**: background `#1a1a1a`, hover `#2a2a2a`
- **Cards**: background `#1a1a1a`, border `rgba(255,255,255,0.1)`, border-radius `12px`
- **Transições**: `0.3s ease` para hover/focus

### Contadores de Caracteres
```javascript
// Padrão para contadores visuais
const charCount = value.length
const maxChars = 60
const isNearLimit = charCount > maxChars * 0.9

<span style={{ color: isNearLimit ? '#E21B3C' : 'rgba(255,255,255,0.5)' }}>
  {charCount}/{maxChars}
</span>
```

### Formatação de Telefone Brasileiro
```javascript
const formatTelefone = (value) => {
  const nums = value.replace(/\D/g, '').slice(0, 11)
  if (nums.length <= 2) return nums
  if (nums.length <= 7) return `(${nums.slice(0,2)}) ${nums.slice(2)}`
  return `(${nums.slice(0,2)}) ${nums.slice(2,7)}-${nums.slice(7)}`
}
```

### Validação de Senha (Frontend)
```javascript
const passwordValidation = {
  minLength: password.length >= 8,
  hasUpperCase: /[A-Z]/.test(password),
  hasNumber: /[0-9]/.test(password),
  hasSpecialChar: /[!@#$%^&*]/.test(password)
}

const isPasswordValid = Object.values(passwordValidation).every(v => v)
```

### Validação de Senha (Backend)
```java
@Pattern(
  regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
  message = "A senha deve ter no mínimo 8 caracteres, incluindo letra maiúscula, número e caractere especial (!@#$%^&*)"
)
private String password;
```

---

## Status de Segurança

### Auditoria Completa Realizada
- ✅ **30/30 issues resolvidas** (100%)
- ✅ 8 críticas resolvidas
- ✅ 12 médias resolvidas
- ✅ 10 baixas resolvidas

### Principais Correções Implementadas
1. **URLs Centralizadas**: Todas as chamadas de API centralizadas em `inkflowApi.js`
2. **Rate Limiting**: Implementado em login, agendamento, contato, chat e leads
3. **Validação de Ownership**: Mensagens validam relação de agendamento
4. **DTOs Seguros**: Endpoints críticos usam DTOs em vez de entities
5. **Cleanup de Memória**: Rate limit filters com limpeza automática
6. **Validação de Senha Forte**: Regex no backend + checklist visual no frontend
7. **Upload Seguro**: Validação de magic bytes no Cloudinary
8. **Anti Double-Booking**: Validação de conflitos de horário
9. **Whitelist de Status**: Apenas status válidos aceitos
10. **Logger Adequado**: SLF4J em vez de System.err

### Pontos de Atenção
- **CORS**: Atualmente permite `*` (recomendado restringir para produção)
- **Cloudinary**: Credenciais expostas no frontend (recomendado proxy no backend)
- **Refresh Tokens**: Não implementado (apenas access token)

---

## Convenções de Nomenclatura

### Estados Booleanos
- `clienteHasNew` → cliente tem novos agendamentos
- `artistaHasNew` → artista tem novos agendamentos
- `studioOpen` → estúdio aceitando agendamentos
- `notifOpen` → dropdown de notificações aberto
- `drawerOpen` → drawer lateral aberto
- `sidebarOpen` → sidebar aberta (mobile)

### Arrays
- `mensagensNaoLidas` → array de mensagens não lidas
- `notifItems` → array de agendamentos para notificação
- `agendamentos` → array de agendamentos
- `artistas` → array de artistas

### Handlers
- `handleAbrirSinoCliente` → abre/fecha sino do cliente
- `handleToggleNotif` → toggle de preferência de notificação
- `switchTab` → muda tab no dashboard
- `openDrawer` → abre drawer com detalhes do agendamento
- `closeDrawer` → fecha drawer

---

## Melhorias Futuras Sugeridas

### Segurança
- [ ] Implementar Cloudinary Proxy (evitar exposição de credenciais no frontend)
- [ ] Restringir CORS para domínios específicos (atualmente permite `*`)
- [ ] Implementar refresh tokens (atualmente apenas access token)
- [ ] Adicionar 2FA para artistas e admins

### Performance
- [ ] Implementar cache Redis para endpoints de leitura frequente
- [ ] Otimizar queries N+1 (usar JOIN FETCH)
- [ ] Implementar paginação em listagens grandes
- [ ] Lazy loading de imagens no portfólio

### UX/UI
- [ ] Implementar WebSocket para chat em tempo real (substituir polling)
- [ ] Adicionar notificações push (PWA)
- [ ] Modo offline com Service Worker
- [ ] Tema claro/escuro configurável

### Funcionalidades
- [ ] Sistema de avaliações (cliente avalia artista)
- [ ] Pagamento online integrado (Stripe/PagSeguro)
- [ ] Galeria de inspirações pública
- [ ] Sistema de cupons de desconto
- [ ] Relatórios e analytics para artistas
