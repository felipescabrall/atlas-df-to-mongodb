package com.example.atlasdfmongodb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "controle_processo")
public class ControleProcesso {

    @Id
    private String id;

    @Field("processo_id")
    private String processoId;

    @Field("nome_processo")
    private String nomeProcesso;

    @Field("status")
    private StatusProcesso status;

    @Field("data_inicio")
    private LocalDateTime dataInicio;

    @Field("data_fim")
    private LocalDateTime dataFim;

    @Field("total_registros")
    private Long totalRegistros;

    @Field("registros_processados")
    private Long registrosProcessados;

    @Field("registros_erro")
    private Long registrosErro;

    @Field("logs_etapas")
    private List<LogEtapa> logsEtapas = new ArrayList<>();

    @Field("erro_detalhes")
    private String erroDetalhes;

    @Field("data_criacao")
    private LocalDateTime dataCriacao;

    @Field("data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Construtores
    public ControleProcesso() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.status = StatusProcesso.INICIADO;
        this.registrosProcessados = 0L;
        this.registrosErro = 0L;
    }

    public ControleProcesso(String processoId, String nomeProcesso) {
        this();
        this.processoId = processoId;
        this.nomeProcesso = nomeProcesso;
        this.dataInicio = LocalDateTime.now();
    }

    // Métodos utilitários
    public void adicionarLog(String etapa, String mensagem, StatusProcesso status) {
        LogEtapa log = new LogEtapa(etapa, mensagem, status, LocalDateTime.now());
        this.logsEtapas.add(log);
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void finalizarProcesso(StatusProcesso statusFinal) {
        this.status = statusFinal;
        this.dataFim = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void incrementarProcessados() {
        this.registrosProcessados++;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void incrementarErros() {
        this.registrosErro++;
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessoId() {
        return processoId;
    }

    public void setProcessoId(String processoId) {
        this.processoId = processoId;
    }

    public String getNomeProcesso() {
        return nomeProcesso;
    }

    public void setNomeProcesso(String nomeProcesso) {
        this.nomeProcesso = nomeProcesso;
    }

    public StatusProcesso getStatus() {
        return status;
    }

    public void setStatus(StatusProcesso status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Long getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(Long totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public Long getRegistrosProcessados() {
        return registrosProcessados;
    }

    public void setRegistrosProcessados(Long registrosProcessados) {
        this.registrosProcessados = registrosProcessados;
    }

    public Long getRegistrosErro() {
        return registrosErro;
    }

    public void setRegistrosErro(Long registrosErro) {
        this.registrosErro = registrosErro;
    }

    public List<LogEtapa> getLogsEtapas() {
        return logsEtapas;
    }

    public void setLogsEtapas(List<LogEtapa> logsEtapas) {
        this.logsEtapas = logsEtapas;
    }

    public String getErroDetalhes() {
        return erroDetalhes;
    }

    public void setErroDetalhes(String erroDetalhes) {
        this.erroDetalhes = erroDetalhes;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    // Enums e Classes internas
    public enum StatusProcesso {
        INICIADO,
        EM_ANDAMENTO,
        CONCLUIDO,
        ERRO,
        CANCELADO
    }

    public static class LogEtapa {
        private String etapa;
        private String mensagem;
        private StatusProcesso status;
        private LocalDateTime timestamp;

        public LogEtapa() {}

        public LogEtapa(String etapa, String mensagem, StatusProcesso status, LocalDateTime timestamp) {
            this.etapa = etapa;
            this.mensagem = mensagem;
            this.status = status;
            this.timestamp = timestamp;
        }

        // Getters e Setters
        public String getEtapa() {
            return etapa;
        }

        public void setEtapa(String etapa) {
            this.etapa = etapa;
        }

        public String getMensagem() {
            return mensagem;
        }

        public void setMensagem(String mensagem) {
            this.mensagem = mensagem;
        }

        public StatusProcesso getStatus() {
            return status;
        }

        public void setStatus(StatusProcesso status) {
            this.status = status;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}