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
- Chat cliente ↔ artista com polling
- Chatbot com Groq
- Perfil público do artista (/artista/:id)
- Proteção de rotas por userType (ProtectedRoute)
- Upload de imagens via Cloudinary com validação de magic bytes
- Disponibilidade semanal do artista
- Sistema de notificações com polling de 10s para mensagens não lidas

## Estrutura dos Projetos
- Frontend correto: INKFLOWFRONTEND-LIMPO
- Backend: INKFLOWBACKEND

## Em andamento
Verificar o que estava sendo feito na sessão anterior antes de continuar.

Confirme que entendeu todas as regras antes de prosseguir.
