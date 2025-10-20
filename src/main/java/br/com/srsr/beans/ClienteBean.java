package br.com.srsr.beans;

import br.com.srsr.dao.ClienteDAO;
import br.com.srsr.entidade.Cliente;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.sql.SQLException;
import java.util.List;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;

@Named("clienteBean")
@ViewScoped
public class ClienteBean implements java.io.Serializable {

    private Cliente cliente = new Cliente();
    private Cliente clienteSelecionado = new Cliente();
    private List<Cliente> clientes;

    @PostConstruct
    public void init() { //esse método é utilizado para reinciiar a página sempre que preciso, por exemplo, após uma exclusão
        ClienteDAO cliDAO = new ClienteDAO();
        clientes = cliDAO.listar();

    }

    public ClienteBean() {

    }

    public String salvar() {
        try {
            String doc = cliente.getCpfCnpj();
            doc = doc.replaceAll("\\D", ""); // com isso aqui eu tô deixando o doc apenas com números

            String email = cliente.getEmail();

            if (doc.length() == 11 || doc.length() == 14) {
                cliente.setCpfCnpj(doc);
            } else {
                addError("CPF/CNPJ inválido",
                        "O campo CPF/CNPJ só aceita números (11 para CPF ou 14 para CNPJ).");
                return null; // permanece na MESMA página
            }
            if (email == null || email.isBlank()) {       // "" ou só espaços → trata como ausente, validação feita para passar no banco, adicionando um
                cliente.setEmail(null);                     // ou vários clientes sem um endereço de email
            }
            new ClienteDAO().inserir(cliente);

            addInfo("CADASTRO REALIZADO", "Cliente salvo com sucesso.");
            cliente = new Cliente(); // limpa o form
            return null; // permanece na MESMA página

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) { // Postgres unique_violation
                addError("Registro duplicado", detalhePostgres(e));
            } else {
                addError("Erro ao salvar", "Detalhe técnico: " + e.getMessage());
            }
            return null; // permanece na mesma página para mostrar as mensagens
        } catch (ClassNotFoundException e) {

            addError("Erro inesperado", "Tente novamente.");
            return null;
        }
    }

    public void preparaExclusao(Cliente cli) {
        this.clienteSelecionado = cli;//Método usado no botão de excluir, commandButton, depois no ConfirmaDialog eu apenas confirmo a exclusão,
        //chamando o método excluir sem parametros, e usando a váriavel que está na memória.
    }

    public void excluir() { //excluir sem parametro, já está vindo o cliente selecionado com o escopo de visualização ViewScoped

        try {
            ClienteDAO cliDAO = new ClienteDAO();
            System.out.println("Id do Cliente: " + clienteSelecionado.getIdCliente() + " Nome do Cliente: " + clienteSelecionado.getNomeRazao());

            if (!cliDAO.clientePossuiServicos(clienteSelecionado.getIdCliente())) { //Aqui estou validando se o Cliente não possui serviço registrado

                cliDAO.remover(clienteSelecionado.getIdCliente());

                addInfo("Cliente: ", clienteSelecionado.getNomeRazao() + " removido");
                init();
            } else {
                addError("Erro ao excluir Cliente", "Existe Serviço Registrado para esse Cadastro");
            }
        } catch (SQLException e) {
            addError("Erro ao excluir", e.getMessage()); //Aqui é validação da parte do SQL, se tiver algum erro no DAO
        }
    }

    public void linhaEdita(RowEditEvent<Cliente> event) {
        Cliente cli = event.getObject();             //Trago da view a linha editada, com esse componente do JSF, o ROWEDIT, atenção que é necessário ter os três atributos para funcionar
        try {
            // --- Normalização do documento ---
            String doc = cli.getCpfCnpj();
            doc = doc.replaceAll("\\D", "");     // deixa só números
            if (!doc.chars().allMatch(ch -> ch >= '0' && ch <= '9')) { // evita dígitos Unicode){
                addError("CPF/CNPJ inválido",
                        "Caracteres inválidos");
                FacesContext ctx = FacesContext.getCurrentInstance(); //Aqui pego o contexto atual da requisição JSF, msgs, componentes, estado das validações e etc, nesse caso em um erro
                ctx.validationFailed(); // <- mantém a linha em edição, tipo um Required="true", não sai do campo em foco
                PrimeFaces.current().ajax().update("growl"); // aqui atualizo só as mensagens de erro, sem recarregar toda a tabela
//init();
                return;
            }
            if (!(doc.length() == 11 || doc.length() == 14)) {
                addError("CPF/CNPJ inválido",
                        "O campo CPF/CNPJ só aceita números (11 para CPF ou 14 para CNPJ).");
                FacesContext ctx = FacesContext.getCurrentInstance(); //Aqui pego o contexto atual da requisição JSF, msgs, componentes, estado das validações e etc, nesse caso em um erro
                ctx.validationFailed(); // <- mantém a linha em edição, tipo um Required="true", não sai do campo em foco
                PrimeFaces.current().ajax().update("growl"); // aqui atualizo só as mensagens de erro, sem recarregar toda a tabela
                return;
            } else {
                cli.setCpfCnpj(doc);
            }

            // --- Normalização do e-mail ---
            String email = cli.getEmail();
            if (email == null || email.isBlank()) {
                cli.setEmail(null);                  // aceita cliente sem e-mail, fazendo isso, salvo como NULL e naão como caracter em branco
            } else {
                cli.setEmail(email.trim().toLowerCase());
                // vou tentar no futuro validar se o campo não tem parametros de e-mail tipo @ e etc:
                // if (!email.matches("^[^@")) { ... return; }
            }

            // --- Persistência ---
            new ClienteDAO().editar(cli);

            addInfo("CADASTRO ALTERADO", "Cliente: " + cli.getNomeRazao() + " alterado com sucesso.");

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {   // Postgres: unique_violation
                init();
                addError("Registro duplicado", detalhePostgres(e));

            } else {
                init();
                addError("Erro ao salvar", "Detalhe técnico: " + e.getMessage());
            }
        }
    }

    public void linhaCancela(RowEditEvent<Cliente> event) {
        Cliente cancela = event.getObject();
        addInfo("Edição Cancelada ", "Cliente: (" + cancela.getNomeRazao().toUpperCase() + ") sem alteração");
        // Se você quiser recarregar do banco para descartar mudanças locais:
        init();
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

    public Cliente getClienteSelecionado() {
        return clienteSelecionado;
    }

    public void setClienteSelecionado(Cliente clienteSelecionado) {
        this.clienteSelecionado = clienteSelecionado;
    }

    private void addInfo(String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, resumo, detalhe));
    }

    private void addError(String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, resumo, detalhe));
    }

    private String detalhePostgres(SQLException e) {
        String msg = e.getMessage();
        if (msg != null) {
            if (msg.contains("uk_cliente_email") || msg.toLowerCase().contains("(email)")) {
                return "Já existe um cliente com este e-mail.";
            }
            if (msg.contains("uk_cliente_cpf") || msg.toLowerCase().contains("(cpf_cnpj)")) {
                return "Já existe um cliente com este CPF/CNPJ.";
            }

        }
        return "Já existe um registro com valor repetido em campo único.";
    }

}
