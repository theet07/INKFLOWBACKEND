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

    /**
     * Gera o conteudo SQL completo com todos os dados atuais do banco.
     * Exporta INSERTs para todas as tabelas na ordem correta (sem violar FKs).
     */
    public String gerarSql() {
        StringBuilder sql = new StringBuilder();
        String ts = LocalDateTime.now().format(FMT);

        sql.append("-- InkFlow Backup\n");
        sql.append("-- Gerado em: ").append(ts).append("\n\n");

        // Artistas
        sql.append("-- ARTISTAS\n");
        for (Artista a : artistaRepository.findAll()) {
            sql.append(String.format(
                "INSERT INTO artistas (id, nome, role, especialidades, bio, foto_url, ativo, email, senha) VALUES (%d, %s, %s, %s, %s, %s, %s, %s, %s);\n",
                a.getId(), q(a.getNome()), q(a.getRole()), q(a.getEspecialidades()),
                q(a.getBio()), q(a.getFotoUrl()),
                a.getAtivo() != null && a.getAtivo() ? "1" : "0",
                q(a.getEmail()), q(a.getPassword())
            ));
        }

        // Clientes (sem expor senha em texto claro — hash BCrypt)
        sql.append("\n-- CLIENTES\n");
        for (Cliente c : clienteRepository.findAll()) {
            sql.append(String.format(
                "INSERT INTO clientes (id, username, email, password, full_name, telefone, profile_image, created_at) VALUES (%d, %s, %s, %s, %s, %s, %s, %s);\n",
                c.getId(), q(c.getUsername()), q(c.getEmail()), q(c.getPassword()),
                q(c.getFullName()), q(c.getTelefone()), q(c.getProfileImage()),
                c.getCreatedAt() != null ? q(c.getCreatedAt().format(FMT)) : "NULL"
            ));
        }

        // Admins
        sql.append("\n-- ADMINS\n");
        for (Admin a : adminRepository.findAll()) {
            sql.append(String.format(
                "INSERT INTO admins (id, nome, email, password) VALUES (%d, %s, %s, %s);\n",
                a.getId(), q(a.getNome()), q(a.getEmail()), q(a.getPassword())
            ));
        }

        // Agendamentos
        sql.append("\n-- AGENDAMENTOS\n");
        for (Agendamento ag : agendamentoRepository.findAll()) {
            sql.append(String.format(
                "INSERT INTO agendamentos (id, cliente_id, artista_id, data_hora, servico, descricao, status, preco, avaliacao, observacoes, valor_pago, valor_pendente, created_at, regiao, largura, altura, tags, imagem_referencia_url) VALUES (%d, %d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);\n",
                ag.getId(),
                ag.getCliente().getId(),
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

        // Portfolio
        sql.append("\n-- PORTFOLIO_ITEMS\n");
        for (PortfolioItem p : portfolioRepository.findAll()) {
            sql.append(String.format(
                "INSERT INTO portfolio_items (id, artista_id, imagem_url, categoria, descricao) VALUES (%d, %d, %s, %s, %s);\n",
                p.getId(), p.getArtista().getId(),
                q(p.getImagemUrl()), q(p.getCategoria()), q(p.getDescricao())
            ));
        }

        // Disponibilidade
        sql.append("\n-- DISPONIBILIDADE_ARTISTAS\n");
        for (DisponibilidadeArtista d : disponibilidadeRepository.findAll()) {
            sql.append(String.format(
                "INSERT INTO disponibilidade_artistas (id, artista_id, dia_semana, hora_inicio, hora_fim, duracao_slot_minutos, ativo) VALUES (%d, %d, %d, %s, %s, %d, %s);\n",
                d.getId(), d.getArtista().getId(), d.getDiaSemana(),
                q(d.getHoraInicio()), q(d.getHoraFim()),
                d.getDuracaoSlotMinutos(),
                d.getAtivo() != null && d.getAtivo() ? "1" : "0"
            ));
        }

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
    @Scheduled(cron = "0 0 0 * * *")
    public void backupAutomatico() {
        log.info("Iniciando backup automatico diario...");
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
    private void enviarWebhook(String conteudo) {
        try {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .header("X-Backup-Filename", "inkflow_backup_" + ts + ".sql")
                    .header("X-Backup-Source", "inkflow-backend")
                    .POST(HttpRequest.BodyPublishers.ofString(conteudo, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Webhook de backup enviado. Status HTTP: {}", response.statusCode());
        } catch (Exception e) {
            log.error("Falha ao enviar webhook de backup: {}", e.getMessage(), e);
        }
    }
}
