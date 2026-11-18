package controllers;

import models.Aplicacao;
import models.Cliente;
import models.Gado;
import services.AplicacaoService;
import services.ClienteService;
import services.GadoService;
import views.*;
import views.componentes.Alerta;
import views.componentes.Icones;

import java.util.List;

public class HerdTechController {
    // Ids das telas
    private static final String INICIO = "inicio";
    private static final String CLIENTES = "clientes";
    private static final String ANIMAIS = "animais";
    private static final String VACINAS = "vacinas";
    private static final String RELATORIOS = "relatorios";

    private final HerdTechView herdTechView = new HerdTechView();
    private final InicioView inicioView = new InicioView();
    private final ClienteView clienteView = new ClienteView();
    private final GadoView gadoView = new GadoView();
    private final AplicacaoView aplicacaoView = new AplicacaoView();
    private final RelatorioView relatorioView = new RelatorioView();

    private final ClienteService clienteService = new ClienteService();
    private final GadoService gadoService = new GadoService();
    private final AplicacaoService aplicacaoService = new AplicacaoService();

    private final ClienteController clienteController = new ClienteController(clienteView, this::atualizarDados);
    private final GadoController gadoController = new GadoController(gadoView, this::atualizarDados);
    private final AplicacaoController aplicacaoController = new AplicacaoController(aplicacaoView, this::atualizarDados);

    private boolean erroJaExibido;

    public void iniciar() {
        // Menu (mesmas abas do Front)
        herdTechView.adicionarTela(INICIO, "Início", cor -> Icones.inicio(17, cor), inicioView);
        herdTechView.adicionarTela(CLIENTES, "Fazendas", cor -> Icones.local(17, cor), clienteView);
        herdTechView.adicionarTela(ANIMAIS, "Animais", cor -> Icones.gado(18, cor), gadoView);
        herdTechView.adicionarTela(VACINAS, "Vacinação", cor -> Icones.seringa(17, cor), aplicacaoView);
        herdTechView.adicionarTela(RELATORIOS, "Relatórios", cor -> Icones.grafico(17, cor), relatorioView);

        // Atalhos da tela inicial
        inicioView.aoCadastrarAnimal(() -> {
            herdTechView.mostrarTela(ANIMAIS);
            gadoController.cadastrar();
        });
        inicioView.aoCadastrarCliente(() -> {
            herdTechView.mostrarTela(CLIENTES);
            clienteController.cadastrar();
        });
        inicioView.aoAplicarVacina(() -> {
            herdTechView.mostrarTela(VACINAS);
            aplicacaoController.cadastrar();
        });
        inicioView.aoVerVacinas(() -> herdTechView.mostrarTela(VACINAS));
        herdTechView.aoClicarNotificacoes(() -> herdTechView.mostrarTela(VACINAS));

        herdTechView.setVisible(true);
        atualizarDados();
    }

    // Busca tudo no banco e atualiza todas as telas
    public void atualizarDados() {
        try {
            List<Cliente> clientes = clienteService.listarTodos();
            List<Gado> gados = gadoService.listarTodos();
            List<Aplicacao> aplicacoes = aplicacaoService.listarTodos();

            clienteView.atualizarTabela(clientes);
            gadoView.atualizarTabela(gados);
            aplicacaoView.atualizarTabela(aplicacoes);
            inicioView.atualizar(clientes, gados, aplicacoes);
            relatorioView.atualizar(clientes, gados, aplicacoes);
            herdTechView.setNotificacoes(InicioView.pendentes(aplicacoes).size());

            herdTechView.setStatusBanco(true, "Conectado ao banco de dados  •  "
                    + clientes.size() + " clientes, " + gados.size() + " animais, " + aplicacoes.size() + " vacinas");
            erroJaExibido = false;
        } catch (Exception e) {
            herdTechView.setStatusBanco(false, "Sem conexão com o banco de dados  •  " + e.getMessage());
            // Mostra o aviso uma vez só (evita várias janelas de erro seguidas)
            if (!erroJaExibido) {
                erroJaExibido = true;
                Alerta.erro(herdTechView, "BANCO DE DADOS",
                        "Não foi possível carregar os dados.\n\n" + e.getMessage()
                                + "\n\nVerifique se o PostgreSQL está rodando e se o banco 'herdtech' foi criado (database/script.sql).");
            }
        }
    }
}
