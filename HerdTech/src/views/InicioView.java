package views;

import models.Aplicacao;
import models.Cliente;
import models.Gado;
import services.AplicacaoService;
import utils.Utilidade;
import views.componentes.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

// Tela inicial (Início): destaque, indicadores, próximas vacinas e composição do rebanho
public class InicioView extends JPanel {
    private final Botao botaoCadastrarAnimal = new Botao("Cadastrar Animal", Icones.gado(16, Color.WHITE), Botao.Estilo.PRIMARIO);
    private final Botao botaoCadastrarCliente = new Botao("Cadastrar Fazenda", Icones.local(16, Color.WHITE), Botao.Estilo.SECUNDARIO);
    private final Botao botaoAplicarVacina = new Botao("Aplicar Vacina", Icones.seringa(16, Tema.SECUNDARIA), Botao.Estilo.CONTORNO);
    private final Botao botaoVerVacinas = new Botao("Ver todas", Botao.Estilo.FANTASMA);

    private final Indicador indicadorClientes = new Indicador("Clientes", Icones.pessoa(22, Tema.PRIMARIA), Tema.PRIMARIA);
    private final Indicador indicadorFazendas = new Indicador("Fazendas", Icones.local(22, Tema.PRIMARIA), Tema.PRIMARIA);
    private final Indicador indicadorAnimais = new Indicador("Total de Animais", Icones.gado(24, Tema.PRIMARIA), Tema.PRIMARIA);
    private final Indicador indicadorVacinas = new Indicador("Total de Vacinas", Icones.seringa(22, Tema.AZUL), Tema.AZUL);
    private final Indicador indicadorPendentes = new Indicador("Doses Pendentes", Icones.calendario(22, Tema.LARANJA), Tema.LARANJA);

    private final JPanel listaProximas = new JPanel();
    private final GraficoBarras graficoRacas = new GraficoBarras(Tema.PRIMARIA);
    private final JLabel resumoGenero = Tema.textoSuave(" ", 13);

    public InicioView() {
        super(new BorderLayout());
        setBackground(Tema.FUNDO);

        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setBackground(Tema.FUNDO);
        conteudo.setBorder(new EmptyBorder(28, 36, 32, 36));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 22, 0);
        conteudo.add(montarDestaque(), c);

