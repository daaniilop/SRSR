package br.com.srsr.dao;

import br.com.srsr.entidade.Servico;
import br.com.srsr.util.ConnectionFactory;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServicoDAO {

    public ServicoDAO() {
    }

    public void inserir(Servico s) throws ClassNotFoundException {
        String sql = "INSERT INTO servico_realizado (id_cliente, descricao, valor_cobrado, data_execucao, observacoes) VALUES (?,?,?,?,?)";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, s.getIdCliente());
            ps.setString(2, s.getDescricao());
            ps.setBigDecimal(3, s.getValorCobrado() == null ? new BigDecimal("0.00") : s.getValorCobrado()); //IF ELSE INLINE
            ps.setDate(4, (Date) s.getData());
            ps.setString(5, s.getObservacoes());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Falha ao Inserir Servico em ServicoDAO", ex);
        }
    }

    public List<Servico> buscarPorCliente(long idCliente) {
        String sql = "SELECT sr.id_sr, c.nome_razao AS cliente, sr.descricao, sr.data_execucao, sr.valor_cobrado, sr.observacoes "
                + "FROM servico_realizado sr JOIN cliente c ON c.id_cliente = sr.id_cliente "
                + "WHERE sr.id_cliente = ? ORDER BY sr.data_execucao DESC";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idCliente);
            ResultSet rs = ps.executeQuery();
            List<Servico> lista = new ArrayList<>();
            while (rs.next()) {
                Servico s = new Servico();
                s.setIdSr(rs.getInt("id_sr"));
                s.setClienteNome(rs.getString("cliente"));
                s.setDescricao(rs.getString("descricao"));
                s.setData(rs.getDate("data_execucao"));
                s.setValorCobrado(rs.getBigDecimal("valor_cobrado"));
                s.setObservacoes(rs.getString("observacoes"));
                lista.add(s);
            }
            return lista;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Servico> buscarPorDatas(Date dataIni, Date dataFim) throws ClassNotFoundException {
        String sql = "SELECT sr.id_sr, c.nome_razao AS cliente, sr.descricao, sr.data_execucao, sr.valor_cobrado, sr.observacoes "
                + "FROM servico_realizado sr JOIN cliente c ON c.id_cliente = sr.id_cliente "
                + "WHERE sr.data_execucao BETWEEN ? AND ? ORDER BY sr.data_execucao DESC";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, dataIni);
            ps.setDate(2, dataFim);

            try (ResultSet rs = ps.executeQuery()) {
                List<Servico> lista = new ArrayList<>();
                while (rs.next()) {
                    Servico s = new Servico();
                    s.setIdSr(rs.getInt("id_sr"));
                    s.setClienteNome(rs.getString("cliente"));
                    s.setDescricao(rs.getString("descricao"));
                    s.setData(rs.getDate("data_execucao"));   // ok, continua java.util.Date na entidade
                    s.setValorCobrado(rs.getBigDecimal("valor_cobrado"));
                    s.setObservacoes(rs.getString("observacoes"));
                    lista.add(s);
                }
                // DEBUG:
                //System.out.println("DAO retornou " + lista.size());

                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public List<Servico> listar() throws ClassNotFoundException {
        try {
            String sql = "select * from servico_realizado ORDER BY data_execucao";

            try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                try (ResultSet rs = ps.executeQuery()) {
                    List<Servico> lista = new ArrayList<>();
                    while (rs.next()) {
                        Servico s = new Servico();
                        ClienteDAO cliDAO = new ClienteDAO();

                        s.setIdSr(rs.getInt("id_sr"));
                        s.setClienteNome(cliDAO.buscar(rs.getInt("id_cliente")));
                        s.setDescricao(rs.getString("descricao"));
                        s.setData(rs.getDate("data_execucao"));   // ok,depois de mil tentativas deixei java.util.Date na entidade
                        s.setValorCobrado(rs.getBigDecimal("valor_cobrado"));
                        s.setObservacoes(rs.getString("observacoes"));
                        lista.add(s);
                    }
                    // DEBUG:
                    //System.out.println("DAO retornou " + lista.size());

                    return lista;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new RuntimeException("Falha ao Listar Servicos em servicoDAO", e);
        }

    }

    public String remover(int id) throws SQLException, ClassNotFoundException {//tendo a chance de erro ele executa o try catch
        String sql = "delete from servico_realizado where id_sr= ?";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            //  DEBUG  System.out.println("Entrou no Try ");
            ps.setInt(1, id);
            try {
                // DEBUG      System.out.println("Entrou no segundo Try ");
                ps.executeUpdate();
            } catch (SQLException e) {
                return e.toString();
            }
        }
        return "Sucesso";

    }

    public void editar(Servico servico) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE servico_realizado SET descricao = ?, valor_cobrado = ?, data_execucao = ?, observacoes = ? WHERE id_sr = ?";
        try {

            try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setString(1, servico.getDescricao());
                ps.setBigDecimal(2, servico.getValorCobrado());
                ps.setDate(3, (Date) servico.getData());
                ps.setString(4, servico.getObservacoes());
                ps.setInt(5, servico.getIdSr());

                ps.executeUpdate();

            }
        } finally {
            // garante fechamento mesmo se der erro
        }
    }
}
