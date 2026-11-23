package tests;

import models.Cliente;
import services.ClienteService;

public class ClienteTest {

    private final ClienteService clienteService = new ClienteService();

    // Cliente criado no teste de cadastro (usado pelos outros testes)
    private Cliente cliente;

    public Cliente getCliente() {
        return cliente;
    }

    public void iniciar() {
        System.out.println("=== TESTES CLIENTE ===");
        cadastrar();
        listar();
        alterar();
    }

    // Teste automatizado da funcionalidade de cadastro
    public void cadastrar() {
        try {
            cliente = new Cliente("Teste", 12345678900L, "17/10/1998", "Fazenda Teste", "Rua Teste");
            Boolean cadastrou = clienteService.cadastrar(cliente);
            if (cadastrou && cliente.getId() != null) {
                System.out.println("Cadastrar: Passou");
            } else {
                System.err.println("Cadastrar: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Cadastrar: Falhou - " + e.getMessage());
        }
    }

    // Teste automatizado da funcionalidade de listagem
    public void listar() {
        try {
            String lista = clienteService.listar();
            if (!lista.isEmpty() && lista.contains("Fazenda Teste")) {
                System.out.println("Listar: Passou");
            } else {
                System.err.println("Listar: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Listar: Falhou - " + e.getMessage());
        }
    }

    // Teste automatizado da funcionalidade de alteração
    public void alterar() {
        try {
            Cliente alterado = new Cliente(cliente.getId(), "Teste2", 98765432100L, "01/01/2000", "Fazenda Teste2", "Rua Teste2");
            Boolean alterou = clienteService.alterar(alterado);
            Cliente conferir = clienteService.buscarPorId(cliente.getId());
            if (alterou && conferir != null && conferir.getNome().equals("Teste2")) {
                cliente = conferir;
                System.out.println("Alterar: Passou");
            } else {
                System.err.println("Alterar: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Alterar: Falhou - " + e.getMessage());
        }
    }

    // Teste automatizado da funcionalidade de exclusão
    // (chamado por último, depois de excluir o gado do cliente)
    public void excluir() {
        try {
            Boolean excluiu = clienteService.excluir(cliente.getId());
            if (excluiu && clienteService.buscarPorId(cliente.getId()) == null) {
                System.out.println("Excluir Cliente: Passou");
            } else {
                System.err.println("Excluir Cliente: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Excluir Cliente: Falhou - " + e.getMessage());
        }
    }

}
