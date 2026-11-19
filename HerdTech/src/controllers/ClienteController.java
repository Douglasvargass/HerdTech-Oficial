package controllers;

import exceptions.ClienteException;
import models.Cliente;
import services.ClienteService;
import services.GadoService;
import views.ClienteView;

public class ClienteController {
    private final ClienteView clienteView;
    private final ClienteService clienteService = new ClienteService();
    private final GadoService gadoService = new GadoService();

    // Avisa o HerdTechController para recarregar as telas depois de uma alteração
    private final Runnable aoAlterarDados;

    public ClienteController(ClienteView clienteView, Runnable aoAlterarDados) {
        this.clienteView = clienteView;
        this.aoAlterarDados = aoAlterarDados;

        // Botões da tela
        clienteView.aoNovo(this::cadastrar);
        clienteView.aoEditar(this::alterar);
        clienteView.aoExcluir(this::excluir);
        // Só o clique do usuário em "Atualizar" conta como consulta na auditoria
        clienteView.aoAtualizar(() -> dao.AuditoriaDAO.comoConsulta(aoAlterarDados));
    }

    public void cadastrar() {
        Cliente cliente = new Cliente();
        while (true) {
            try {
                cliente = clienteView.informarDados("CADASTRO DE CLIENTE", cliente);
                if (cliente == null) return;

                if (clienteService.cadastrar(cliente)) {
                    clienteView.alertaSucesso("CADASTRO DE CLIENTE", "Cliente " + cliente.getNome() + " cadastrado com sucesso!");
                    aoAlterarDados.run();
                } else {
                    clienteView.alertaErro("CADASTRO DE CLIENTE", "Erro ao Cadastrar!");
                }
                return;
            } catch (ClienteException e) {
                // Dados inválidos: mostra os erros e reabre o formulário com o que já foi digitado
                clienteView.alertaErro("CADASTRO DE CLIENTE", e.getMessage());
            } catch (Exception e) {
                clienteView.alertaErro("CADASTRO DE CLIENTE", e.getMessage());
                return;
            }
        }
    }

    private void excluir() {
        try {
            Integer id = clienteView.getIdSelecionado();
            if (id == null) {
                clienteView.alertaErro("EXCLUIR CLIENTE", "Selecione um cliente na tabela.");
                return;
            }

            Cliente excluirCliente = clienteService.buscarPorId(id);
            if (excluirCliente == null) {
                clienteView.alertaErro("EXCLUIR CLIENTE", "Ops! Cliente inexistente");
                return;
            }

            // Um cliente com animais não pode ser excluído (o banco não deixa)
            long animais = gadoService.listarTodos().stream()
                    .filter(gado -> gado.getDono().getId().equals(id)).count();
            if (animais > 0) {
                clienteView.alertaErro("EXCLUIR CLIENTE", "O cliente " + excluirCliente.getNome() + " possui "
                        + animais + (animais == 1 ? " animal cadastrado" : " animais cadastrados")
                        + ".\nExclua os animais dele antes de excluir o cliente.");
                return;
            }

            int confirma = clienteView.alertaConfirmacao("EXCLUIR CLIENTE",
                    "Deseja realmente excluir o cliente "
                            + excluirCliente.getNome()
                            + " (Id: " + excluirCliente.getId() + ")?\nEssa ação não pode ser desfeita."
            );

            if (confirma == 0) {
                if (clienteService.excluir(id)) {
                    clienteView.alertaSucesso("EXCLUIR CLIENTE", "Cliente Excluido com Sucesso");
                    aoAlterarDados.run();
                } else {
                    clienteView.alertaErro("EXCLUIR CLIENTE", "Ops! Erro ao Excluir cliente");
                }
            }
        } catch (Exception e) {
            clienteView.alertaErro("EXCLUIR CLIENTE", e.getMessage());
        }
    }

    private void alterar() {
        try {
            Integer id = clienteView.getIdSelecionado();
            if (id == null) {
                clienteView.alertaErro("ALTERAR CLIENTE", "Selecione um cliente na tabela.");
                return;
            }

            Cliente alterarPorId = clienteService.buscarPorId(id);
            if (alterarPorId == null) {
                clienteView.alertaErro("ALTERAR CLIENTE", "Ops! Cliente inexistente");
                return;
            }

            Cliente alterarCliente = new Cliente (alterarPorId.getId(), alterarPorId.getNome(), alterarPorId.getCpf(), alterarPorId.getDataNasc(), alterarPorId.getNomeFazenda(), alterarPorId.getEndereco());
            while (true) {
                try {
                    alterarCliente = clienteView.informarDados("ALTERAR CLIENTE", alterarCliente);
                    if (alterarCliente == null) return;

                    if (clienteService.alterar(alterarCliente)) {
                        clienteView.alertaSucesso("ALTERAR CLIENTE", "Cliente Alterado com Sucesso");
                        aoAlterarDados.run();
                    } else {
                        clienteView.alertaErro("ALTERAR CLIENTE", "Ops! Erro ao Alterar cliente");
                    }
                    return;
                } catch (ClienteException e) {
                    clienteView.alertaErro("ALTERAR CLIENTE", e.getMessage());
                }
            }
        } catch (Exception e) {
            clienteView.alertaErro("ALTERAR CLIENTE", e.getMessage());
        }
    }
}
