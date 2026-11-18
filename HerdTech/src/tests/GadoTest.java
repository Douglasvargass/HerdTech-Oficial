package tests;

import models.Cliente;
import models.Gado;
import services.GadoService;

public class GadoTest {

    private final GadoService gadoService = new GadoService();

    // Dono do animal (cliente criado no ClienteTest)
    private final Cliente dono;

    // Gado criado no teste de cadastro (usado pelos outros testes)
    private Gado gado;

    public GadoTest(Cliente dono) {
        this.dono = dono;
    }

    public Gado getGado() {
        return gado;
    }

    public void iniciar() {
        System.out.println("=== TESTES GADO ===");
        cadastrar();
        listar();
        listarGado();
        alterar();
    }

    // Teste automatizado da funcionalidade de cadastro
    public void cadastrar() {
        try {
            gado = new Gado(dono, "Mimosa Teste", "Nelore", "Femea", "05/06/2025");
            Boolean cadastrou = gadoService.cadastrar(gado);
            if (cadastrou && gado.getId() != null) {
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
            String lista = gadoService.listar();
            if (!lista.isEmpty() && lista.contains("Mimosa Teste")) {
                System.out.println("Listar: Passou");
            } else {
                System.err.println("Listar: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Listar: Falhou - " + e.getMessage());
        }
    }

    // Teste automatizado da listagem simplificada (sem os dados do dono)
    public void listarGado() {
        try {
            String lista = gadoService.listarGado();
            if (!lista.isEmpty() && lista.contains("Mimosa Teste")) {
                System.out.println("Listar Gado: Passou");
            } else {
                System.err.println("Listar Gado: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Listar Gado: Falhou - " + e.getMessage());
        }
    }

    // Teste automatizado da funcionalidade de alteração
    public void alterar() {
        try {
            Gado alterado = new Gado(gado.getId(), dono, "Mumu Teste", "Jersey", "Femea", "21/12/2025");
            Boolean alterou = gadoService.alterar(alterado);
            Gado conferir = gadoService.buscarPorId(gado.getId());
            if (alterou && conferir != null && conferir.getNome().equals("Mumu Teste")) {
                gado = conferir;
                System.out.println("Alterar: Passou");
            } else {
                System.err.println("Alterar: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Alterar: Falhou - " + e.getMessage());
        }
    }

    // Teste automatizado da funcionalidade de exclusão
    // (chamado depois de excluir as aplicações de vacina do animal)
    public void excluir() {
        try {
            Boolean excluiu = gadoService.excluir(gado.getId());
            if (excluiu && gadoService.buscarPorId(gado.getId()) == null) {
                System.out.println("Excluir Gado: Passou");
            } else {
                System.err.println("Excluir Gado: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Excluir Gado: Falhou - " + e.getMessage());
        }
    }

}
