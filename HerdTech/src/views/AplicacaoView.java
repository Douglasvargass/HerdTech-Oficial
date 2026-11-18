package views;

import models.Aplicacao;
import models.Gado;
import services.AplicacaoService;
import utils.Utilidade;
import views.componentes.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public class AplicacaoView extends TelaLista {
    private static final String[] VACINAS = {"Febre Aftosa", "Brucelose", "Raiva", "Clostridiose", "Leptospirose",
            "IBR / BVD", "Carbúnculo", "Botulismo"};
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AplicacaoView() {
        super("Vacinação",
                "Histórico sanitário do rebanho e agenda das próximas doses.",
                "Aplicar Vacina",
                "Nenhuma vacina registrada ainda. Clique em \"Aplicar Vacina\" para começar.",
                new String[]{"ID", "Animal", "Dono", "Vacina", "Aplicada em", "Próxima dose", "Prazo", "Aplicado por", "Situação"});
        larguras(70, 130, 150, 140, 105, 110, 100, 140, 110);
        usarRenderizador(8, new Etiqueta());
    }

    public void atualizarTabela(List<Aplicacao> aplicacoes) {
        // Ids das doses mais recentes de cada animal/vacina
        Set<Integer> ultimas = new HashSet<>();
        for (Aplicacao aplicacao : AplicacaoService.ultimasDoses(aplicacoes)) ultimas.add(aplicacao.getId());

        List<Object[]> linhas = new ArrayList<>();
        for (Aplicacao aplicacao : aplicacoes) {
            boolean renovada = !ultimas.contains(aplicacao.getId());
            SituacaoVacina situacao = renovada ? SituacaoVacina.RENOVADA : SituacaoVacina.de(aplicacao.getProximaVacina());
            linhas.add(new Object[]{
                    aplicacao.getId(),
                    aplicacao.getGado().getNome(),
                    aplicacao.getDono().getNome(),
                    aplicacao.getVacina(),
                    aplicacao.getDataAplicacao(),
                    aplicacao.getProximaVacina(),
                    renovada ? "-" : SituacaoVacina.prazo(aplicacao.getProximaVacina()),
                    aplicacao.getQuemAplicou(),
                    situacao
            });
        }
        preencher(linhas);
    }

    // Abre o formulário com os dados da aplicação. Retorna null se o usuário cancelar.
    public Aplicacao informarDados(String titulo, Aplicacao aplicacao, List<Gado> gados) {
        boolean nova = aplicacao.getId() == null;
        Formulario formulario = new Formulario(this,
                nova ? "Registrar Aplicação de Vacina" : "Alterar Aplicação",
                nova ? "Escolha o animal e informe a vacina aplicada." : "Aplicação #" + aplicacao.getId(),
                Icones.seringa(22, Tema.PRIMARIA));

        JComboBox<Gado> gado = Tema.estilizarCombo(new JComboBox<>(gados.toArray(new Gado[0])));
        gado.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean selecionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
                if (valor instanceof Gado) {
                    Gado item = (Gado) valor;
                    setText("#" + item.getId() + "  " + item.getNome() + " (" + item.getRaca() + ")  —  " + item.getDono().getNome());
                }
                setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                return this;
            }
        });
        if (aplicacao.getGado() != null) gado.setSelectedItem(aplicacao.getGado());
        // Na alteração o animal não muda (igual ao sistema anterior)
        gado.setEnabled(nova);

        JComboBox<String> vacina = Tema.estilizarCombo(new JComboBox<>(VACINAS));
        vacina.setEditable(true);
        vacina.getEditor().setItem(aplicacao.getVacina() == null ? "" : aplicacao.getVacina());

        String hoje = LocalDate.now().format(FORMATO);
        JFormattedTextField dataAplicacao = Tema.campoMascara("##/##/####",
                nova && aplicacao.getDataAplicacao() == null ? hoje : aplicacao.getDataAplicacao());
        JFormattedTextField proximaVacina = Tema.campoMascara("##/##/####", aplicacao.getProximaVacina());
        JTextField quemAplicou = Tema.campoTexto(aplicacao.getQuemAplicou());

        // Atalhos para calcular a próxima dose a partir da data da aplicação
        JPanel atalhos = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        atalhos.setOpaque(false);
        String[] rotulos = {"+30 dias", "+3 meses", "+6 meses", "+1 ano"};
        List<UnaryOperator<LocalDate>> somas = Arrays.asList(
                d -> d.plusDays(30), d -> d.plusMonths(3), d -> d.plusMonths(6), d -> d.plusYears(1));
        for (int i = 0; i < rotulos.length; i++) {
            Botao atalho = new Botao(rotulos[i], Botao.Estilo.CONTORNO);
            atalho.setFont(Tema.fonte(Font.BOLD, 12));
            atalho.setBorder(new EmptyBorder(6, 12, 6, 12));
            UnaryOperator<LocalDate> soma = somas.get(i);
            atalho.addActionListener(e -> {
                LocalDate base = Utilidade.converterData(Tema.textoMascara(dataAplicacao));
                if (base == null) base = LocalDate.now();
                proximaVacina.setValue(soma.apply(base).format(FORMATO));
            });
            atalhos.add(atalho);
            atalhos.add(Box.createHorizontalStrut(8));
        }

        formulario.adicionarCampo("Animal", gado);
        formulario.adicionarCampo("Vacina", vacina, "Escolha da lista ou digite o nome");
        formulario.adicionarLinha("Data da Aplicação", dataAplicacao, "Próxima Dose", proximaVacina);
        formulario.adicionarCampo("Calcular próxima dose", atalhos);
        formulario.adicionarCampo("Quem aplicou?", quemAplicou, "Veterinário ou Dono");

        boolean salvar = formulario.exibir();
        formulario.dispose();
        if (!salvar) return null;

        Gado escolhido = (Gado) gado.getSelectedItem();
        Object vacinaDigitada = vacina.getEditor().getItem();

        aplicacao.setGado(escolhido);
        aplicacao.setDono(escolhido == null ? null : escolhido.getDono());
        aplicacao.setVacina(vacinaDigitada == null ? "" : vacinaDigitada.toString().trim());
        aplicacao.setDataAplicacao(Tema.textoMascara(dataAplicacao));
        aplicacao.setProximaVacina(Tema.textoMascara(proximaVacina));
        aplicacao.setQuemAplicou(quemAplicou.getText().trim());

        return aplicacao;
    }
}
