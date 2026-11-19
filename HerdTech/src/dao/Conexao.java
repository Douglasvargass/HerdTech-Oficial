package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class Conexao {

    // Dados de acesso ao banco (ajuste conforme o seu PostgreSQL)
    private static final String URL = "jdbc:postgresql://ep-summer-leaf-b6p0fxz2-pooler.c-2.sa-east-1.aws.neon.tech:5432/herdtechdb";
    private static final String USUARIO = "neondb_owner";

    // Senha padrão do banco do grupo (se ficar null, o programa pede a senha numa janela)
    private static String senha = "npg_colqk0pdjgx2";

    // Abre uma nova conexão com o banco
    public static Connection conectar() throws SQLException {
        // Se ainda não temos a senha, pede ao usuário
        if (senha == null) {
            senha = pedirSenha();
        }

        try {
            return DriverManager.getConnection(URL, USUARIO, senha);
        } catch (SQLException e) {
            // Código 28P01 = senha incorreta: esquece a senha para pedir de novo na próxima vez
            if ("28P01".equals(e.getSQLState())) {
                senha = null;
                throw new SQLException("Senha do banco incorreta! Tente novamente.");
            }
            throw e;
        }
    }

    // Conexão direta (sem o pooler do Neon). Necessária para LISTEN/NOTIFY, que não funciona através do pooler.
    public static Connection conectarDireto() throws SQLException {
        if (senha == null) {
            throw new SQLException("Senha do banco ainda não informada.");
        }
        return DriverManager.getConnection(URL.replace("-pooler", ""), USUARIO, senha);
    }

    // Mostra uma janela para digitar a senha (campo escondido)
    private static String pedirSenha() throws SQLException {
        JPasswordField campo = new JPasswordField();
        int opcao = JOptionPane.showConfirmDialog(
                null,
                campo,
                "Senha do PostgreSQL (usuário: " + USUARIO + ")",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (opcao != JOptionPane.OK_OPTION) {
            throw new SQLException("Conexão cancelada pelo usuário.");
        }
        return new String(campo.getPassword());
    }
}
