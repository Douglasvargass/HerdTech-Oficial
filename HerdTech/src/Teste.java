import tests.AplicacaoTest;
import tests.ClienteTest;
import tests.GadoTest;

public class Teste {
    public static void main(String[] args) {
        System.out.println("=== TESTES HERDTECH ===");

        // Testes com o banco: Cliente -> Gado -> Aplicação
        // (cada um depende do registro criado pelo anterior)
        ClienteTest clienteTest = new ClienteTest();
        clienteTest.iniciar();
        if (clienteTest.getCliente() == null || clienteTest.getCliente().getId() == null) {
            System.err.println("Cliente não foi cadastrado, testes de Gado e Aplicação cancelados.");
            return;
        }

        GadoTest gadoTest = new GadoTest(clienteTest.getCliente());
        gadoTest.iniciar();
        if (gadoTest.getGado() == null || gadoTest.getGado().getId() == null) {
            System.err.println("Gado não foi cadastrado, testes de Aplicação cancelados.");
            clienteTest.excluir();
            return;
        }

        AplicacaoTest aplicacaoTest = new AplicacaoTest(gadoTest.getGado());
        aplicacaoTest.iniciar();

        // Exclusão na ordem inversa (o banco não deixa excluir quem tem registros ligados)
        System.out.println("=== TESTES EXCLUSÃO ===");
        aplicacaoTest.excluir();
        gadoTest.excluir();
        clienteTest.excluir();
    }
}
