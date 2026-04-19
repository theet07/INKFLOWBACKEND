package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.model.Admin;
import com.backend.INKFLOW.model.Artista;
import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.model.DisponibilidadeArtista;
import com.backend.INKFLOW.model.PortfolioItem;
import com.backend.INKFLOW.repository.AdminRepository;
import com.backend.INKFLOW.repository.AgendamentoRepository;
import com.backend.INKFLOW.repository.ArtistaRepository;
import com.backend.INKFLOW.repository.ClienteRepository;
import com.backend.INKFLOW.repository.DisponibilidadeRepository;
import com.backend.INKFLOW.repository.PortfolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${backup.webhook.url:}")
    private String webhookUrl;

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ArtistaRepository artistaRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private DisponibilidadeRepository disponibilidadeRepository;

    public boolean isWebhookConfigurado() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    /**
     * Gera o conteudo SQL completo com todos os dados atuais do banco.
     * Cada tabela e processada em bloco try-catch independente para que
     * uma falha isolada nao derrube o backup inteiro.
     */
    public String gerarSql() {
        StringBuilder sql = new StringBuilder();
        String ts = LocalDateTime.now().format(FMT);

        sql.append("-- ============================================================\n");
        sql.append("-- InkFlow — Disaster Recovery Backup\n");
        sql.append("-- Gerado em: ").append(ts).append("\n");
        sql.append("-- Rodar em um banco SQL Server vazio para restauracao completa.\n");
        sql.append("-- ============================================================\n\n");

        // --------------------------------------------------------
        // 1. ARTISTAS (sem FK)
        // --------------------------------------------------------
        sql.append("-- ========================\n-- ARTISTAS\n-- ========================\n");
        sql.append("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='artistas' AND xtype='U')\n");
        sql.append("CREATE TABLE artistas (\n");
        sql.append("    id INT IDENTITY(1,1) PRIMARY KEY,\n");
        sql.append("    nome NVARCHAR(255) NOT NULL,\n");
        sql.append("    role NVARCHAR(255) NULL,\n");
        sql.append("    especialidades NVARCHAR(500) NULL,\n");
        sql.append("    bio NVARCHAR(500) NULL,\n");
        sql.append("    foto_url NVARCHAR(500) NULL,\n");
        sql.append("    ativo BIT NOT NULL DEFAULT 1,\n");
        sql.append("    email NVARCHAR(255) NULL UNIQUE,\n");
        sql.append("    senha NVARCHAR(255) NULL\n");
        sql.append(");\n\n");
        try {
            sql.append("SET IDENTITY_INSERT artistas ON;\n");
            for (Artista a : artistaRepository.findAll()) {
                sql.append(String.format(
                    "INSERT INTO artistas (id, nome, role, especialidades, bio, foto_url, ativo, email, senha) VALUES (%d, %s, %s, %s, %s, %s, %s, %s, %s);\n",
                    a.getId(), q(a.getNome()), q(a.getRole()), q(a.getEspecialidades()),
                    q(a.getBio()), q(a.getFotoUrl()),
                    a.getAtivo() != null && a.getAtivo() ? "1" : "0",
                    q(a.getEmail()), q(a.getPassword())
                ));
            }
            sql.append("SET IDENTITY_INSERT artistas OFF;\n");
        } catch (Exception e) { log.error("Backup: erro em artistas: {}", e.getMessage()); }

        // --------------------------------------------------------
        // 2. CLIENTES (sem FK)
        // --------------------------------------------------------
        sql.append("\n-- ========================\n-- CLIENTES\n-- ========================\n");
        sql.append("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='clientes' AND xtype='U')\n");
        sql.append("CREATE TABLE clientes (\n");
        sql.append("    id BIGINT IDENTITY(1,1) PRIMARY KEY,\n");
        sql.append("    username NVARCHAR(50) NOT NULL UNIQUE,\n");
        sql.append("    email NVARCHAR(100) NOT NULL UNIQUE,\n");
        sql.append("    password NVARCHAR(255) NOT NULL,\n");
        sql.append("    full_name NVARCHAR(100) NULL,\n");
        sql.append("    telefone NVARCHAR(20) NULL,\n");
        sql.append("    profile_image NVARCHAR(500) NULL,\n");
        sql.append("    created_at DATETIME2 DEFAULT GETDATE()\n");
        sql.append(");\n\n");
        try {
            sql.append("SET IDENTITY_INSERT clientes ON;\n");
            for (Cliente c : clienteRepository.findAll()) {
                sql.append(String.format(
                    "INSERT INTO clientes (id, username, email, password, full_name, telefone, profile_image, created_at) VALUES (%d, %s, %s, %s, %s, %s, %s, %s);\n",
                    c.getId(), q(c.getUsername()), q(c.getEmail()), q(c.getPassword()),
                    q(c.getFullName()), q(c.getTelefone()), q(c.getProfileImage()),
                    c.getCreatedAt() != null ? q(c.getCreatedAt().format(FMT)) : "NULL"
                ));
            }
            sql.append("SET IDENTITY_INSERT clientes OFF;\n");
        } catch (Exception e) { log.error("Backup: erro em clientes: {}", e.getMessage()); }

        // --------------------------------------------------------
        // 3. ADMINS (sem FK)
        // --------------------------------------------------------
        sql.append("\n-- ========================\n-- ADMINS\n-- ========================\n");
        sql.append("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='admins' AND xtype='U')\n");
        sql.append("CREATE TABLE admins (\n");
        sql.append("    id BIGINT IDENTITY(1,1) PRIMARY KEY,\n");
        sql.append("    nome NVARCHAR(255) NOT NULL,\n");
        sql.append("    email NVARCHAR(255) NOT NULL UNIQUE,\n");
        sql.append("    password NVARCHAR(255) NOT NULL\n");
        sql.append(");\n\n");
        try {
            sql.append("SET IDENTITY_INSERT admins ON;\n");
            for (Admin a : adminRepository.findAll()) {
                sql.append(String.format(
                    "INSERT INTO admins (id, nome, email, password) VALUES (%d, %s, %s, %s);\n",
                    a.getId(), q(a.getNome()), q(a.getEmail()), q(a.getPassword())
                ));
            }
            sql.append("SET IDENTITY_INSERT admins OFF;\n");
        } catch (Exception e) { log.error("Backup: erro em admins: {}", e.getMessage()); }

        // --------------------------------------------------------
        // 4. AGENDAMENTOS (FK: clientes, artistas)
        // --------------------------------------------------------
        sql.append("\n-- ========================\n-- AGENDAMENTOS\n-- ========================\n");
        sql.append("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='agendamentos' AND xtype='U')\n");
        sql.append("CREATE TABLE agendamentos (\n");
        sql.append("    id BIGINT IDENTITY(1,1) PRIMARY KEY,\n");
        sql.append("    cliente_id BIGINT NOT NULL,\n");
        sql.append("    artista_id INT NULL,\n");
        sql.append("    data_hora DATETIME2 NOT NULL,\n");
        sql.append("    servico NVARCHAR(255) NULL,\n");
        sql.append("    descricao NVARCHAR(MAX) NULL,\n");
        sql.append("    status NVARCHAR(30) NOT NULL DEFAULT 'PENDENTE',\n");
        sql.append("    preco DECIMAL(10,2) NULL,\n");
        sql.append("    avaliacao INT NULL,\n");
        sql.append("    observacoes NVARCHAR(MAX) NULL,\n");
        sql.append("    valor_pago DECIMAL(10,2) NULL,\n");
        sql.append("    valor_pendente DECIMAL(10,2) NULL,\n");
        sql.append("    created_at DATETIME2 DEFAULT GETDATE(),\n");
        sql.append("    regiao NVARCHAR(100) NULL,\n");
        sql.append("    largura DECIMAL(6,2) NULL,\n");
        sql.append("    altura DECIMAL(6,2) NULL,\n");
        sql.append("    tags NVARCHAR(500) NULL,\n");
        sql.append("    imagem_referencia_url NVARCHAR(1000) NULL,\n");
        sql.append("    CONSTRAINT FK_ag_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),\n");
        sql.append("    CONSTRAINT FK_ag_artista FOREIGN KEY (artista_id) REFERENCES artistas(id)\n");
        sql.append(");\n\n");
        try {
            sql.append("SET IDENTITY_INSERT agendamentos ON;\n");
            for (Agendamento ag : agendamentoRepository.findAll()) {
                sql.append(String.format(
                    "INSERT INTO agendamentos (id, cliente_id, artista_id, data_hora, servico, descricao, status, preco, avaliacao, observacoes, valor_pago, valor_pendente, created_at, regiao, largura, altura, tags, imagem_referencia_url) VALUES (%d, %d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);\n",
                    ag.getId(), ag.getCliente().getId(),
                    ag.getArtista() != null ? ag.getArtista().getId().toString() : "NULL",
                    q(ag.getDataHora() != null ? ag.getDataHora().format(FMT) : null),
                    q(ag.getServico()), q(ag.getDescricao()), q(ag.getStatus()),
                    ag.getPreco() != null ? ag.getPreco().toString() : "NULL",
                    ag.getAvaliacao() != null ? ag.getAvaliacao().toString() : "NULL",
                    q(ag.getObservacoes()),
                    ag.getValorPago() != null ? ag.getValorPago().toString() : "NULL",
                    ag.getValorPendente() != null ? ag.getValorPendente().toString() : "NULL",
                    ag.getCreatedAt() != null ? q(ag.getCreatedAt().format(FMT)) : "NULL",
                    q(ag.getRegiao()),
                    ag.getLargura() != null ? ag.getLargura().toString() : "NULL",
                    ag.getAltura() != null ? ag.getAltura().toString() : "NULL",
                    q(ag.getTags()), q(ag.getImagemReferenciaUrl())
                ));
            }
            sql.append("SET IDENTITY_INSERT agendamentos OFF;\n");
        } catch (Exception e) { log.error("Backup: erro em agendamentos: {}", e.getMessage()); }

        // --------------------------------------------------------
        // 5. PORTFOLIO_ITEMS (FK: artistas)
        // --------------------------------------------------------
        sql.append("\n-- ========================\n-- PORTFOLIO_ITEMS\n-- ========================\n");
        sql.append("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='portfolio_items' AND xtype='U')\n");
        sql.append("CREATE TABLE portfolio_items (\n");
        sql.append("    id BIGINT IDENTITY(1,1) PRIMARY KEY,\n");
        sql.append("    artista_id INT NOT NULL,\n");
        sql.append("    imagem_url NVARCHAR(1000) NOT NULL,\n");
        sql.append("    categoria NVARCHAR(255) NULL,\n");
        sql.append("    descricao NVARCHAR(500) NULL,\n");
        sql.append("    CONSTRAINT FK_portfolio_artista FOREIGN KEY (artista_id) REFERENCES artistas(id)\n");
        sql.append(");\n\n");
        try {
            sql.append("SET IDENTITY_INSERT portfolio_items ON;\n");
            for (PortfolioItem p : portfolioRepository.findAll()) {
                sql.append(String.format(
                    "INSERT INTO portfolio_items (id, artista_id, imagem_url, categoria, descricao) VALUES (%d, %d, %s, %s, %s);\n",
                    p.getId(), p.getArtista().getId(),
                    q(p.getImagemUrl()), q(p.getCategoria()), q(p.getDescricao())
                ));
            }
            sql.append("SET IDENTITY_INSERT portfolio_items OFF;\n");
        } catch (Exception e) { log.error("Backup: erro em portfolio: {}", e.getMessage()); }

        // --------------------------------------------------------
        // 6. DISPONIBILIDADE_ARTISTAS (FK: artistas)
        // --------------------------------------------------------
        sql.append("\n-- ========================\n-- DISPONIBILIDADE_ARTISTAS\n-- ========================\n");
        sql.append("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='disponibilidade_artistas' AND xtype='U')\n");
        sql.append("CREATE TABLE disponibilidade_artistas (\n");
        sql.append("    id BIGINT IDENTITY(1,1) PRIMARY KEY,\n");
        sql.append("    artista_id INT NOT NULL,\n");
        sql.append("    dia_semana INT NOT NULL,\n");
        sql.append("    hora_inicio NVARCHAR(5) NOT NULL,\n");
        sql.append("    hora_fim NVARCHAR(5) NOT NULL,\n");
        sql.append("    duracao_slot_minutos INT NOT NULL DEFAULT 60,\n");
        sql.append("    ativo BIT NOT NULL DEFAULT 1,\n");
        sql.append("    CONSTRAINT FK_disp_artista FOREIGN KEY (artista_id) REFERENCES artistas(id),\n");
        sql.append("    CONSTRAINT UQ_disp_artista_dia UNIQUE (artista_id, dia_semana)\n");
        sql.append(");\n\n");
        try {
            sql.append("SET IDENTITY_INSERT disponibilidade_artistas ON;\n");
            for (DisponibilidadeArtista d : disponibilidadeRepository.findAll()) {
                sql.append(String.format(
                    "INSERT INTO disponibilidade_artistas (id, artista_id, dia_semana, hora_inicio, hora_fim, duracao_slot_minutos, ativo) VALUES (%d, %d, %d, %s, %s, %d, %s);\n",
                    d.getId(), d.getArtista().getId(), d.getDiaSemana(),
                    q(d.getHoraInicio()), q(d.getHoraFim()),
                    d.getDuracaoSlotMinutos(),
                    d.getAtivo() != null && d.getAtivo() ? "1" : "0"
                ));
            }
            sql.append("SET IDENTITY_INSERT disponibilidade_artistas OFF;\n");
        } catch (Exception e) { log.error("Backup: erro em disponibilidade: {}", e.getMessage()); }

        sql.append("\n-- ============================================================\n");
        sql.append("-- Backup concluido. InkFlow restaurado com sucesso.\n");
        sql.append("-- ============================================================\n");

        return sql.toString();
    }

    /** Escapa valor para SQL — retorna NULL se nulo, ou 'valor' com aspas simples escapadas. */
    private String q(String val) {
        if (val == null) return "NULL";
        return "'" + val.replace("'", "''") + "'";
    }

    /**
     * Cron diario as 00:00 — gera backup e envia via webhook se configurado.
     * Para configurar o webhook, defina a variavel de ambiente BACKUP_WEBHOOK_URL.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void backupAutomatico() {
        log.info(">>> GATILHO DE BACKUP ACIONADO <<<");
        log.info("Iniciando rotina automatica de backup e envio para Webhook");
        try {
            String conteudo = gerarSql();
            int linhas = conteudo.split("\n").length;
            log.info("Backup gerado: {} linhas de SQL", linhas);

            if (webhookUrl != null && !webhookUrl.isBlank()) {
                enviarWebhook(conteudo);
            } else {
                log.warn("BACKUP_WEBHOOK_URL nao configurada. Backup gerado apenas em memoria.");
            }
        } catch (Exception e) {
            log.error("Falha no backup automatico: {}", e.getMessage(), e);
        }
    }

    /**
     * Envia o conteudo SQL para o webhook configurado via POST com Content-Type text/plain.
     * Configure BACKUP_WEBHOOK_URL com uma URL de destino (ex: webhook.site, servidor proprio).
     */
    public void enviarWebhook(String conteudo) {
        try {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "inkflow_backup_" + ts + ".sql";
            String boundary = "----InkFlowBackup" + ts;

            byte[] fileBytes = conteudo.getBytes(StandardCharsets.UTF_8);
            String partHeader =
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"files[0]\"; filename=\"" + filename + "\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";
            String partFooter = "\r\n--" + boundary + "--\r\n";

            byte[] headerBytes = partHeader.getBytes(StandardCharsets.UTF_8);
            byte[] footerBytes = partFooter.getBytes(StandardCharsets.UTF_8);
            byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
            System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
            System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
            System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Webhook de backup enviado. Status HTTP: {}", response.statusCode());
        } catch (Exception e) {
            log.error("Falha ao enviar webhook de backup: {}", e.getMessage(), e);
        }
    }
}
