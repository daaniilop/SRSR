package br.com.srsr.beans;

import br.com.srsr.dao.ClienteDAO;
import br.com.srsr.dao.ServicoDAO;
import br.com.srsr.entidade.Cliente;
import br.com.srsr.entidade.Servico;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import static java.lang.Integer.parseInt;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;

@Named("servicoBean")
@ViewScoped
public class ServicoBean implements java.io.Serializable {

    private Servico servico = new Servico();
    private Servico servicoSelecionado = new Servico();

    private Integer idClienteSelecionado;
    private List<SelectItem> opcoesClientes;
    private List<Servico> resultado; //variavel utilizada para carregamento da consulta na página do editar/excluir
    private List<Servico> consulta; //variável utilizada para as consultas por cliente ou período de data

    private Date dataIni;
    private Date dataFim;

    @PostConstruct
    public void init() {
        try {
            // Carrega tudo na abertura da tela
            resultado = new ServicoDAO().listar();

        } catch (ClassNotFoundException e) {
            addError("Erro ao listar serviços", e.getMessage());
            resultado = Collections.emptyList();
        }
    }

    public ServicoBean() { //quando eu carro uma instância dos serviço eu já carrego a lista de clientes, para ser mostrada no View
        carregarOpcoesClientes();
    }

    public void linhaCancela(RowEditEvent<Servico> event) {
        Servico cancela = event.getObject();
        addInfo("Edição Cancelada ", "Serviço de ID: (" + cancela.getIdSr() + ") sem alteração");
        // Se você quiser recarregar do banco para descartar mudanças locais:
        init();
    }

    public void linhaEdita(RowEditEvent<Servico> event) throws SQLException, ClassNotFoundException {
        Servico ser = event.getObject();
        if (validaDataEdit(ser.getData())) {
            try {
                //aqui estou criando uma variável do tipo Data compatível com o formato do Banco de Dados
                //e jogando a data que eu editei, nesse formato para depois enviar para persistir no DAO;
                java.sql.Date data_sql = new java.sql.Date(ser.getData().getTime());
                System.out.println("Data transformada: " + data_sql);
                ser.setData(data_sql);
                // --- Persistência ---
                new ServicoDAO().editar(ser);

                String descricao = ser.getDescricao();
                if (descricao.length() > 25) { // define o limite que quiser, ex: 30 caracteres
                    descricao = descricao.substring(0, 22);
                }
                addInfo("CADASTRO ALTERADO", "Serviço de: (" + descricao.toUpperCase() + "...) alterado com sucesso.");
                init();
            } catch (ClassNotFoundException | SQLException e) {
                addError("ERRO: ", "" + e);
            }

        } else {
            FacesContext ctx = FacesContext.getCurrentInstance(); //Aqui pego o contexto atual da requisição JSF, msgs, componentes, estado das validações e etc, nesse caso em um erro
            ctx.validationFailed(); // <- mantém a linha em edição, tipo um Required="true", não sai do campo em foco
            PrimeFaces.current().ajax().update("growl"); // aqui atualizo só as mensagens de erro, sem recarregar toda a tabela
            addError("Data Inválida", "Escolha outra data");
            //init();
        }
    }

    private void carregarOpcoesClientes() {
        List<Cliente> clientes = new ClienteDAO().listar();
        opcoesClientes = new ArrayList<>();
        for (Cliente c : clientes) {
            opcoesClientes.add(new SelectItem(c.getIdCliente(), c.getNomeRazao()));
        }
    }

    public void preparaExclusao(Servico ser) {
        this.servicoSelecionado = ser; //Método usado no botão de excluir, commandButton, depois no ConfirmaDialog eu apenas confirmo a exclusão,
        //chamando o método excluir sem parametros, e usando a váriavel que está na memória.
    }

