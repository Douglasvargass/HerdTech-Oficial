package dao;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import java.sql.Connection;
import java.sql.Statement;

// "Webhook" do banco: o PostgreSQL avisa o programa (LISTEN/NOTIFY) sempre que cliente, gado,
// aplicacao ou auditoria mudam, mesmo que a alteração venha de outra máquina ou de fora do programa.
public class NotificacaoBanco {
    private static final String CANAL = "herdtech_mudanca";

    private final Runnable aoMudar;
    private Thread ouvinte;
    private volatile boolean ativo;

    // aoMudar é chamado na thread do ouvinte; quem recebe deve mover o trabalho para a tela
    public NotificacaoBanco(Runnable aoMudar) {
        this.aoMudar = aoMudar;
    }

    public synchronized void iniciar() {
        if (ativo) return;
        ativo = true;
        ouvinte = new Thread(this::ouvir, "notificacao-banco");
        ouvinte.setDaemon(true);
        ouvinte.start();
    }

    public synchronized void parar() {
        ativo = false;
        if (ouvinte != null) ouvinte.interrupt();
    }

    private void ouvir() {
        boolean gatilhosProntos = false;
        while (ativo) {
            try {
                if (!gatilhosProntos) {
                    instalarGatilhos();
                    gatilhosProntos = true;
                }
                try (Connection con = Conexao.conectarDireto()) {
                    try (Statement st = con.createStatement()) {
                        st.execute("LISTEN " + CANAL);
                    }
                    PGConnection pg = con.unwrap(PGConnection.class);
                    System.out.println("Atualização automática ativa (canal " + CANAL + ").");
                    while (ativo) {
                        PGNotification[] avisos = pg.getNotifications(10000);
                        if (avisos != null && avisos.length > 0) aoMudar.run();
                    }
                }
            } catch (Exception e) {
                if (!ativo) return;
                System.err.println("Atualização automática indisponível, tentando de novo: " + e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    // Gatilho de auditoria: registra no log toda inclusão/alteração/exclusão, inclusive as feitas por
    // usuários da versão antiga do sistema (que não gravam auditoria). Quando a versão nova também grava,
    // ela substitui o registro do gatilho por um mais completo (ver AuditoriaDAO.gravar), sem duplicar.
    private void instalarAuditoriaNoBanco(Statement st) throws Exception {
        st.execute("CREATE OR REPLACE FUNCTION herdtech_auditar() RETURNS trigger AS $$ "
                + "DECLARE ent text; ac text; reg int; det text; "
                + "BEGIN "
                + "ent := CASE TG_TABLE_NAME WHEN 'cliente' THEN 'Cliente' WHEN 'gado' THEN 'Animal' ELSE 'Vacina' END; "
                + "IF TG_OP = 'INSERT' THEN "
                + "  ac := 'INCLUSAO'; reg := NEW.id; "
                + "  det := 'Registro incluído:' || chr(10) || (SELECT string_agg(key || ': ' || coalesce(value, ''), chr(10)) "
                + "    FROM jsonb_each_text(to_jsonb(NEW)) WHERE key <> 'id'); "
                + "ELSIF TG_OP = 'UPDATE' THEN "
                + "  ac := 'ALTERACAO'; reg := NEW.id; "
                + "  det := 'Campos alterados:' || chr(10) || coalesce((SELECT string_agg(n.key || ': ''' || coalesce(o.value, '') "
                + "    || ''' -> ''' || coalesce(n.value, '') || '''', chr(10)) "
                + "    FROM jsonb_each_text(to_jsonb(NEW)) n JOIN jsonb_each_text(to_jsonb(OLD)) o ON o.key = n.key "
                + "    WHERE n.value IS DISTINCT FROM o.value), 'Nenhum campo foi modificado.'); "
                + "ELSE "
                + "  ac := 'EXCLUSAO'; reg := OLD.id; "
                + "  det := 'Registro excluído:' || chr(10) || (SELECT string_agg(key || ': ' || coalesce(value, ''), chr(10)) "
                + "    FROM jsonb_each_text(to_jsonb(OLD)) WHERE key <> 'id'); "
                + "END IF; "
                + "INSERT INTO auditoria (data_hora, maquina, usuario, acao, entidade, registro_id, detalhes, origem) "
                + "VALUES (now() AT TIME ZONE 'America/Sao_Paulo', '(versão antiga)', current_user, ac, ent, reg, det, 'TRIGGER'); "
                + "RETURN NULL; "
                + "EXCEPTION WHEN OTHERS THEN RETURN NULL; "   // a auditoria nunca pode impedir a operação
                + "END; $$ LANGUAGE plpgsql");
        for (String tabela : new String[]{"cliente", "gado", "aplicacao"}) {
            st.execute("DROP TRIGGER IF EXISTS herdtech_auditoria ON " + tabela);
            st.execute("CREATE TRIGGER herdtech_auditoria AFTER INSERT OR UPDATE OR DELETE ON " + tabela
                    + " FOR EACH ROW EXECUTE FUNCTION herdtech_auditar()");
        }
    }

    // Cria (se ainda não existirem) a função e os gatilhos que disparam o aviso
    private void instalarGatilhos() throws Exception {
        try (Connection con = Conexao.conectar(); Statement st = con.createStatement()) {
            AuditoriaDAO.garantirTabela(con);
            st.execute("CREATE OR REPLACE FUNCTION herdtech_notificar() RETURNS trigger AS $$ "
                    + "BEGIN PERFORM pg_notify('" + CANAL + "', TG_TABLE_NAME); RETURN NULL; END; $$ LANGUAGE plpgsql");
            for (String tabela : new String[]{"cliente", "gado", "aplicacao"}) {
                st.execute("DROP TRIGGER IF EXISTS herdtech_aviso ON " + tabela);
                st.execute("CREATE TRIGGER herdtech_aviso AFTER INSERT OR UPDATE OR DELETE ON " + tabela
                        + " FOR EACH STATEMENT EXECUTE FUNCTION herdtech_notificar()");
            }
            instalarAuditoriaNoBanco(st);

            // Consultas no log não disparam aviso (senão a atualização se alimentaria sozinha)
            st.execute("DROP TRIGGER IF EXISTS herdtech_aviso ON auditoria");
            st.execute("CREATE TRIGGER herdtech_aviso AFTER INSERT ON auditoria "
                    + "FOR EACH ROW WHEN (NEW.acao <> 'CONSULTA') EXECUTE FUNCTION herdtech_notificar()");
        }
    }
}
