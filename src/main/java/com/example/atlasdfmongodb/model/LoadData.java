package com.example.atlasdfmongodb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "load_data")
public class LoadData {

    @Id
    private String id;

    @Field("data")
    private String data; // formato YYYY-MM-DD

    @Field("status")
    private String status; // pronto, processado

    @Field("data_processamento")
    private LocalDateTime dataProcessamento;

    @Field("procedimentos_executados")
    private List<ProcedimentoExecutado> procedimentosExecutados = new ArrayList<>();
    
    @Field("processoId")
    private String processoId;
    
    @Field("data_processamento_formato")
    private String dataProcessamentoFormato; // formato yyyyMMdd
    
    @Field("totalDocumentos")
    private Long totalDocumentos;
    
    @Field("documentosValidos")
    private Long documentosValidos;
    
    @Field("documentosInvalidos")
    private Long documentosInvalidos;
    
    @Field("percentualValidos")
    private Double percentualValidos;
    
    @Field("percentualInvalidos")
    private Double percentualInvalidos;
    
    @Field("tempoTotalExecucaoSegundos")
    private Double tempoTotalExecucaoSegundos;

    // Construtores
    public LoadData() {
    }

    public LoadData(String data, String status) {
        this.data = data;
        this.status = status;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }

    public void setDataProcessamento(LocalDateTime dataProcessamento) {
        this.dataProcessamento = dataProcessamento;
    }

    public List<ProcedimentoExecutado> getProcedimentosExecutados() {
        return procedimentosExecutados;
    }

    public void setProcedimentosExecutados(List<ProcedimentoExecutado> procedimentosExecutados) {
        this.procedimentosExecutados = procedimentosExecutados;
    }

    // Métodos utilitários
    public void adicionarProcedimento(String nome, double tempoExecucaoSegundos) {
        ProcedimentoExecutado procedimento = new ProcedimentoExecutado(nome, tempoExecucaoSegundos);
        this.procedimentosExecutados.add(procedimento);
    }

    @Override
    public String toString() {
        return "LoadData{" +
                "id='" + id + '\'' +
                ", data='" + data + '\'' +
                ", status='" + status + '\'' +
                ", dataProcessamento=" + dataProcessamento +
                ", procedimentosExecutados=" + procedimentosExecutados +
                '}';
    }

    // Classe interna para procedimentos executados
    public static class ProcedimentoExecutado {
        @Field("nome")
        private String nome;

        @Field("tempo_execucao_segundos")
        private Double tempoExecucaoSegundos; // em segundos

        @Field("data_execucao")
        private LocalDateTime dataExecucao;

        public ProcedimentoExecutado() {
        }

        public ProcedimentoExecutado(String nome, Double tempoExecucaoSegundos) {
            this.nome = nome;
            this.tempoExecucaoSegundos = tempoExecucaoSegundos;
            this.dataExecucao = LocalDateTime.now();
        }

        // Getters e Setters
        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public Double getTempoExecucaoSegundos() {
            return tempoExecucaoSegundos;
        }

        public void setTempoExecucaoSegundos(Double tempoExecucaoSegundos) {
            this.tempoExecucaoSegundos = tempoExecucaoSegundos;
        }

        public LocalDateTime getDataExecucao() {
            return dataExecucao;
        }

        public void setDataExecucao(LocalDateTime dataExecucao) {
            this.dataExecucao = dataExecucao;
        }

        @Override
        public String toString() {
            return "ProcedimentoExecutado{" +
                    "nome='" + nome + '\'' +
                    ", tempoExecucaoSegundos=" + tempoExecucaoSegundos +
                    ", dataExecucao=" + dataExecucao +
                    '}';
        }
    }
}