    public void excluir() { //excluir sem parametro, já está vindo o cliente selecionado com o escopo de visualização ViewScoped

        try {
            ServicoDAO serDAO = new ServicoDAO();
            //  System.out.println("Id do Cliente: " + servicoSelecionado.getIdCliente() + " Nome do Cliente: " + servicoSelecionado.getNomeRazao());

            //  if (!serDAO.clientePossuiServicos(servicoSelecionado.getIdCliente())) { //Aqui estou validando se o Cliente não possui serviço registrado
            serDAO.remover(servicoSelecionado.getIdSr());

            addInfo("Serviço: ", servicoSelecionado.getIdSr() + " removido");
            init();

        } catch (SQLException e) {
            addError("Erro ao excluir", e.getMessage()); //Aqui é validação da parte do SQL, se tiver algum erro no DAO
        } catch (ClassNotFoundException ex) {
            System.getLogger(ServicoBean.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public Boolean validaData() {

        //comando para validação de data na hora de incluir serviços, com validações para tal
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); //Comando que cria variável de data no padrão dd/MM/yyyy
        String data_escolhida = sdf.format(servico.getData()); // aqui formato a data que vem do xhtml, (ela vem inteira com hora, tres letras do mes e etc) fica formatado em dd/MM/yyyy

        java.util.Date dataHoje = new Date();  //aqui estou atribuindo a data atual do sistema, no formato completo
        String data_sistema = sdf.format(dataHoje); //aqui formatando a data no padrão dd/MM/yyyy
        //

        if (data_escolhida.equalsIgnoreCase(data_sistema)) { //se a data do sistema, for a mesma escolhida pelo usuário, retorna true, ou serja, serviço feito no mesmo dia

            return true;
        } else {
            //
            String partes_sistema[] = data_sistema.split("/");
            String partes_escolhida[] = data_escolhida.split("/");
            //
            int dia_sistema = parseInt(partes_sistema[0]);
            int dia_escolhida = parseInt(partes_escolhida[0]);
            int mes_sistema = parseInt(partes_sistema[1]);
            int mes_escolhida = parseInt(partes_escolhida[1]);

            return dia_sistema >= dia_escolhida && mes_sistema == mes_escolhida; //se o dia do sistema for maior ou igual e o mes igual, retorna verdad, se não erro de data

        }
    }

    public Boolean validaDataEdit(Date dataEscolhida) {

        //comando para validação de data na hora de incluir serviços, com validações para tal
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); //Comando que cria variável de data no padrão dd/MM/yyyy
        String data_escolhida = sdf.format(dataEscolhida); // aqui formato a data que vem do xhtml, (ela vem inteira com hora, tres letras do mes e etc) fica formatado em dd/MM/yyyy

        java.util.Date dataHoje = new Date();  //aqui estou atribuindo a data atual do sistema, no formato completo
        String data_sistema = sdf.format(dataHoje); //aqui formatando a data no padrão dd/MM/yyyy
        //

        if (data_escolhida.equalsIgnoreCase(data_sistema)) { //se a data do sistema, for a mesma escolhida pelo usuário, retorna true, ou serja, serviço feito no mesmo dia

            return true;
        } else {
            //
            String partes_sistema[] = data_sistema.split("/");
            String partes_escolhida[] = data_escolhida.split("/");
            //
            int dia_sistema = parseInt(partes_sistema[0]);
            int dia_escolhida = parseInt(partes_escolhida[0]);
            int mes_sistema = parseInt(partes_sistema[1]);
            int mes_escolhida = parseInt(partes_escolhida[1]);

            return dia_sistema >= dia_escolhida && mes_sistema == mes_escolhida; //se o dia do sistema for maior ou igual e o mes igual, retorna verdad, se não erro de data

        }
    }

    public String validaDataBusca() {
        try {
            //Esses dois itens são apenas como forma de redundancia, na View eu já deixo as datas como Required="true"
            if (servico == null) {
                addError("Erro", "Objeto serviço não informado.");
                return null;
            }
            if (servico.getDataIni() == null || servico.getDataFim() == null) {
                addError("Erro", "Datas inicial e final são obrigatórias.");
                return null;
            }

            // aqui eu normalizo as daatas para LocalDate (ignora horas/minutos/segundos)
            ZoneId zone = ZoneId.systemDefault();
            LocalDate ini = Instant.ofEpochMilli(servico.getDataIni().getTime()).atZone(zone).toLocalDate();
            LocalDate fim = Instant.ofEpochMilli(servico.getDataFim().getTime()).atZone(zone).toLocalDate();
            LocalDate hoje = LocalDate.now(zone);

            //inicio das validações com mensagem de erro e retorno null, para permanecer na mesma página
            if (ini.isAfter(fim)) {
                addError("Erro", "Data inicial não pode ser maior que a data final.");
                return null;
            }

            if (ini.isAfter(hoje) || fim.isAfter(hoje)) {
                addError("Erro", "Datas no futuro não são permitidas.");
                return null;
            }

            //aqui deu um trabalho da cachorra, mas deu certo, validando que se busca for no mês atual, a data final tem que ser a data de hoje
            boolean ambosNoMesAtual = (ini.getYear() == hoje.getYear() && fim.getYear() == hoje.getYear()
                    && ini.getMonth() == hoje.getMonth() && fim.getMonth() == hoje.getMonth());
            if (ambosNoMesAtual && !fim.isEqual(hoje)) {
                addError("Erro", "Dentro do mês atual, a data final deve ser hoje.");
                return null;
            }

            if (ini.getYear() == fim.getYear() && ini.getMonthValue() > fim.getMonthValue()) {
                addError("Erro", "Mês inicial não pode ser maior que o mês final.");
                return null;
            }

            return "true";
        } catch (Exception e) {
            addError("Erro inesperado na validação", e.getMessage());
            return null;
        }
    }

    public String salvar() {
        servico.setIdCliente(idClienteSelecionado); //pego o ID do cliente selecionado, para amarrar o serviço a um cliente

        if (servico.getValorCobrado() == null) {
            servico.setValorCobrado(new BigDecimal("0.00")); //se eu não colocar valor, salva o serviço com zero r$
        }
        if (validaData()) {
            try {
                java.sql.Date data_sql = new java.sql.Date(servico.getData().getTime());
                servico.setData(data_sql);
                new ServicoDAO().inserir(servico);

                addInfo("Serviço adicionado com Sucesso", "");
                this.servico = new Servico();
                return "servico_form.xhtml";
            } catch (ClassNotFoundException ex) {
                addError("erro", "");
            }

        } else {
            addError("Data Inválida", "Escolha outra data");
            return null;
        }
        return "servico_form.xhtml";
    }

    public String buscarPorCliente() {
        if (idClienteSelecionado != null) {
            consulta = new ServicoDAO().buscarPorCliente(idClienteSelecionado);
        } else {
            consulta = Collections.emptyList();
        }
        return null;
    }

    public String buscarPorDatas() throws ClassNotFoundException {
        // Converte as datas do bean para java.sql.Date
        java.sql.Date data_sql_ini = new java.sql.Date(servico.getDataIni().getTime());
        java.sql.Date data_sql_fim = new java.sql.Date(servico.getDataFim().getTime());

        if ("true".equalsIgnoreCase(validaDataBusca())) {

            consulta = new ServicoDAO().buscarPorDatas(data_sql_ini, data_sql_fim);

            if (consulta == null || consulta.isEmpty()) {
                addInfo("Sem registros", "Nenhum serviço no período.");
                consulta = java.util.Collections.emptyList();
            }
        } else {
            addError("Intervalo Inválido", "");
        }
        return null;
    }

    public String listaTodosServicos() {

        try {
            new ServicoDAO().listar();
        } catch (ClassNotFoundException e) {
            addError("Erro ao Listar Serviços:", "" + e);
        }

        return null;
    }

    private void addInfo(String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, resumo, detalhe));
    }

    private void addError(String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, resumo, detalhe));
    }

    // getters/setters
    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Integer getIdClienteSelecionado() {
        return idClienteSelecionado;
    }

    public void setIdClienteSelecionado(Integer idClienteSelecionado) {
        this.idClienteSelecionado = idClienteSelecionado;
    }

    public List<SelectItem> getOpcoesClientes() {
        return opcoesClientes;
    }

    public List<Servico> getResultado() {
        return resultado;
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

    public List<Servico> getConsulta() {
        return consulta;
    }

    public void setConsulta(List<Servico> consulta) {
        this.consulta = consulta;
    }

    public Servico getServicoSelecionado() {
        return servicoSelecionado;
    }

    public void setServicoSelecionado(Servico servicoSelecionado) {
        this.servicoSelecionado = servicoSelecionado;
    }

}
