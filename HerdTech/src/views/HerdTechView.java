package views;

import views.componentes.Icones;
import views.componentes.Tema;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

// Janela principal: cabeçalho com logo e menu (igual à navbar do Front) + área das telas
public class HerdTechView extends JFrame {
    private final CardLayout cartoes = new CardLayout();
    private final JPanel areaTelas = new JPanel(cartoes);
    private final JPanel menu = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
    private final Map<String, ItemMenu> itens = new LinkedHashMap<>();
    private final Notificacao notificacao = new Notificacao();
    private final JLabel status = new JLabel();

    private Consumer<String> aoTrocarTela;
    private String telaAtual;

    public HerdTechView() {
        super("HerdTech - Gestão Inteligente de Rebanho");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImages(Tema.iconesJanela());
        setMinimumSize(new Dimension(1180, 720));
        setSize(1366, 840);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FUNDO);
        raiz.add(montarCabecalho(), BorderLayout.NORTH);
        areaTelas.setBackground(Tema.FUNDO);
        raiz.add(areaTelas, BorderLayout.CENTER);
        raiz.add(montarRodape(), BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    private JComponent montarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Color.WHITE);
        cabecalho.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Tema.BORDA), new EmptyBorder(10, 32, 10, 32)));

        JLabel logo = Tema.logo(52);
        logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logo.setToolTipText("Ir para o Início");
        logo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!itens.isEmpty()) mostrarTela(itens.keySet().iterator().next());
            }
        });

        menu.setOpaque(false);
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        centro.add(menu);

        // Perfil (igual ao Front: sino + nome + cargo)
        JPanel perfil = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        perfil.setOpaque(false);

        JComponent avatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.suavizar(g2);
                g2.setColor(Tema.PRIMARIA);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(Tema.fonte(Font.BOLD, 16));
                FontMetrics m = g2.getFontMetrics();
                g2.drawString("G", (getWidth() - m.stringWidth("G")) / 2, (getHeight() + m.getAscent() - m.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));

        JPanel usuario = new JPanel(new GridLayout(2, 1));
        usuario.setOpaque(false);
        JLabel nome = new JLabel("Guilherme");
        nome.setFont(Tema.fonte(Font.BOLD, 14));
        nome.setForeground(Tema.TEXTO);
        usuario.add(nome);
        usuario.add(Tema.textoSuave("Administrador", 12));

        JPanel separador = new JPanel();
        separador.setBackground(Tema.BORDA);
        separador.setPreferredSize(new Dimension(1, 30));

        perfil.add(notificacao);
        perfil.add(separador);
        perfil.add(avatar);
        perfil.add(usuario);

        JPanel colunaLogo = new JPanel(new GridBagLayout());
        colunaLogo.setOpaque(false);
        colunaLogo.add(logo);
        JPanel colunaPerfil = new JPanel(new GridBagLayout());
        colunaPerfil.setOpaque(false);
        colunaPerfil.add(perfil);

        cabecalho.add(colunaLogo, BorderLayout.WEST);
        cabecalho.add(centro, BorderLayout.CENTER);
        cabecalho.add(colunaPerfil, BorderLayout.EAST);
        return cabecalho;
    }

    private JComponent montarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Tema.BORDA), new EmptyBorder(7, 32, 7, 32)));

        status.setFont(Tema.fonte(Font.PLAIN, 12));
        status.setForeground(Tema.TEXTO_SUAVE);
        status.setIconTextGap(8);
        setStatusBanco(true, "Conectando ao banco de dados...");

        JLabel direitos = Tema.textoSuave("HerdTech © " + LocalDate.now().getYear() + "  •  Tecnologia no campo", 12);

        rodape.add(status, BorderLayout.WEST);
        rodape.add(direitos, BorderLayout.EAST);
        return rodape;
    }

    // ---------------------------------------------------------------- Telas

    public void adicionarTela(String id, String rotulo, Function<Color, Icon> icone, JComponent tela) {
        ItemMenu item = new ItemMenu(rotulo, icone);
        item.addActionListener(e -> mostrarTela(id));
        itens.put(id, item);
        menu.add(item);
        areaTelas.add(tela, id);
        if (telaAtual == null) mostrarTela(id);
    }

    public void mostrarTela(String id) {
        telaAtual = id;
        cartoes.show(areaTelas, id);
        for (Map.Entry<String, ItemMenu> item : itens.entrySet()) {
            item.getValue().setAtivo(item.getKey().equals(id));
        }
        if (aoTrocarTela != null) aoTrocarTela.accept(id);
    }

    public void aoTrocarTela(Consumer<String> acao) {
        this.aoTrocarTela = acao;
    }

    public void aoClicarNotificacoes(Runnable acao) {
        notificacao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                acao.run();
            }
        });
    }

    public void setNotificacoes(int quantidade) {
        notificacao.setQuantidade(quantidade);
    }

    public void setStatusBanco(boolean conectado, String mensagem) {
        Color cor = conectado ? Tema.PRIMARIA : Tema.VERMELHO;
        status.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.suavizar(g2);
                g2.setColor(Tema.clarear(cor, 0.75));
                g2.fillOval(x, y, 12, 12);
                g2.setColor(cor);
                g2.fillOval(x + 3, y + 3, 6, 6);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        });
        status.setText(mensagem);
        status.setToolTipText(mensagem);
    }

    // ---------------------------------------------------------------- Item do menu

    private static class ItemMenu extends JButton {
        private final Function<Color, Icon> icone;
        private boolean ativo;
        private boolean sobre;

        ItemMenu(String texto, Function<Color, Icon> icone) {
            super(texto);
            this.icone = icone;
            setFont(Tema.fonte(Font.BOLD, 14));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setIconTextGap(8);
            setBorder(new EmptyBorder(10, 16, 10, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sobre = true;
                    atualizarCores();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sobre = false;
                    atualizarCores();
                }
            });
            atualizarCores();
        }

        void setAtivo(boolean ativo) {
            this.ativo = ativo;
            atualizarCores();
        }

        private void atualizarCores() {
            Color cor = ativo || sobre ? Tema.PRIMARIA : Tema.TEXTO_SUAVE;
            setForeground(ativo ? Tema.PRIMARIA : sobre ? Tema.PRIMARIA : Tema.TEXTO);
            setIcon(icone.apply(cor));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (ativo || sobre) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.suavizar(g2);
                g2.setColor(ativo ? Tema.PRIMARIA_SUAVE : Tema.LINHA_ALTERNADA);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    // ---------------------------------------------------------------- Sino de notificações

    private static class Notificacao extends JComponent {
        private final Icon sino = Icones.sino(22, Tema.TEXTO);
        private int quantidade;

        Notificacao() {
            setPreferredSize(new Dimension(38, 38));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setQuantidade(0);
        }

        void setQuantidade(int quantidade) {
            this.quantidade = quantidade;
            setToolTipText(quantidade == 0 ? "Nenhuma vacina pendente"
                    : quantidade + (quantidade == 1 ? " dose de vacina precisa" : " doses de vacina precisam") + " de atenção");
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.suavizar(g2);
            sino.paintIcon(this, g2, (getWidth() - 22) / 2, (getHeight() - 22) / 2 + 1);
            if (quantidade > 0) {
                String texto = quantidade > 9 ? "9+" : String.valueOf(quantidade);
                g2.setFont(Tema.fonte(Font.BOLD, 10));
                FontMetrics m = g2.getFontMetrics();
                int largura = Math.max(17, m.stringWidth(texto) + 8);
                int x = getWidth() - largura, y = 1;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x - 2, y - 2, largura + 4, 21, 21, 21);
                g2.setColor(Tema.VERMELHO);
                g2.fillRoundRect(x, y, largura, 17, 17, 17);
                g2.setColor(Color.WHITE);
                g2.drawString(texto, x + (largura - m.stringWidth(texto)) / 2, y + (17 + m.getAscent() - m.getDescent()) / 2);
            }
            g2.dispose();
        }
    }
}
