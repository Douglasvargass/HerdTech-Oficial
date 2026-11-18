package controllers;

import exceptions.GadoException;
import models.Cliente;
import models.Gado;
import services.AplicacaoService;
import services.ClienteService;
import services.GadoService;
import views.GadoView;

import java.util.List;

public class GadoController {
    private final GadoView gadoView;
    private final GadoService gadoService = new GadoService();
    private final ClienteService clienteService = new ClienteService();
    private final AplicacaoService aplicacaoService = new AplicacaoService();

    // Avisa o HerdTechController para recarregar as telas depois de uma alteração
    private final Runnable aoAlterarDados;

    public GadoController(GadoView gadoView, Runnable aoAlterarDados) {
        this.gadoView = gadoView;
        this.aoAlterarDados = aoAlterarDados;

        // Botões da tela
        gadoView.aoNovo(this::cadastrar);
        gadoView.aoEditar(this::alterar);
        gadoView.aoExcluir(this::excluir);
        gadoView.aoAtualizar(aoAlterarDados);
    }

    public void cadastrar() {
        try {
            List<Cliente> clientes = clienteService.listarTodos();
            if (clientes.isEmpty()) {
                gadoView.alertaErro("CADASTRO DO GADO", "Cadastre um cliente (fazenda) antes de cadastrar animais.");
                return;
            }

            Gado gado = new Gado();
            while (true) {
                try {
                    gado = gadoView.informarDados("CADASTRO DO GADO", gado, clientes);
                    if (gado == null) return;

                    if (gadoService.cadastrar(gado)) {
                        gadoView.alertaSucesso("CADASTRO DO GADO", "Animal " + gado.getNome() + " cadastrado com sucesso!");
                        aoAlterarDados.run();
                    } else {
                        gadoView.alertaErro("CADASTRO DO GADO", "Erro ao Cadastrar!");
                    }
                    return;
                } catch (GadoException e) {
                    // Dados inválidos: mostra os erros e reabre o formulário com o que já foi digitado
                    gadoView.alertaErro("CADASTRO DO GADO", e.getMessage());
                }
            }
        } catch (Exception e) {
            gadoView.alertaErro("CADASTRO DO GADO", e.getMessage());
        }
    }

    private void excluir() {
        try {
            Integer id = gadoView.getIdSelecionado();
            if (id == null) {
                gadoView.alertaErro("EXCLUIR GADO", "Selecione um animal na tabela.");
                return;
            }

            Gado excluirGado = gadoService.buscarPorId(id);
            if (excluirGado == null) {
                gadoView.alertaErro("EXCLUIR GADO", "Ops! Gado inexistente");
                return;
            }

            // Um animal com vacinas registradas não pode ser excluído (o banco não deixa)
            long vacinas = aplicacaoService.listarTodos().stream()
                    .filter(aplicacao -> aplicacao.getGado().getId().equals(id)).count();
            if (vacinas > 0) {
                gadoView.alertaErro("EXCLUIR GADO", "O animal " + excluirGado.getNome() + " possui "
                        + vacinas + (vacinas == 1 ? " vacina registrada" : " vacinas registradas")
                        + ".\nExclua o histórico de vacinas dele antes de excluir o animal.");
                return;
            }

            int confirma = gadoView.alertaConfirmacao("EXCLUIR GADO",
                    "Deseja realmente excluir o animal "
                            + excluirGado.getNome()
                            + " (Id: " + excluirGado.getId() + ")?\nEssa ação não pode ser desfeita."
            );

            if (confirma == 0) {
                if (gadoService.excluir(id)) {
                    gadoView.alertaSucesso("EXCLUIR GADO", "Gado Excluido com Sucesso");
                    aoAlterarDados.run();
                } else {
                    gadoView.alertaErro("EXCLUIR GADO", "Ops! Erro ao Excluir Gado");
                }
            }
        } catch (Exception e) {
            gadoView.alertaErro("EXCLUIR GADO", e.getMessage());
        }
    }

    private void alterar() {
        try {
            Integer id = gadoView.getIdSelecionado();
            if (id == null) {
                gadoView.alertaErro("ALTERAR GADO", "Selecione um animal na tabela.");
                return;
            }

            Gado alterarPorId = gadoService.buscarPorId(id);
            if (alterarPorId == null) {
                gadoView.alertaErro("ALTERAR GADO", "Ops! Gado inexistente");
                return;
            }

            List<Cliente> clientes = clienteService.listarTodos();
            Gado alterarGado = new Gado (alterarPorId.getId(), alterarPorId.getDono(), alterarPorId.getNome(), alterarPorId.getRaca(), alterarPorId.getGenero(), alterarPorId.getDataNasc());
            while (true) {
                try {
                    alterarGado = gadoView.informarDados("ALTERAR GADO", alterarGado, clientes);
                    if (alterarGado == null) return;

                    if (gadoService.alterar(alterarGado)) {
                        gadoView.alertaSucesso("ALTERAR GADO", "Gado Alterado com Sucesso");
                        aoAlterarDados.run();
                    } else {
                        gadoView.alertaErro("ALTERAR GADO", "Ops! Erro ao Alterar Gado");
                    }
                    return;
                } catch (GadoException e) {
                    gadoView.alertaErro("ALTERAR GADO", e.getMessage());
                }
            }
        } catch (Exception e) {
            gadoView.alertaErro("ALTERAR GADO", e.getMessage());
        }
    }
}
