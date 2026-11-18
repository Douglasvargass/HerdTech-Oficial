package views;

import models.Aplicacao;
import models.Cliente;
import models.Gado;
import services.AplicacaoService;
import views.componentes.Cartao;
import views.componentes.GraficoBarras;
import views.componentes.SituacaoVacina;
import views.componentes.Tema;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Tela de Relatórios: gráficos gerados a partir dos dados do banco
public class RelatorioView extends JPanel {
    private final GraficoBarras animaisPorFazenda = new GraficoBarras(Tema.PRIMARIA);
    private final GraficoBarras situacaoSanitaria = new GraficoBarras(Tema.PRIMARIA);
    private final GraficoBarras racas = new GraficoBarras(Tema.SECUNDARIA);
    private final GraficoBarras vacinasAplicadas = new GraficoBarras(Tema.AZUL);

    public RelatorioView() {
        super(new BorderLayout());
        setBackground(Tema.FUNDO);

        JPanel conteudo = new JPanel(new BorderLayout(0, 20));
        conteudo.setBackground(Tema.FUNDO);
        conteudo.setBorder(new EmptyBorder(28, 36, 32, 36));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Tema.titulo("Relatórios", 26));
        textos.add(Box.createVerticalStrut(4));
        textos.add(Tema.textoSuave("Visão geral do rebanho e da situação sanitária.", 14));

        JPanel grade = new JPanel(new GridLayout(2, 2, 16, 16));
        grade.setOpaque(false);
        grade.add(painel("Animais por Fazenda", "Distribuição do rebanho entre as propriedades", animaisPorFazenda));
        grade.add(painel("Situação Sanitária", "Dose mais recente de cada animal / vacina", situacaoSanitaria));
        grade.add(painel("Composição por Raça", "Quantidade de animais de cada raça", racas));
        grade.add(painel("Vacinas mais Aplicadas", "Total de aplicações registradas por vacina", vacinasAplicadas));

        JPanel corpo = new JPanel(new BorderLayout());
        corpo.setOpaque(false);
        corpo.add(grade, BorderLayout.NORTH);

        conteudo.add(textos, BorderLayout.NORTH);
        conteudo.add(corpo, BorderLayout.CENTER);

        JScrollPane rolagem = Tema.rolagem(conteudo);
        rolagem.getViewport().setBackground(Tema.FUNDO);
        add(rolagem, BorderLayout.CENTER);
    }

    private JComponent painel(String titulo, String subtitulo, GraficoBarras grafico) {
        Cartao cartao = new Cartao(new BorderLayout(0, 16));
        cartao.add(InicioView.cabecalhoPainel(titulo, subtitulo), BorderLayout.NORTH);
        cartao.add(grafico, BorderLayout.CENTER);
        return cartao;
    }

    public void atualizar(List<Cliente> clientes, List<Gado> gados, List<Aplicacao> aplicacoes) {
        // Animais por fazenda (inclui fazendas ainda sem animais)
        Map<String, Integer> porFazenda = new HashMap<>();
        for (Cliente cliente : clientes) porFazenda.putIfAbsent(cliente.getNomeFazenda().trim(), 0);
        for (Gado gado : gados) porFazenda.merge(gado.getDono().getNomeFazenda().trim(), 1, Integer::sum);
        animaisPorFazenda.definir(porFazenda);

        // Situação sanitária (na ordem de urgência, com as cores de status)
        Map<SituacaoVacina, Integer> situacoes = new HashMap<>();
        for (Aplicacao aplicacao : AplicacaoService.ultimasDoses(aplicacoes)) {
            situacoes.merge(SituacaoVacina.de(aplicacao.getProximaVacina()), 1, Integer::sum);
        }
        situacaoSanitaria.limpar();
        for (SituacaoVacina situacao : SituacaoVacina.values()) {
            if (situacoes.containsKey(situacao)) {
                situacaoSanitaria.adicionar(situacao.getRotulo(), situacoes.get(situacao), situacao.getCor());
            }
        }
        situacaoSanitaria.atualizar();

        // Raças
        Map<String, Integer> porRaca = new HashMap<>();
        for (Gado gado : gados) porRaca.merge(gado.getRaca().trim(), 1, Integer::sum);
        racas.definir(porRaca);

        // Vacinas
        Map<String, Integer> porVacina = new HashMap<>();
        for (Aplicacao aplicacao : aplicacoes) porVacina.merge(aplicacao.getVacina().trim(), 1, Integer::sum);
        vacinasAplicadas.definir(porVacina);

        revalidate();
        repaint();
    }
}
