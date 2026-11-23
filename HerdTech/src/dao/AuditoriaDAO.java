package dao;

import models.RegistroAuditoria;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Trilha de auditoria: guarda inclusões, alterações, exclusões e consultas na tabela "auditoria".
// Uma falha ao gravar o log NUNCA atrapalha a operação principal do sistema.
public class AuditoriaDAO {
    public static final String INCLUSAO = "INCLUSAO";
    public static final String ALTERACAO = "ALTERACAO";
    public static final String EXCLUSAO = "EXCLUSAO";
    public static final String CONSULTA = "CONSULTA";

    private static final String USUARIO = System.getProperty("user.name", "desconhecido");
    private static final String MAQUINA = descobrirMaquina();
    private static boolean tabelaPronta;

    // Por padrão as leituras internas (carregar telas, validar duplicidade, preencher formulários,
    // atualização automática) NÃO entram no log. Só entram as consultas pedidas pelo usuário
    // (ver comoConsulta), senão o log se enche de registros que ninguém fez.
    private static final ThreadLocal<Boolean> SEM_CONSULTAS = ThreadLocal.withInitial(() -> true);

    // Consultas são gravadas em segundo plano para não deixar as telas lentas
    private static final ExecutorService SEGUNDO_PLANO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "auditoria");
        t.setDaemon(true);
        return t;
    });

    private static String descobrirMaquina() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            String nome = System.getenv("COMPUTERNAME");
            return nome != null ? nome : "desconhecida";
        }
    }

    // Executa a ação registrando as consultas feitas por ela (usado quando o usuário clica em "Atualizar")
    public static void comoConsulta(Runnable acao) {
        boolean anterior = SEM_CONSULTAS.get();
        SEM_CONSULTAS.set(false);
        try {
            acao.run();
        } finally {
            SEM_CONSULTAS.set(anterior);
        }
    }

    public static void registrar(String acao, String entidade, Integer registroId, String detalhes) {
        boolean consulta = CONSULTA.equals(acao);
        if (consulta && SEM_CONSULTAS.get()) return;

        LocalDateTime agora = LocalDateTime.now();
        Runnable gravar = () -> gravar(agora, acao, entidade, registroId, detalhes);
        if (consulta) SEGUNDO_PLANO.execute(gravar);
        else gravar.run();
    }

    // ---------------------------------------------------------------- Texto dos detalhes

    // "campo: valor" um por linha (usado nas inclusões e exclusões)
    public static String lista(Map<String, String> campos) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> c : campos.entrySet()) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(c.getKey()).append(": ").append(c.getValue());
        }
        return sb.toString();
    }

    // "campo=valor, campo=valor" em uma linha (usado nas consultas)
    public static String resumo(Map<String, String> campos) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> c : campos.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(c.getKey()).append('=').append(c.getValue());
        }
        return sb.toString();
    }

    // Só o que mudou: "campo: 'antes' -> 'depois'"
    public static String diferencas(Map<String, String> antes, Map<String, String> depois) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> c : depois.entrySet()) {
            String valorAntes = antes.get(c.getKey());
            if (!Objects.equals(valorAntes, c.getValue())) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(c.getKey()).append(": '").append(valorAntes == null ? "" : valorAntes)
                        .append("' -> '").append(c.getValue()).append("'");
            }
        }
        return sb.length() == 0 ? "Nenhum campo foi modificado." : sb.toString();
    }

    // ---------------------------------------------------------------- Banco

    private static synchronized void gravar(LocalDateTime dataHora, String acao, String entidade,
                                            Integer registroId, String detalhes) {
        try (Connection con = Conexao.conectar()) {
            garantirTabela(con);
            con.setAutoCommit(false);

            // Se o gatilho do banco já registrou esta mudança (a versão antiga do sistema não grava log),
            // troca o registro simples dele por este, que tem máquina, usuário e detalhes completos
            if (registroId != null && !CONSULTA.equals(acao)) {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM auditoria WHERE id = (SELECT id FROM auditoria WHERE origem = 'TRIGGER' "
                                + "AND entidade = ? AND registro_id = ? AND acao = ? "
                                + "AND data_hora >= (now() AT TIME ZONE 'America/Sao_Paulo') - interval '2 minutes' "
                                + "ORDER BY id DESC LIMIT 1)")) {
                    ps.setString(1, entidade);
                    ps.setInt(2, registroId);
                    ps.setString(3, acao);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO auditoria (data_hora, maquina, usuario, acao, entidade, registro_id, detalhes) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setTimestamp(1, Timestamp.valueOf(dataHora));
                ps.setString(2, MAQUINA);
                ps.setString(3, USUARIO);
                ps.setString(4, acao);
                ps.setString(5, entidade);
                if (registroId == null) ps.setNull(6, java.sql.Types.INTEGER);
                else ps.setInt(6, registroId);
                ps.setString(7, detalhes);
                ps.executeUpdate();
            }
            con.commit();
        } catch (Exception e) {
            System.err.println("Falha ao gravar auditoria: " + e.getMessage());
        }
    }

    // Cria a tabela (e a coluna "maquina", em bancos que já tinham a versão anterior)
    static synchronized void garantirTabela(Connection con) throws SQLException {
        if (tabelaPronta) return;
        try (Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS auditoria ("
                    + "id SERIAL PRIMARY KEY, "
                    + "data_hora TIMESTAMP NOT NULL, "
                    + "maquina VARCHAR(100), "
                    + "usuario VARCHAR(100) NOT NULL, "
                    + "acao VARCHAR(20) NOT NULL, "
                    + "entidade VARCHAR(30) NOT NULL, "
                    + "registro_id INTEGER, "
                    + "detalhes TEXT)");
            st.execute("ALTER TABLE auditoria ADD COLUMN IF NOT EXISTS maquina VARCHAR(100)");
            // origem: APP (gravado pelo programa) ou TRIGGER (gravado pelo banco)
            st.execute("ALTER TABLE auditoria ADD COLUMN IF NOT EXISTS origem VARCHAR(10) NOT NULL DEFAULT 'APP'");
        }
        tabelaPronta = true;
    }

    // Devolve os registros mais recentes primeiro (a própria leitura da auditoria não gera log)
    public ArrayList<RegistroAuditoria> listar(int limite) {
        ArrayList<RegistroAuditoria> lista = new ArrayList<>();
        try (Connection con = Conexao.conectar()) {
            garantirTabela(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM auditoria ORDER BY data_hora DESC, id DESC LIMIT ?")) {
                ps.setInt(1, limite);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int registro = rs.getInt("registro_id");
                        boolean semRegistro = rs.wasNull();
                        String maquina = rs.getString("maquina");
                        lista.add(new RegistroAuditoria(
                                rs.getInt("id"),
                                rs.getTimestamp("data_hora").toLocalDateTime(),
                                maquina == null ? "-" : maquina,
                                rs.getString("usuario"),
                                rs.getString("acao"),
                                rs.getString("entidade"),
                                semRegistro ? null : registro,
                                rs.getString("detalhes")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro no banco: " + e.getMessage());
        }
        return lista;
    }
}
