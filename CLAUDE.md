# InkFlow — Onboarding para Nova Sessão

## Regras de Comportamento (OBRIGATÓRIAS)
1. Antes de qualquer alteração: descreva o arquivo, linha exata e o 
   que será substituído. Aguarde confirmação antes de aplicar.
2. NUNCA use PowerShell, scripts .ps1 ou manipulação por índice de 
   caractere. Use apenas fsReplace com trechos exatos.
   Se fsReplace falhar, oriente edição manual no VS Code.

## Commits e Push
- Branch: teste (frontend e backend)
- Formato: prefixo em inglês (feat:, fix:, style:, chore:) + mensagem em português
- Exemplo: `feat: adiciona polling de 10s para mensagens não lidas no header do cliente`
- Ordem: backend primeiro, depois frontend (quando os dois são afetados)

## Comandos de Push
Frontend (dois remotes simultâneos):
```bash
git push origin teste && git push netelinriquen teste
```

Backend (um remote):
```bash
git push origin teste
```

## Repositórios
- Frontend origin: theet07/INKFLOWFRONTEND (branch: teste)
- Frontend netelinriquen: netelinriquen/INKFLOWFRONTEND (branch: teste)
- Backend origin: theet07/INKFLOWBACKEND (branch: teste)

## Stack
- Frontend: React + Vite → Vercel
- Backend: Spring Boot → Render
- Banco: SQL Server (somee.com)
- Storage: Cloudinary
- Auth: JWT com blacklist
- IA: Groq (llama-3.3-70b-versatile)

## O que está implementado e funcionando
- Autenticação JWT com rate limiting, blacklist e BCrypt strength 12
- Fluxo completo de agendamento (cliente logado e não logado)
- Dashboard do artista (agenda, solicitações, portfólio, configurações, mensagens)
- Dashboard admin com backup automático por email
- Chat cliente ↔ artista com polling de 10s
- Chatbot com Groq (system prompt: InkFlow opera em "todo o Brasil")
- Perfil público do artista (/artista/:id)
- Proteção de rotas por userType (ProtectedRoute)
- Upload de imagens via Cloudinary com validação de magic bytes
- Disponibilidade semanal do artista
- Sistema de notificações com polling de 10s:
  - Cliente (Header.jsx): badge no sino para agendamentos + mensagens
  - Artista (ArtistDashboard.jsx): badge no sino para agendamentos + mensagens
  - Som de beep ao receber nova mensagem (configurável)
  - Preferências salvas em localStorage (sino, mensagens, som)

## Estrutura dos Projetos
- Frontend correto: INKFLOWFRONTEND-LIMPO
- Backend: INKFLOWBACKEND

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
```

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
