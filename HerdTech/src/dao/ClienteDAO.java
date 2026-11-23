package dao;

import models.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClienteDAO {

    // Busca todos os clientes
    public ArrayList<Cliente> listar() {
        ArrayList<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY id";

        try (Connection con = Conexao.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(montarCliente(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro no banco: " + e.getMessage());
        }
        AuditoriaDAO.registrar(AuditoriaDAO.CONSULTA, "Cliente", null, "Listagem de clientes: " + lista.size() + " registro(s)");
        return lista;
    }

    // Busca um cliente pelo id (retorna null se não existir)
    public Cliente buscarPorId(Integer id) {
        Cliente cliente = buscar(id);
        AuditoriaDAO.registrar(AuditoriaDAO.CONSULTA, "Cliente", id, cliente == null ? "Consulta por id: não encontrado" : "Consulta por id: " + AuditoriaDAO.resumo(campos(cliente)));
        return cliente;
    }

    // Busca sem gerar log (usada para guardar o "antes" das alterações)
    private Cliente buscar(Integer id) {
        String sql = "SELECT * FROM cliente WHERE id = ?";

        try (Connection con = Conexao.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return montarCliente(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro no banco: " + e.getMessage());
        }
        return null;
    }

    // Insere um cliente e devolve o id gerado pelo banco
    public Boolean cadastrar(Cliente cliente) {
        String sql = "INSERT INTO cliente (nome, cpf, data_nasc, nome_fazenda, endereco) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexao.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNome());
            ps.setLong(2, cliente.getCpf());
            ps.setString(3, cliente.getDataNasc());
            ps.setString(4, cliente.getNomeFazenda());
            ps.setString(5, cliente.getEndereco());

            if (ps.executeUpdate() == 0) return false;

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) cliente.setId(chaves.getInt(1));
            }
            AuditoriaDAO.registrar(AuditoriaDAO.INCLUSAO, "Cliente", cliente.getId(), "Registro incluído:\n" + AuditoriaDAO.lista(campos(cliente)));
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Erro no banco: " + e.getMessage());
        }
    }

    // Atualiza os dados de um cliente
    public Boolean alterar(Cliente cliente) {
        String sql = "UPDATE cliente SET nome = ?, cpf = ?, data_nasc = ?, nome_fazenda = ?, endereco = ? WHERE id = ?";

        try (Connection con = Conexao.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNome());
            ps.setLong(2, cliente.getCpf());
            ps.setString(3, cliente.getDataNasc());
            ps.setString(4, cliente.getNomeFazenda());
            ps.setString(5, cliente.getEndereco());
            ps.setInt(6, cliente.getId());

            Map<String, String> antes = campos(buscar(cliente.getId()));
            boolean ok = ps.executeUpdate() > 0;
            if (ok) AuditoriaDAO.registrar(AuditoriaDAO.ALTERACAO, "Cliente", cliente.getId(), "Campos alterados:\n" + AuditoriaDAO.diferencas(antes, campos(cliente)));
            return ok;
        } catch (SQLException e) {
            throw new RuntimeException("Erro no banco: " + e.getMessage());
        }
    }

    // Remove um cliente pelo id
    public Boolean excluir(Integer id) {
        String sql = "DELETE FROM cliente WHERE id = ?";

        try (Connection con = Conexao.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            Map<String, String> antes = campos(buscar(id));
            boolean ok = ps.executeUpdate() > 0;
            if (ok) AuditoriaDAO.registrar(AuditoriaDAO.EXCLUSAO, "Cliente", id, "Registro excluído:\n" + AuditoriaDAO.lista(antes));
            return ok;
        } catch (SQLException e) {
            throw new RuntimeException("Erro no banco (o cliente pode ter gado cadastrado): " + e.getMessage());
        }
    }

    // Converte uma linha do banco em objeto Cliente
    private Cliente montarCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getLong("cpf"),
                rs.getString("data_nasc"),
                rs.getString("nome_fazenda"),
                rs.getString("endereco")
        );
    }

    // Campos do cliente para o log de auditoria
    private Map<String, String> campos(Cliente c) {
        Map<String, String> m = new LinkedHashMap<>();
        if (c == null) return m;
        m.put("Nome", c.getNome());
        m.put("CPF", String.valueOf(c.getCpf()));
        m.put("Nascimento", c.getDataNasc());
        m.put("Fazenda", c.getNomeFazenda());
        m.put("Endereço", c.getEndereco());
        return m;
    }
}
