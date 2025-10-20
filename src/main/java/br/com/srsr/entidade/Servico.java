package br.com.srsr.entidade;

import java.math.BigDecimal;
import java.util.Date;

public class Servico {

    private Integer idSr;
    private Integer idCliente;
    private String descricao;
    private BigDecimal valorCobrado;
    private Date data, dataIni, dataFim;
    private String observacoes;
    private String clienteNome;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public java.math.BigDecimal getValorCobrado() {
        return valorCobrado;
    }

    public void setValorCobrado(java.math.BigDecimal valorCobrado) {
        this.valorCobrado = valorCobrado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Date getDataIni() {
        return dataIni;
    }

    public void setDataIni(Date dataIni) {
        this.dataIni = dataIni;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public Integer getIdSr() {
        return idSr;
    }

    public void setIdSr(Integer idSr) {
        this.idSr = idSr;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

}
