package tests;

import models.Aplicacao;
import models.Gado;
import services.AplicacaoService;

public class AplicacaoTest {

    private final AplicacaoService aplicacaoService = new AplicacaoService();

    // Animal que recebe a vacina (gado criado no GadoTest)
    private final Gado gado;

    // Aplicação criada no teste de cadastro (usada pelos outros testes)
    private Aplicacao aplicacao;

    public AplicacaoTest(Gado gado) {
        this.gado = gado;
    }

    public void iniciar() {
        System.out.println("=== TESTES APLICAÇÃO (VACINA) ===");
        cadastrar();
        listar();
        alterar();
    }

    // Teste automatizado da funcionalidade de cadastro
    public void cadastrar() {
        try {
            aplicacao = new Aplicacao(gado.getDono(), gado, "Aftosa Teste", "10/01/2026", "10/07/2026", "Veterinário Teste");
            Boolean cadastrou = aplicacaoService.cadastrar(aplicacao);
            if (cadastrou && aplicacao.getId() != null) {
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
            String lista = aplicacaoService.listar();
            if (!lista.isEmpty() && lista.contains("Aftosa Teste")) {
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
            Aplicacao alterada = new Aplicacao(aplicacao.getId(), gado.getDono(), gado, "Brucelose Teste", "15/02/2026", "15/08/2026", "Dono Teste");
            Boolean alterou = aplicacaoService.alterar(alterada);
            Aplicacao conferir = aplicacaoService.buscarPorId(aplicacao.getId());
            if (alterou && conferir != null && conferir.getVacina().equals("Brucelose Teste")) {
                aplicacao = conferir;
                System.out.println("Alterar: Passou");
            } else {
                System.err.println("Alterar: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Alterar: Falhou - " + e.getMessage());
        }
    }

    // Teste automatizado da funcionalidade de exclusão
    public void excluir() {
        try {
            Boolean excluiu = aplicacaoService.excluir(aplicacao.getId());
            if (excluiu && aplicacaoService.buscarPorId(aplicacao.getId()) == null) {
                System.out.println("Excluir Aplicação: Passou");
            } else {
                System.err.println("Excluir Aplicação: Falhou");
            }
        } catch (Exception e) {
            System.err.println("Excluir Aplicação: Falhou - " + e.getMessage());
        }
    }
}
