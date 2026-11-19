package controllers;

import dao.AuditoriaDAO;
import models.RegistroAuditoria;
import views.AuditoriaView;
import javax.swing.SwingWorker;
import java.util.List;

public class AuditoriaController {
    private static final int LIMITE = 5000;
    private final AuditoriaView view;
    private final AuditoriaDAO dao = new AuditoriaDAO();

    public AuditoriaController(AuditoriaView view) {
        this.view = view;
        view.aoAtualizar(this::carregar);
    }

    // Busca o histórico em segundo plano e mostra na tela
    public void carregar() {
        new SwingWorker<List<RegistroAuditoria>, Void>() {
            @Override
            protected List<RegistroAuditoria> doInBackground() {
                return dao.listar(LIMITE);
            }

            @Override
            protected void done() {
                try {
                    view.atualizarTabela(get());
                } catch (Exception e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    view.alertaErro("AUDITORIA", "Não foi possível carregar o histórico.\n\n" + causa.getMessage());
                }
            }
        }.execute();
    }
}
