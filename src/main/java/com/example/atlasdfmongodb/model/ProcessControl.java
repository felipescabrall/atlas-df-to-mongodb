package com.example.atlasdfmongodb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "process_control")
public class ProcessControl {

    @Id
    private String id;

    @Field("status")
    private StatusProcesso status;

    @Field("data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Field("processo_id")
    private String processoId;

    @Field("erro_detalhes")
    private String erroDetalhes;

    public enum StatusProcesso {
        PRONTO,
        EM_EXECUCAO,
        PROCESSADO,
        ERRO
    }

    // Construtores
    public ProcessControl() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public ProcessControl(StatusProcesso status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public ProcessControl(StatusProcesso status, String processoId) {
        this.status = status;
        this.processoId = processoId;
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StatusProcesso getStatus() {
        return status;
    }

    public void setStatus(StatusProcesso status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
        
        // Limpar erro_detalhes se o status não for ERROR
        if (status != StatusProcesso.ERRO) {
            this.erroDetalhes = null;
        }
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getProcessoId() {
        return processoId;
    }

    public void setProcessoId(String processoId) {
        this.processoId = processoId;
    }

    public String getErroDetalhes() {
        return erroDetalhes;
    }

    public void setErroDetalhes(String erroDetalhes) {
        this.erroDetalhes = erroDetalhes;
    }

    // Métodos utilitários
    public void atualizarStatus(StatusProcesso novoStatus) {
        this.status = novoStatus;
        this.dataAtualizacao = LocalDateTime.now();
        
        // Limpar erro_detalhes se o status não for ERROR
        if (novoStatus != StatusProcesso.ERRO) {
            this.erroDetalhes = null;
        }
    }

    public void atualizarStatusComErro(StatusProcesso novoStatus, String erro) {
        this.status = novoStatus;
        this.erroDetalhes = erro;
        this.dataAtualizacao = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ProcessControl{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", dataAtualizacao=" + dataAtualizacao +
                ", processoId='" + processoId + '\'' +
                ", erroDetalhes='" + erroDetalhes + '\'' +
                '}';
    }
}