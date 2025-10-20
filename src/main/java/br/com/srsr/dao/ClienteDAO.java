package br.com.srsr.dao;

import br.com.srsr.entidade.Cliente;
import br.com.srsr.util.ConnectionFactory;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteDAO {

    private Connection connection; //crio um objeto tipo conexão

    public ClienteDAO() {
        try {
            connection = ConnectionFactory.getConnection();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("erro a abrir conexão com ConnectionFactory", ex);
        }
    }

    public List<Cliente> listar() {
        String sql = "SELECT id_cliente, nome_razao, cpf_cnpj, telefone, email, observacoes FROM cliente ORDER BY nome_razao";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Cliente> lista = new ArrayList<>();
            while (rs.next()) {
                Cliente x = new Cliente();
                x.setIdCliente(rs.getInt("id_cliente"));
                x.setNomeRazao(rs.getString("nome_razao"));
                x.setCpfCnpj(rs.getString("cpf_cnpj"));
                x.setTelefone(rs.getString("telefone"));
                x.setEmail(rs.getString("email"));
                x.setObservacoes(rs.getString("observacoes"));
                lista.add(x);
            }
            return lista;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void inserir(Cliente cte) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO cliente (nome_razao, cpf_cnpj, telefone, email, observacoes) VALUES (?,?,?,?,?)";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cte.getNomeRazao());
            ps.setString(2, cte.getCpfCnpj());
            ps.setString(3, cte.getTelefone());
            ps.setString(4, cte.getEmail());
            ps.setString(5, cte.getObservacoes());
            ps.executeUpdate();
        }
        //para um select, usar execute Query, porque o resultado deve chegar em algum lugar
        //para inserção, remoção, update, execute query não precisando de retorno, o int retornado é o num de linha, afetados
        // aqui só vou tratar a inclusão ou seja relacionamento com banco, o restante de mensagens fica no bean, para ter um DAO mais enxuto e direto
    }

    public void editar(Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET cpf_cnpj = ?, nome_razao = ?, observacoes = ?, email = ?, telefone =?  WHERE id_cliente = ?";
        try {
            abreCon(); // preenche o this.connection

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, cliente.getCpfCnpj());
                ps.setString(2, cliente.getNomeRazao());
                ps.setString(3, cliente.getObservacoes());
                ps.setString(4, cliente.getEmail());
                ps.setString(5, cliente.getTelefone());
                ps.setInt(6, cliente.getIdCliente());
                ps.executeUpdate();
            }
        } finally {
            // garante fechamento mesmo se der erro
            fechaCon();
        }
    }

    public String remover(int id) throws SQLException {//tendo a chance de erro ele executa o try catch
        String sql = "delete from cliente where id_cliente= ?";
        abreCon();
        try (PreparedStatement ps = connection.prepareCall(sql)) {
            //  DEBUG  System.out.println("Entrou no Try ");
            ps.setInt(1, id);
            try {
                // DEBUG      System.out.println("Entrou no segundo Try ");
                ps.executeUpdate();
            } catch (SQLException e) {
                return e.toString();
            }
            fechaCon();
        }
        return "Sucesso";

    }

    public boolean clientePossuiServicos(int clienteId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM servico_realizado WHERE id_cliente = ?";
        abreCon();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, clienteId);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        }
    }

    public void abreCon() {

        try {
            //nesse construtor farei a conexão com a fábrica
            if (connection.isClosed()) {
                connection = ConnectionFactory.getConnection();

            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(ServicoDAO.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("erro ao abrir conexão no método", ex);
        }

    }

    public void fechaCon() {

        try {
            if (!connection.isClosed()) {
                connection.close();

            }

        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("erro ao fechar conexão no método", ex);
        }

    }

    public String buscar(int id) {
        try {
            Cliente cliente = new Cliente();
            String sql = "select * from cliente where id_cliente = ?";
            abreCon();
            ResultSet rs;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                rs = ps.executeQuery();
                rs.next();
                cliente.setNomeRazao(rs.getString("nome_razao"));
            }
            rs.close();
            fechaCon();

            return cliente.getNomeRazao();

        } catch (SQLException e) {
            Logger.getLogger(ClienteDAO.class
                    .getName()).log(Level.SEVERE, null, e);
            throw new RuntimeException("Falha ao Buscar Clientes em buscar(int id) ClienteDAO", e);
        }
    }

}
