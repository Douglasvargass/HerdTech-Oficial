package views;

import views.componentes.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.Pattern;

// Base das telas de Clientes, Animais e Vacinação: título, pesquisa, botões e tabela
public abstract class TelaLista extends JPanel {
    protected final DefaultTableModel modelo;
    protected final JTable tabela;
    private final TableRowSorter<DefaultTableModel> ordenador;
    private final CampoBusca campoBusca = new CampoBusca("Pesquisar...");
    private final JLabel contador = Tema.textoSuave("", 13);
    private final String mensagemVazia;

    private final Botao botaoNovo;
    private final Botao botaoEditar = new Botao("Editar", Icones.editar(15, Tema.SECUNDARIA), Botao.Estilo.CONTORNO);
    private final Botao botaoExcluir = new Botao("Excluir", Icones.lixeira(15, Color.WHITE), Botao.Estilo.PERIGO);
    private final Botao botaoAtualizar = new Botao("Atualizar", Icones.atualizar(15, Tema.PRIMARIA), Botao.Estilo.FANTASMA);

    private Runnable acaoEditar;

    protected TelaLista(String titulo, String subtitulo, String textoNovo, String mensagemVazia, String[] colunas) {
        super(new BorderLayout(0, 20));
        this.mensagemVazia = mensagemVazia;
        setBackground(Tema.FUNDO);
        setBorder(new EmptyBorder(28, 36, 28, 36));

        // ---------- Cabeçalho da tela
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Tema.titulo(titulo, 26));
        textos.add(Box.createVerticalStrut(4));
        textos.add(Tema.textoSuave(subtitulo, 14));

        botaoNovo = new Botao(textoNovo, Icones.mais(16, Color.WHITE), Botao.Estilo.PRIMARIO);
        botaoNovo.setBorder(new EmptyBorder(12, 20, 12, 22));
        JPanel colunaNovo = new JPanel(new GridBagLayout());
        colunaNovo.setOpaque(false);
        colunaNovo.add(botaoNovo);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(textos, BorderLayout.WEST);
        topo.add(colunaNovo, BorderLayout.EAST);

        // ---------- Tabela
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int coluna) {
                if (coluna == 0) return Integer.class;
                if (getRowCount() > 0 && getValueAt(0, coluna) != null) return getValueAt(0, coluna).getClass();
                return Object.class;
            }
        };

        tabela = new JTable(modelo) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    Tema.suavizar(g2);
                    g2.setFont(Tema.fonte(Font.PLAIN, 14));
                    g2.setColor(Tema.TEXTO_SUAVE);
                    String texto = campoBusca.getText().isEmpty() ? TelaLista.this.mensagemVazia
                            : "Nenhum resultado para \"" + campoBusca.getText() + "\"";
                    FontMetrics m = g2.getFontMetrics();
                    g2.drawString(texto, (getWidth() - m.stringWidth(texto)) / 2, 70);
                    g2.dispose();
                }
            }
        };
        Tema.estilizarTabela(tabela);
        ordenador = new TableRowSorter<>(modelo);
        tabela.setRowSorter(ordenador);

        // ---------- Barra de ferramentas
        campoBusca.setPreferredSize(new Dimension(300, 40));
        campoBusca.setToolTipText("Pesquise por qualquer informação da tabela");

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        esquerda.setOpaque(false);
        esquerda.add(campoBusca);
        esquerda.add(Box.createHorizontalStrut(14));
        esquerda.add(contador);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        direita.setOpaque(false);
        direita.add(botaoAtualizar);
        direita.add(botaoEditar);
        direita.add(botaoExcluir);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(new EmptyBorder(0, 0, 16, 0));
        barra.add(esquerda, BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);

        JScrollPane rolagem = Tema.rolagem(tabela);
        rolagem.setBorder(new MatteBorder(1, 1, 1, 1, Tema.BORDA));

        Cartao cartao = new Cartao(new BorderLayout());
        cartao.add(barra, BorderLayout.NORTH);
        cartao.add(rolagem, BorderLayout.CENTER);

        add(topo, BorderLayout.NORTH);
        add(cartao, BorderLayout.CENTER);

        // ---------- Comportamentos
        campoBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
        });

        tabela.getSelectionModel().addListSelectionListener(e -> atualizarBotoes());
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() >= 0 && acaoEditar != null) {
                    acaoEditar.run();
                }
            }
        });

        atualizarBotoes();
        atualizarContador();
    }

    // ---------------------------------------------------------------- Ações (ligadas pelo Controller)

    public void aoNovo(Runnable acao) {
        botaoNovo.addActionListener(e -> acao.run());
    }

    public void aoEditar(Runnable acao) {
        acaoEditar = acao;
        botaoEditar.addActionListener(e -> acao.run());
        tabela.registerKeyboardAction(e -> {
            if (getIdSelecionado() != null) acao.run();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
    }

    public void aoExcluir(Runnable acao) {
        botaoExcluir.addActionListener(e -> acao.run());
        tabela.registerKeyboardAction(e -> {
            if (getIdSelecionado() != null) acao.run();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_FOCUSED);
    }

    public void aoAtualizar(Runnable acao) {
        botaoAtualizar.addActionListener(e -> acao.run());
    }

    // ---------------------------------------------------------------- Tabela

    // Id (coluna 0) da linha selecionada, ou null se nada estiver selecionado
    public Integer getIdSelecionado() {
        int linhaVisivel = tabela.getSelectedRow();
        if (linhaVisivel < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(linhaVisivel), 0);
    }

    // Troca o conteúdo da tabela mantendo a linha selecionada
    protected void preencher(List<Object[]> linhas) {
        Integer selecionado = getIdSelecionado();
        modelo.setRowCount(0);
        for (Object[] linha : linhas) modelo.addRow(linha);

        if (selecionado != null) {
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (selecionado.equals(modelo.getValueAt(i, 0))) {
                    int visivel = tabela.convertRowIndexToView(i);
                    if (visivel >= 0) tabela.setRowSelectionInterval(visivel, visivel);
                    break;
                }
            }
        }
        atualizarContador();
        atualizarBotoes();
    }

    protected void larguras(int... larguras) {
        for (int i = 0; i < larguras.length && i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.getColumnModel().getColumn(0).setMaxWidth(90);
    }

    protected void usarRenderizador(int coluna, TableCellRenderer renderizador) {
        tabela.getColumnModel().getColumn(coluna).setCellRenderer(renderizador);
    }

    private void filtrar() {
        String texto = campoBusca.getText().trim();
        ordenador.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
        atualizarContador();
        tabela.repaint();
    }

    private void atualizarContador() {
        int total = modelo.getRowCount();
        int visiveis = tabela.getRowCount();
        if (total == visiveis) {
            contador.setText(total + (total == 1 ? " registro" : " registros"));
        } else {
            contador.setText("Mostrando " + visiveis + " de " + total);
        }
    }

    private void atualizarBotoes() {
        boolean selecionado = tabela.getSelectedRow() >= 0;
        botaoEditar.setEnabled(selecionado);
        botaoExcluir.setEnabled(selecionado);
    }

    // ---------------------------------------------------------------- Alertas (mesmos nomes de antes)

    public void alertaSucesso(String titulo, String msg) {
        Alerta.sucesso(this, titulo, msg);
    }

    public void alertaErro(String titulo, String msg) {
        Alerta.erro(this, titulo, msg);
    }

    // Retorna 0 quando o usuário confirma (igual ao JOptionPane.YES_OPTION)
    public int alertaConfirmacao(String titulo, String msg) {
        return Alerta.confirmar(this, titulo, msg, "Sim, excluir") ? 0 : 1;
    }
}
