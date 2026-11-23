package views;

import models.RegistroAuditoria;
import dao.AuditoriaDAO;
import views.componentes.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Tela de Auditoria: histórico de inclusões, alterações, exclusões e consultas (somente leitura)
public class AuditoriaView extends JPanel {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String TODAS = "Todas";
    private static final String[] COLUNAS = {"ID", "Data/Hora", "Máquina", "Usuário", "Ação", "Entidade", "Registro", "Detalhes"};

    private final DefaultTableModel modelo = new DefaultTableModel(COLUNAS, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int coluna) {
            return (coluna == 0 || coluna == 6) ? Integer.class : String.class;
        }
    };
    private final JTable tabela = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> ordenador = new TableRowSorter<>(modelo);
    private final CampoBusca campoBusca = new CampoBusca("Pesquisar no log...");
    private final JComboBox<String> filtroAcao = Tema.estilizarCombo(new JComboBox<>(new String[]{
            TODAS, AuditoriaDAO.INCLUSAO, AuditoriaDAO.ALTERACAO, AuditoriaDAO.EXCLUSAO, AuditoriaDAO.CONSULTA}));
    private final JComboBox<String> filtroEntidade = Tema.estilizarCombo(new JComboBox<>(new String[]{
            TODAS, "Cliente", "Animal", "Vacina"}));
    private final JLabel contador = Tema.textoSuave("", 13);
    private final JTextArea detalhes = new JTextArea(3, 20);
    private final Botao botaoAtualizar = new Botao("Atualizar", Icones.atualizar(15, Tema.PRIMARIA), Botao.Estilo.FANTASMA);
    private final Botao botaoExportar = new Botao("Exportar CSV", Botao.Estilo.CONTORNO);

    public AuditoriaView() {
        super(new BorderLayout(0, 20));
        setBackground(Tema.FUNDO);
        setBorder(new EmptyBorder(28, 36, 28, 36));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Tema.titulo("Auditoria", 26));
        textos.add(Box.createVerticalStrut(4));
        textos.add(Tema.textoSuave("Histórico de tudo que foi incluído, alterado, excluído e consultado no sistema.", 14));

        Tema.estilizarTabela(tabela);
        tabela.setRowSorter(ordenador);
        tabela.setRowHeight(36);
        int[] larguras = {60, 150, 130, 110, 110, 100, 80, 560};
        for (int i = 0; i < larguras.length; i++) tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        tabela.getColumnModel().getColumn(4).setCellRenderer(new RenderizadorAcao());
        tabela.getColumnModel().getColumn(7).setCellRenderer(new Tema.RenderizadorCelula() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int l, int c) {
                // Na tabela os detalhes ficam em uma linha; o texto completo aparece no painel de baixo
                return super.getTableCellRendererComponent(t, valor == null ? "" : valor.toString().replace("\n", "  |  "), sel, foco, l, c);
            }
        });
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        campoBusca.setPreferredSize(new Dimension(280, 40));
        filtroAcao.setPreferredSize(new Dimension(140, 40));
        filtroEntidade.setPreferredSize(new Dimension(120, 40));

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        esquerda.setOpaque(false);
        esquerda.add(campoBusca);
        esquerda.add(filtroAcao);
        esquerda.add(filtroEntidade);
        esquerda.add(contador);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        direita.setOpaque(false);
        direita.add(botaoAtualizar);
        direita.add(botaoExportar);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(new EmptyBorder(0, 0, 16, 0));
        barra.add(esquerda, BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);

        JScrollPane rolagem = Tema.rolagem(tabela);
        rolagem.setBorder(new MatteBorder(1, 1, 1, 1, Tema.BORDA));

        detalhes.setEditable(false);
        detalhes.setLineWrap(true);
        detalhes.setWrapStyleWord(true);
        detalhes.setFont(Tema.fonte(Font.PLAIN, 13));
        detalhes.setForeground(Tema.TEXTO);
        detalhes.setBackground(Tema.LINHA_ALTERNADA);
        detalhes.setBorder(new EmptyBorder(10, 12, 10, 12));
        detalhes.setText("Selecione uma linha para ver os detalhes completos (antes e depois de cada alteração).");
        JScrollPane rolagemDetalhes = new JScrollPane(detalhes);
        rolagemDetalhes.setBorder(new MatteBorder(1, 1, 1, 1, Tema.BORDA));
        rolagemDetalhes.setPreferredSize(new Dimension(0, 90));

        Cartao cartao = new Cartao(new BorderLayout(0, 12));
        cartao.add(barra, BorderLayout.NORTH);
        cartao.add(rolagem, BorderLayout.CENTER);
        cartao.add(rolagemDetalhes, BorderLayout.SOUTH);

        add(textos, BorderLayout.NORTH);
        add(cartao, BorderLayout.CENTER);

        DocumentListener ouvinte = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        };
        campoBusca.getDocument().addDocumentListener(ouvinte);
        filtroAcao.addActionListener(e -> filtrar());
        filtroEntidade.addActionListener(e -> filtrar());
        botaoExportar.addActionListener(e -> exportar());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            int linha = tabela.getSelectedRow();
            if (linha >= 0) {
                Object texto = modelo.getValueAt(tabela.convertRowIndexToModel(linha), 7);
                detalhes.setText(texto == null ? "" : texto.toString());
                detalhes.setCaretPosition(0);
            }
        });
        atualizarContador();
    }

    public void aoAtualizar(Runnable acao) {
        botaoAtualizar.addActionListener(e -> acao.run());
    }

    public void atualizarTabela(List<RegistroAuditoria> registros) {
        modelo.setRowCount(0);
        for (RegistroAuditoria r : registros) {
            modelo.addRow(new Object[]{r.getId(), r.getDataHora().format(FORMATO), r.getMaquina(), r.getUsuario(),
                    r.getAcao(), r.getEntidade(), r.getRegistroId(), r.getDetalhes()});
        }
        atualizarContador();
    }

    public void alertaErro(String titulo, String msg) {
        Alerta.erro(this, titulo, msg);
    }

    private void filtrar() {
        List<RowFilter<Object, Object>> filtros = new ArrayList<>();
        String acao = (String) filtroAcao.getSelectedItem();
        String entidade = (String) filtroEntidade.getSelectedItem();
        String texto = campoBusca.getText().trim();
        if (!TODAS.equals(acao)) filtros.add(RowFilter.regexFilter("^" + Pattern.quote(acao) + "$", 4));
        if (!TODAS.equals(entidade)) filtros.add(RowFilter.regexFilter("^" + Pattern.quote(entidade) + "$", 5));
        if (!texto.isEmpty()) filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
        ordenador.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
        atualizarContador();
    }

    private void atualizarContador() {
        int total = modelo.getRowCount();
        int visiveis = tabela.getRowCount();
        contador.setText(total == visiveis ? total + " registros" : "Mostrando " + visiveis + " de " + total);
    }

    // Exporta o que está filtrado na tela para um arquivo CSV (abre no Excel)
    private void exportar() {
        JFileChooser escolha = new JFileChooser();
        escolha.setSelectedFile(new File("auditoria_herdtech.csv"));
        if (escolha.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (PrintWriter saida = new PrintWriter(escolha.getSelectedFile(), StandardCharsets.UTF_8)) {
            saida.print('﻿'); // para o Excel reconhecer os acentos
            saida.println(String.join(";", COLUNAS));
            for (int linha = 0; linha < tabela.getRowCount(); linha++) {
                int m = tabela.convertRowIndexToModel(linha);
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < COLUNAS.length; c++) {
                    Object valor = modelo.getValueAt(m, c);
                    String texto = valor == null ? "" : valor.toString().replace("\"", "\"\"");
                    sb.append(c > 0 ? ";" : "").append('"').append(texto).append('"');
                }
                saida.println(sb);
            }
            Alerta.sucesso(this, "AUDITORIA", "Arquivo salvo em:\n" + escolha.getSelectedFile().getAbsolutePath());
        } catch (Exception e) {
            Alerta.erro(this, "AUDITORIA", "Não foi possível salvar o arquivo.\n\n" + e.getMessage());
        }
    }

    // Colore a ação: verde (inclusão), azul (alteração), vermelho (exclusão), cinza (consulta)
    private static class RenderizadorAcao extends Tema.RenderizadorCelula {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int l, int c) {
            Component comp = super.getTableCellRendererComponent(t, valor, sel, foco, l, c);
            Color cor = Tema.TEXTO_SUAVE;
            if (AuditoriaDAO.INCLUSAO.equals(valor)) cor = Tema.PRIMARIA_ESCURA;
            else if (AuditoriaDAO.ALTERACAO.equals(valor)) cor = Tema.AZUL;
            else if (AuditoriaDAO.EXCLUSAO.equals(valor)) cor = Tema.VERMELHO;
            comp.setForeground(cor);
            comp.setFont(Tema.fonte(Font.BOLD, 12));
            return comp;
        }
    }
}
