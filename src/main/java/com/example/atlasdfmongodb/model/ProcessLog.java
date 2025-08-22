package com.example.atlasdfmongodb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Document(collection = "process_logs")
public class ProcessLog {

    @Id
    private String id;

    @Field("processo_id")
    private String processoId;

    @Field("mensagem")
    private String mensagem;

    @Field("data_inicio")
    private LocalDateTime dataInicio;

    @Field("data_fim")
    private LocalDateTime dataFim;

    @Field("tempo_total_segundos")
    private Double tempoTotalSegundos; // em segundos

    @Field("etapa")
    private String etapa;

    @Field("status")
    private String status;

    // Construtores
    public ProcessLog() {
    }

    public ProcessLog(String processoId, String mensagem, String etapa) {
        this.processoId = processoId;
        this.mensagem = mensagem;
        this.etapa = etapa;
        this.dataInicio = LocalDateTime.now();
        this.status = "EM_ANDAMENTO";
    }

    public ProcessLog(String processoId, String mensagem, String etapa, String status) {
        this.processoId = processoId;
        this.mensagem = mensagem;
        this.etapa = etapa;
        this.status = status;
        this.dataInicio = LocalDateTime.now();
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

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
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
        if (this.dataInicio != null && dataFim != null) {
            long millis = ChronoUnit.MILLIS.between(this.dataInicio, dataFim);
            this.tempoTotalSegundos = millis / 1000.0;
        }
    }

    public Double getTempoTotalSegundos() {
        return tempoTotalSegundos;
    }

    public void setTempoTotalSegundos(Double tempoTotalSegundos) {
        this.tempoTotalSegundos = tempoTotalSegundos;
    }

    public String getEtapa() {
        return etapa;
    }

    public void setEtapa(String etapa) {
        this.etapa = etapa;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Métodos utilitários
    public void finalizarEtapa() {
        this.dataFim = LocalDateTime.now();
        if (this.dataInicio != null) {
            long millis = ChronoUnit.MILLIS.between(this.dataInicio, this.dataFim);
            this.tempoTotalSegundos = millis / 1000.0;
        }
        this.status = "CONCLUIDO";
    }

    public void finalizarEtapaComErro(String erro) {
        this.dataFim = LocalDateTime.now();
        if (this.dataInicio != null) {
            long millis = ChronoUnit.MILLIS.between(this.dataInicio, this.dataFim);
            this.tempoTotalSegundos = millis / 1000.0;
        }
        this.status = "ERRO";
        this.mensagem = this.mensagem + " - ERRO: " + erro;
    }

    @Override
    public String toString() {
        return "ProcessLog{" +
                "id='" + id + '\'' +
                ", processoId='" + processoId + '\'' +
                ", mensagem='" + mensagem + '\'' +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", tempoTotalSegundos=" + tempoTotalSegundos +
                ", etapa='" + etapa + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}