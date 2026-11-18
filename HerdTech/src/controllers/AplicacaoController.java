package controllers;

import exceptions.AplicacaoException;
import models.Aplicacao;
import models.Gado;
import services.AplicacaoService;
import services.GadoService;
import views.AplicacaoView;

import java.util.List;

public class AplicacaoController {
    private final AplicacaoView aplicacaoView;
    private final AplicacaoService aplicacaoService = new AplicacaoService();
    private final GadoService gadoService = new GadoService();

    // Avisa o HerdTechController para recarregar as telas depois de uma alteração
    private final Runnable aoAlterarDados;

    public AplicacaoController(AplicacaoView aplicacaoView, Runnable aoAlterarDados) {
        this.aplicacaoView = aplicacaoView;
        this.aoAlterarDados = aoAlterarDados;

        // Botões da tela
        aplicacaoView.aoNovo(this::cadastrar);
        aplicacaoView.aoEditar(this::alterar);
        aplicacaoView.aoExcluir(this::excluir);
        aplicacaoView.aoAtualizar(aoAlterarDados);
    }

    public void cadastrar() {
        try {
            List<Gado> gados = gadoService.listarTodos();
            if (gados.isEmpty()) {
                aplicacaoView.alertaErro("CADASTRO DE APLICAÇÃO", "Cadastre um animal antes de registrar vacinas.");
                return;
            }

            Aplicacao aplicacao = new Aplicacao();
            while (true) {
                try {
                    aplicacao = aplicacaoView.informarDados("CADASTRO DE APLICAÇÃO", aplicacao, gados);
                    if (aplicacao == null) return;

                    if (aplicacaoService.cadastrar(aplicacao)) {
                        aplicacaoView.alertaSucesso("CADASTRO DE APLICAÇÃO", "Aplicação de " + aplicacao.getVacina()
                                + " registrada para " + aplicacao.getGado().getNome() + ".\nPróxima dose: " + aplicacao.getProximaVacina());
                        aoAlterarDados.run();
                    } else {
                        aplicacaoView.alertaErro("CADASTRO DE APLICAÇÃO", "Erro ao Cadastrar!");
                    }
                    return;
                } catch (AplicacaoException e) {
                    // Dados inválidos: mostra os erros e reabre o formulário com o que já foi digitado
                    aplicacaoView.alertaErro("CADASTRO DE APLICAÇÃO", e.getMessage());
                }
            }
        } catch (Exception e) {
            aplicacaoView.alertaErro("CADASTRO DE APLICAÇÃO", e.getMessage());
        }
    }

    private void excluir() {
        try {
            Integer id = aplicacaoView.getIdSelecionado();
            if (id == null) {
                aplicacaoView.alertaErro("EXCLUIR APLICAÇÃO", "Selecione uma aplicação na tabela.");
                return;
            }

            Aplicacao excluirAplicacao = aplicacaoService.buscarPorId(id);
            if (excluirAplicacao == null) {
                aplicacaoView.alertaErro("EXCLUIR APLICAÇÃO", "Ops! Aplicação inexistente");
                return;
            }

            int confirma = aplicacaoView.alertaConfirmacao("EXCLUIR APLICAÇÃO",
                    "Deseja realmente excluir a aplicação de "
                            + excluirAplicacao.getVacina()
                            + " do animal " + excluirAplicacao.getGado().getNome()
                            + " (Id: " + excluirAplicacao.getId() + ")?"
            );

            if (confirma == 0) {
                if (aplicacaoService.excluir(id)) {
                    aplicacaoView.alertaSucesso("EXCLUIR APLICAÇÃO", "Aplicação Excluida com Sucesso");
                    aoAlterarDados.run();
                } else {
                    aplicacaoView.alertaErro("EXCLUIR APLICAÇÃO", "Ops! Erro ao Excluir Aplicação");
                }
            }
        } catch (Exception e) {
            aplicacaoView.alertaErro("EXCLUIR APLICAÇÃO", e.getMessage());
        }
    }

    private void alterar() {
        try {
            Integer id = aplicacaoView.getIdSelecionado();
            if (id == null) {
                aplicacaoView.alertaErro("ALTERAR APLICAÇÃO", "Selecione uma aplicação na tabela.");
                return;
            }

            Aplicacao alterarPorId = aplicacaoService.buscarPorId(id);
            if (alterarPorId == null) {
                aplicacaoView.alertaErro("ALTERAR APLICAÇÃO", "Ops! Aplicação inexistente");
                return;
            }

            List<Gado> gados = gadoService.listarTodos();
            Aplicacao alterarAplicacao = new Aplicacao(alterarPorId.getId(), alterarPorId.getDono(), alterarPorId.getGado(), alterarPorId.getVacina(), alterarPorId.getDataAplicacao(), alterarPorId.getProximaVacina(), alterarPorId.getQuemAplicou());
            while (true) {
                try {
                    alterarAplicacao = aplicacaoView.informarDados("ALTERAR APLICAÇÃO", alterarAplicacao, gados);
                    if (alterarAplicacao == null) return;

                    if (aplicacaoService.alterar(alterarAplicacao)) {
                        aplicacaoView.alertaSucesso("ALTERAR APLICAÇÃO", "Aplicação Alterada com Sucesso");
                        aoAlterarDados.run();
                    } else {
                        aplicacaoView.alertaErro("ALTERAR APLICAÇÃO", "Ops! Erro ao Alterar Aplicação");
                    }
                    return;
                } catch (AplicacaoException e) {
                    aplicacaoView.alertaErro("ALTERAR APLICAÇÃO", e.getMessage());
                }
            }
        } catch (Exception e) {
            aplicacaoView.alertaErro("ALTERAR APLICAÇÃO", e.getMessage());
        }
    }
}
