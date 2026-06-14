package com.backend.INKFLOW.service;

import com.backend.INKFLOW.repository.CicatrizacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatusDiasScheduler {

    private static final Logger log = LoggerFactory.getLogger(StatusDiasScheduler.class);

    @Autowired private CicatrizacaoRepository cicatrizacaoRepository;
    @Autowired private CicatrizacaoService cicatrizacaoService;

    @Scheduled(cron = "0 1 0 * * *")
    public void atualizarTodasCicatrizacoesAtivas() {
        var ativas = cicatrizacaoRepository.findAllByStatus("ATIVA");
        log.info("[Scheduler] Atualizando status de {} cicatrizacoes ativas", ativas.size());
        for (var cic : ativas) {
            try {
                cicatrizacaoService.atualizarStatusDias(cic.getId());
            } catch (Exception e) {
                log.error("[Scheduler] Erro ao atualizar cicatrizacaoId={}: {}", cic.getId(), e.getMessage());
            }
        }
    }
}