        c.gridy = 1;
        conteudo.add(montarIndicadores(), c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        conteudo.add(montarPaineis(), c);

        // Espaço que "empurra" o conteúdo para cima
        c.gridy = 3;
        c.weighty = 1;
        JPanel espaco = new JPanel();
        espaco.setOpaque(false);
        conteudo.add(espaco, c);

        JScrollPane rolagem = Tema.rolagem(conteudo);
        rolagem.getViewport().setBackground(Tema.FUNDO);
        add(rolagem, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------- Montagem

    private JComponent montarDestaque() {
        Cartao destaque = new Cartao(new BorderLayout(30, 0));
        destaque.setGradiente(Color.WHITE, Tema.HERO_FIM);
        destaque.setBorder(new EmptyBorder(36, 44, 38, 44));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        Etiqueta tag = new Etiqueta("TECNOLOGIA NO CAMPO", Tema.PRIMARIA);
        JLabel linha1 = Tema.titulo("Gestão inteligente", 36);
        JLabel linha2 = Tema.titulo("Tecnologia para um", 36);
        JLabel linha3 = Tema.titulo("rebanho mais produtivo", 36);
        linha3.setForeground(Tema.PRIMARIA);
        JLabel descricao = Tema.textoSuave("Controle completo do seu rebanho, fazendas e histórico sanitário em um só lugar.", 16);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botoes.setOpaque(false);
        for (Botao botao : new Botao[]{botaoCadastrarAnimal, botaoCadastrarCliente, botaoAplicarVacina}) {
            botao.setFont(Tema.fonte(Font.BOLD, 14));
            botao.setBorder(new EmptyBorder(13, 22, 13, 22));
            botoes.add(botao);
            botoes.add(Box.createHorizontalStrut(12));
        }

        for (JComponent item : new JComponent[]{tag, linha1, linha2, linha3, descricao, botoes}) {
            item.setAlignmentX(LEFT_ALIGNMENT);
        }
        textos.add(tag);
        textos.add(Box.createVerticalStrut(18));
        textos.add(linha1);
        textos.add(linha2);
        textos.add(linha3);
        textos.add(Box.createVerticalStrut(14));
        textos.add(descricao);
        textos.add(Box.createVerticalStrut(26));
        textos.add(botoes);

        // Lado direito: logo + selo "Mais Lucro"
        JPanel lado = new JPanel(new GridBagLayout());
        lado.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        lado.add(Tema.logo(170), c);
        c.gridy = 1;
        c.insets = new Insets(18, 0, 0, 0);
        lado.add(montarSeloLucro(), c);

        destaque.add(textos, BorderLayout.CENTER);
        destaque.add(lado, BorderLayout.EAST);
        return destaque;
    }

    private JComponent montarSeloLucro() {
        JPanel selo = new JPanel(new GridLayout(0, 1, 0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.suavizar(g2);
                g2.setColor(Tema.SECUNDARIA);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        selo.setOpaque(false);
        selo.setBorder(new EmptyBorder(12, 22, 12, 22));

        JLabel produtividade = new JLabel("+ Produtividade");
        JLabel custo = new JLabel("−  Custo");
        JLabel lucro = new JLabel("=  Mais Lucro");
        for (JLabel rotulo : new JLabel[]{produtividade, custo}) {
            rotulo.setFont(Tema.fonte(Font.PLAIN, 13));
            rotulo.setForeground(new Color(255, 255, 255, 210));
        }
        lucro.setFont(Tema.fonte(Font.BOLD, 15));
        lucro.setForeground(new Color(0x2ECC71));
        lucro.setBorder(new MatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 60)));

        selo.add(produtividade);
        selo.add(custo);
        selo.add(lucro);
        return selo;
    }

    private JComponent montarIndicadores() {
        JPanel linha = new JPanel(new GridLayout(1, 5, 16, 0));
        linha.setOpaque(false);
        linha.add(indicadorAnimais);
        linha.add(indicadorFazendas);
        linha.add(indicadorClientes);
        linha.add(indicadorVacinas);
        linha.add(indicadorPendentes);
        return linha;
    }

    private JComponent montarPaineis() {
        JPanel linha = new JPanel(new GridLayout(1, 2, 16, 0));
        linha.setOpaque(false);

        // Próximas vacinações
        Cartao proximas = new Cartao(new BorderLayout(0, 12));
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(cabecalhoPainel("Próximas Vacinações", "Doses atrasadas e dos próximos " + SituacaoVacina.DIAS_EM_BREVE + " dias"), BorderLayout.WEST);
        botaoVerVacinas.setBorder(new EmptyBorder(6, 10, 6, 10));
        topo.add(botaoVerVacinas, BorderLayout.EAST);
        listaProximas.setOpaque(false);
        listaProximas.setLayout(new BoxLayout(listaProximas, BoxLayout.Y_AXIS));
        proximas.add(topo, BorderLayout.NORTH);
        proximas.add(listaProximas, BorderLayout.CENTER);

        // Composição do rebanho
        Cartao composicao = new Cartao(new BorderLayout(0, 12));
        composicao.add(cabecalhoPainel("Composição do Rebanho", "Quantidade de animais por raça"), BorderLayout.NORTH);
        composicao.add(graficoRacas, BorderLayout.CENTER);
        composicao.add(resumoGenero, BorderLayout.SOUTH);

        linha.add(proximas);
        linha.add(composicao);
        return linha;
    }

    static JComponent cabecalhoPainel(String titulo, String subtitulo) {
        JPanel cabecalho = new JPanel(new GridLayout(2, 1, 0, 2));
        cabecalho.setOpaque(false);
        cabecalho.add(Tema.titulo(titulo, 17));
        cabecalho.add(Tema.textoSuave(subtitulo, 13));
        return cabecalho;
    }

    // ---------------------------------------------------------------- Dados

    public void atualizar(List<Cliente> clientes, List<Gado> gados, List<Aplicacao> aplicacoes) {
        Set<String> fazendas = new HashSet<>();
        for (Cliente cliente : clientes) fazendas.add(cliente.getNomeFazenda().trim().toLowerCase());

        List<Aplicacao> pendentes = pendentes(aplicacoes);
        long atrasadas = pendentes.stream()
                .filter(a -> SituacaoVacina.de(a.getProximaVacina()) == SituacaoVacina.ATRASADA).count();

        indicadorAnimais.definir(gados.size(), "Geral", Tema.PRIMARIA);
        indicadorFazendas.definir(fazendas.size(), "Ativas", Tema.PRIMARIA);
        indicadorClientes.definir(clientes.size(), "Produtores", Tema.PRIMARIA);
        indicadorVacinas.definir(aplicacoes.size(), "Aplicadas", Tema.AZUL);
        if (atrasadas > 0) {
            indicadorPendentes.definir(pendentes.size(), atrasadas + (atrasadas == 1 ? " atrasada" : " atrasadas"), Tema.VERMELHO);
        } else {
            indicadorPendentes.definir(pendentes.size(), "Próximos " + SituacaoVacina.DIAS_EM_BREVE + " dias", Tema.LARANJA);
        }

        // Lista de próximas vacinas (até 6)
        listaProximas.removeAll();
        if (pendentes.isEmpty()) {
            JLabel vazio = Tema.textoSuave("Nenhuma dose pendente. Rebanho em dia!", 14);
            vazio.setBorder(new EmptyBorder(18, 2, 18, 0));
            listaProximas.add(vazio);
        } else {
            for (int i = 0; i < Math.min(6, pendentes.size()); i++) {
                listaProximas.add(itemVacina(pendentes.get(i), i > 0));
            }
        }

        // Raças e gênero
        Map<String, Integer> racas = new HashMap<>();
        int femeas = 0, machos = 0;
        for (Gado gado : gados) {
            racas.merge(gado.getRaca().trim(), 1, Integer::sum);
            String genero = gado.getGenero().trim().toLowerCase();
            if (genero.startsWith("f")) femeas++;
            else if (genero.startsWith("m")) machos++;
        }
        graficoRacas.definir(racas);
        resumoGenero.setText(gados.isEmpty() ? " " : "Fêmeas: " + femeas + "   •   Machos: " + machos);

        revalidate();
        repaint();
    }

    // Doses (mais recentes de cada animal/vacina) que exigem atenção, da mais urgente para a menos
    public static List<Aplicacao> pendentes(List<Aplicacao> aplicacoes) {
        List<Aplicacao> lista = new ArrayList<>();
        for (Aplicacao aplicacao : AplicacaoService.ultimasDoses(aplicacoes)) {
            if (SituacaoVacina.de(aplicacao.getProximaVacina()).exigeAtencao()) lista.add(aplicacao);
        }
        lista.sort(Comparator.comparing(a -> Utilidade.converterData(a.getProximaVacina()),
                Comparator.nullsLast(Comparator.<LocalDate>naturalOrder())));
        return lista;
    }

    private JComponent itemVacina(Aplicacao aplicacao, boolean comLinha) {
        SituacaoVacina situacao = SituacaoVacina.de(aplicacao.getProximaVacina());

        JPanel item = new JPanel(new BorderLayout(14, 0));
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(10, 0, 10, 0));
        if (comLinha) {
            item.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 0, 0, Tema.BORDA), new EmptyBorder(10, 0, 10, 0)));
        }

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 1));
        textos.setOpaque(false);
        JLabel vacina = new JLabel(aplicacao.getVacina());
        vacina.setFont(Tema.fonte(Font.BOLD, 14));
        vacina.setForeground(Tema.TEXTO);
        textos.add(vacina);
        textos.add(Tema.textoSuave(aplicacao.getGado().getNome() + "  •  " + aplicacao.getDono().getNomeFazenda(), 12));

        JPanel direita = new JPanel();
        direita.setOpaque(false);
        direita.setLayout(new BoxLayout(direita, BoxLayout.Y_AXIS));
        Etiqueta etiqueta = new Etiqueta(situacao.getRotulo(), situacao.getCor());
        JLabel data = Tema.textoSuave(aplicacao.getProximaVacina() + " (" + SituacaoVacina.prazo(aplicacao.getProximaVacina()) + ")", 12);
        etiqueta.setAlignmentX(RIGHT_ALIGNMENT);
        data.setAlignmentX(RIGHT_ALIGNMENT);
        direita.add(etiqueta);
        direita.add(Box.createVerticalStrut(2));
        direita.add(data);

        item.add(Icones.bloco(Icones.seringa(18, situacao.getCor()), situacao.getCor(), 40), BorderLayout.WEST);
        item.add(textos, BorderLayout.CENTER);
        item.add(direita, BorderLayout.EAST);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, item.getPreferredSize().height));
        item.setAlignmentX(LEFT_ALIGNMENT);
        return item;
    }

    // ---------------------------------------------------------------- Botões (ligados pelo Controller)

    public void aoCadastrarAnimal(Runnable acao) {
        botaoCadastrarAnimal.addActionListener(e -> acao.run());
    }

    public void aoCadastrarCliente(Runnable acao) {
        botaoCadastrarCliente.addActionListener(e -> acao.run());
    }

    public void aoAplicarVacina(Runnable acao) {
        botaoAplicarVacina.addActionListener(e -> acao.run());
    }

    public void aoVerVacinas(Runnable acao) {
        botaoVerVacinas.addActionListener(e -> acao.run());
    }

    // ---------------------------------------------------------------- Cartão de indicador (KPI)

    private static class Indicador extends Cartao {
        private final JLabel valor = new JLabel("0");
        private final Etiqueta sub = new Etiqueta(" ", Tema.PRIMARIA);

        Indicador(String rotulo, Icon icone, Color cor) {
            super(new BorderLayout(14, 0));
            setBorder(new EmptyBorder(18, 18, 20, 18));

            valor.setFont(Tema.fonte(Font.BOLD, 28));
            valor.setForeground(Tema.SECUNDARIA);

            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            JLabel titulo = Tema.textoSuave(rotulo, 13);
            for (JComponent c : new JComponent[]{titulo, valor, sub}) c.setAlignmentX(LEFT_ALIGNMENT);
            textos.add(titulo);
            textos.add(valor);
            textos.add(Box.createVerticalStrut(2));
            textos.add(sub);

            JPanel colunaIcone = new JPanel(new BorderLayout());
            colunaIcone.setOpaque(false);
            colunaIcone.add(Icones.bloco(icone, cor, 48), BorderLayout.NORTH);

            add(colunaIcone, BorderLayout.WEST);
            add(textos, BorderLayout.CENTER);
        }

        void definir(long quantidade, String texto, Color cor) {
            valor.setText(String.valueOf(quantidade));
            sub.definir(texto, cor);
        }
    }
}
