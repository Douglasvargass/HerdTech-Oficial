package views.componentes;

import utils.Utilidade;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Cores, fontes e estilos usados em todas as telas (baseados no Front: style.css + Logo.png)
public final class Tema {
    // Cores do Front (style.css)
    public static final Color PRIMARIA = new Color(0x00A859);
    public static final Color PRIMARIA_ESCURA = new Color(0x008744);
    public static final Color PRIMARIA_SUAVE = new Color(0xE6F6EE);
    public static final Color SECUNDARIA = new Color(0x2E3A59);
    public static final Color FUNDO = new Color(0xF4F6F9);
    public static final Color CARTAO = Color.WHITE;
    public static final Color TEXTO = new Color(0x2C3E50);
    public static final Color TEXTO_SUAVE = new Color(0x7F8C8D);
    public static final Color BORDA = new Color(0xE2E8F0);
    public static final Color BORDA_FORTE = new Color(0xCBD5E1);
    public static final Color LINHA_ALTERNADA = new Color(0xF8FAFC);
    public static final Color HERO_FIM = new Color(0xF1F8F4);

    // Azul-marinho da logo
    public static final Color MARINHO = new Color(0x14293F);

    // Cores de status
    public static final Color VERMELHO = new Color(0xE74C3C);
    public static final Color LARANJA = new Color(0xF39C12);
    public static final Color AZUL = new Color(0x3498DB);

    private static final String FAMILIA = escolherFonte();
    private static BufferedImage logo;

    private Tema() {
    }

    // ---------------------------------------------------------------- Geral

    // Ajustes globais do Swing (chamar antes de criar qualquer janela)
    public static void aplicar() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorado) {
            // Se não conseguir, segue com o visual padrão do Java
        }

        UIManager.put("ToolTip.background", SECUNDARIA);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.font", fonte(Font.PLAIN, 12));
        UIManager.put("ToolTip.border", new EmptyBorder(6, 10, 6, 10));
        UIManager.put("ComboBox.font", fonte(Font.PLAIN, 14));
        UIManager.put("ComboBox.selectionBackground", PRIMARIA_SUAVE);
        UIManager.put("ComboBox.selectionForeground", TEXTO);
        UIManager.put("List.selectionBackground", PRIMARIA_SUAVE);
        UIManager.put("List.selectionForeground", TEXTO);
        UIManager.put("OptionPane.messageFont", fonte(Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", fonte(Font.BOLD, 13));
    }

    private static String escolherFonte() {
        String[] preferidas = {"Plus Jakarta Sans", "Segoe UI", "Inter", "Helvetica Neue", "Arial"};
        List<String> disponiveis = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String familia : preferidas) {
            if (disponiveis.contains(familia)) return familia;
        }
        return Font.SANS_SERIF;
    }

    public static Font fonte(int estilo, int tamanho) {
        return new Font(FAMILIA, estilo, tamanho);
    }

    // Mistura a cor com branco (fator 0 = cor original, 1 = branco)
    public static Color clarear(Color cor, double fator) {
        int r = (int) (cor.getRed() + (255 - cor.getRed()) * fator);
        int g = (int) (cor.getGreen() + (255 - cor.getGreen()) * fator);
        int b = (int) (cor.getBlue() + (255 - cor.getBlue()) * fator);
        return new Color(r, g, b);
    }

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public static Window janela(Component componente) {
        if (componente == null) return null;
        if (componente instanceof Window) return (Window) componente;
        return SwingUtilities.getWindowAncestor(componente);
    }

    // ---------------------------------------------------------------- Logo

    public static BufferedImage logo() {
        if (logo != null) return logo;
        try {
            URL recurso = Tema.class.getResource("/assets/Logo.png");
            if (recurso != null) {
                logo = ImageIO.read(recurso);
            } else {
                // Se o recurso não foi copiado para o "out", procura direto na pasta do projeto
                for (String caminho : new String[]{"src/assets/Logo.png", "Front/Logo.png"}) {
                    File arquivo = new File(caminho);
                    if (arquivo.exists()) {
                        logo = ImageIO.read(arquivo);
                        break;
                    }
                }
            }
            if (logo != null) logo = removerFundoBranco(logo);
        } catch (Exception ignorado) {
            logo = null;
        }
        return logo;
    }

    // O PNG da logo tem fundo branco: deixa transparente para combinar com qualquer fundo
    private static BufferedImage removerFundoBranco(BufferedImage origem) {
        BufferedImage imagem = new BufferedImage(origem.getWidth(), origem.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < origem.getHeight(); y++) {
            for (int x = 0; x < origem.getWidth(); x++) {
                int rgb = origem.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                int menor = Math.min(r, Math.min(g, b));
                int alfa = (rgb >>> 24);
                // Quanto mais perto do branco, mais transparente (borda suave)
                if (menor > 215) alfa = Math.min(alfa, (255 - menor) * 255 / 40);
                imagem.setRGB(x, y, (alfa << 24) | (rgb & 0xFFFFFF));
            }
        }
        return imagem;
    }

    // Logo redimensionada pela altura (ou um texto, caso a imagem não seja encontrada)
    public static JLabel logo(int altura) {
        BufferedImage imagem = logo();
        if (imagem == null) {
            JLabel texto = new JLabel("<html><span style='color:#14293F'>Herd</span><span style='color:#00A859'>Tech</span></html>");
            texto.setFont(fonte(Font.BOLD, Math.max(18, altura / 2)));
            return texto;
        }
        int largura = imagem.getWidth() * altura / imagem.getHeight();
        return new JLabel(new IconeNitido(imagem, largura, altura));
    }

    // Ícone que se redesenha na resolução real da tela. Com a escala do Windows em 125%/150%,
    // um ImageIcon comum seria ampliado pelo Java e ficaria borrado.
    private static final class IconeNitido implements Icon {
        private final BufferedImage origem;
        private final int largura, altura;
        private BufferedImage cache;

        IconeNitido(BufferedImage origem, int largura, int altura) {
            this.origem = origem;
            this.largura = largura;
            this.altura = altura;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            AffineTransform t = g2.getTransform();
            int pw = (int) Math.round(largura * t.getScaleX());
            int ph = (int) Math.round(altura * t.getScaleY());
            if (cache == null || cache.getWidth() != pw || cache.getHeight() != ph) {
                cache = redimensionar(origem, pw, ph);
            }
            // Posição em pixels reais da tela; desenha a imagem 1:1, sem nova ampliação
            double dx = t.getTranslateX() + x * t.getScaleX();
            double dy = t.getTranslateY() + y * t.getScaleY();
            g2.setTransform(AffineTransform.getTranslateInstance(Math.round(dx), Math.round(dy)));
            g2.drawImage(cache, 0, 0, null);
            g2.dispose();
        }

        @Override public int getIconWidth() { return largura; }
        @Override public int getIconHeight() { return altura; }
    }

    // Ícones da janela: só o desenho (H + vaca), centralizado num quadrado
    public static List<Image> iconesJanela() {
        List<Image> icones = new ArrayList<>();
        BufferedImage imagem = logo();
        if (imagem == null) return icones;

        int w = imagem.getWidth(), h = imagem.getHeight();
        BufferedImage marca = imagem.getSubimage((int) (w * 0.17), 0, (int) (w * 0.68), (int) (h * 0.72));
        for (int tamanho : new int[]{16, 24, 32, 48, 64, 128}) {
            BufferedImage quadrado = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = quadrado.createGraphics();
            suavizar(g);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, tamanho, tamanho, tamanho / 4.0, tamanho / 4.0));
            double escala = Math.min((tamanho * 0.86) / marca.getWidth(), (tamanho * 0.86) / marca.getHeight());
            int mw = (int) (marca.getWidth() * escala), mh = (int) (marca.getHeight() * escala);
            g.drawImage(redimensionar(marca, mw, mh), (tamanho - mw) / 2, (tamanho - mh) / 2, null);
            g.dispose();
            icones.add(quadrado);
        }
        return icones;
    }

    // Redimensiona em etapas para a imagem ficar nítida
    private static BufferedImage redimensionar(BufferedImage origem, int largura, int altura) {
        BufferedImage atual = origem;
        int w = origem.getWidth(), h = origem.getHeight();
        do {
            w = Math.max(largura, w / 2);
            h = Math.max(altura, h / 2);
            BufferedImage proxima = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = proxima.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(atual, 0, 0, w, h, null);
            g.dispose();
            atual = proxima;
        } while (w != largura || h != altura);
        return atual;
    }

    // ---------------------------------------------------------------- Textos

    public static JLabel titulo(String texto, int tamanho) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(fonte(Font.BOLD, tamanho));
        rotulo.setForeground(SECUNDARIA);
        return rotulo;
    }

    public static JLabel textoSuave(String texto, int tamanho) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(fonte(Font.PLAIN, tamanho));
        rotulo.setForeground(TEXTO_SUAVE);
        return rotulo;
    }

    public static JLabel rotuloCampo(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(fonte(Font.BOLD, 13));
        rotulo.setForeground(TEXTO);
        return rotulo;
    }

    // ---------------------------------------------------------------- Campos

    public static JTextField campoTexto(String valor) {
        JTextField campo = new JTextField(valor == null ? "" : valor);
        estilizarCampo(campo, new Insets(9, 12, 9, 12));
        return campo;
    }

    // Campo com máscara (ex.: "##/##/####" para datas)
    public static JFormattedTextField campoMascara(String mascara, String valor) {
        JFormattedTextField campo;
        try {
            MaskFormatter formato = new MaskFormatter(mascara);
            formato.setPlaceholderCharacter('_');
            campo = new JFormattedTextField(formato);
        } catch (ParseException e) {
            campo = new JFormattedTextField();
        }
        campo.setFocusLostBehavior(JFormattedTextField.PERSIST);
        if (valor != null && !valor.trim().isEmpty()) {
            try {
                campo.setValue(valor);
            } catch (Exception ignorado) {
                campo.setText(valor);
            }
        }
        estilizarCampo(campo, new Insets(9, 12, 9, 12));
        return campo;
    }

    // Texto digitado num campo com máscara ("" se nada foi digitado)
    public static String textoMascara(JFormattedTextField campo) {
        String texto = campo.getText();
        if (Utilidade.somenteNumeros(texto).isEmpty()) return "";
        return texto.trim();
    }

    public static void estilizarCampo(JComponent campo, Insets espaco) {
        campo.setFont(fonte(Font.PLAIN, 14));
        campo.setForeground(TEXTO);
        campo.setBackground(Color.WHITE);
        campo.setBorder(new CompoundBorder(new BordaArredondada(), new EmptyBorder(espaco)));
        if (campo instanceof JTextField) {
            JTextField texto = (JTextField) campo;
            texto.setCaretColor(PRIMARIA);
            texto.setSelectionColor(clarear(PRIMARIA, 0.7));
            texto.setSelectedTextColor(TEXTO);
        }
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.repaint();
            }
        });
    }

    public static <T> JComboBox<T> estilizarCombo(JComboBox<T> combo) {
        combo.setFont(fonte(Font.PLAIN, 14));
        combo.setForeground(TEXTO);
        combo.setBackground(Color.WHITE);
        combo.setMaximumRowCount(10);
        Dimension tamanho = combo.getPreferredSize();
        combo.setPreferredSize(new Dimension(tamanho.width, 40));
        return combo;
    }

    // Borda arredondada que fica verde quando o campo está em foco
    public static class BordaArredondada extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int largura, int altura) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            if (c.hasFocus()) {
                g2.setColor(new Color(0, 168, 89, 45));
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Double(x + 1.5, y + 1.5, largura - 3, altura - 3, 12, 12));
                g2.setColor(PRIMARIA);
            } else {
                g2.setColor(BORDA_FORTE);
            }
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Double(x + 2.5, y + 2.5, largura - 5, altura - 5, 10, 10));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(3, 3, 3, 3);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(3, 3, 3, 3);
            return insets;
        }
    }

    // ---------------------------------------------------------------- Tabelas

    public static void estilizarTabela(JTable tabela) {
        tabela.setFont(fonte(Font.PLAIN, 13));
        tabela.setForeground(TEXTO);
        tabela.setBackground(Color.WHITE);
        tabela.setRowHeight(42);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.setGridColor(BORDA);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setSelectionBackground(PRIMARIA_SUAVE);
        tabela.setSelectionForeground(TEXTO);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(false);
        tabela.setDefaultRenderer(Object.class, new RenderizadorCelula());
        tabela.setDefaultRenderer(Integer.class, new RenderizadorCelula());

        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new Dimension(0, 42));
        cabecalho.setBackground(LINHA_ALTERNADA);
        cabecalho.setDefaultRenderer((t, valor, selecionado, foco, linha, coluna) -> {
            String texto = valor == null ? "" : valor.toString().toUpperCase();

            // Seta indicando a ordenação atual
            if (t.getRowSorter() != null && !t.getRowSorter().getSortKeys().isEmpty()) {
                RowSorter.SortKey chave = t.getRowSorter().getSortKeys().get(0);
                if (chave.getColumn() == t.convertColumnIndexToModel(coluna)) {
                    if (chave.getSortOrder() == SortOrder.ASCENDING) texto += "  ▲";
                    else if (chave.getSortOrder() == SortOrder.DESCENDING) texto += "  ▼";
                }
            }

            JLabel rotulo = new JLabel(texto);
            rotulo.setFont(fonte(Font.BOLD, 11));
            rotulo.setForeground(TEXTO_SUAVE);
            rotulo.setOpaque(true);
            rotulo.setBackground(LINHA_ALTERNADA);
            rotulo.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDA), new EmptyBorder(0, 14, 0, 14)));
            return rotulo;
        });
    }

    // Célula padrão: espaçamento, linhas zebradas e ID em destaque suave
    public static class RenderizadorCelula extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionado, false, linha, coluna);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setHorizontalAlignment(LEFT);
            setFont(fonte(Font.PLAIN, 13));
            setForeground(TEXTO);
            if (!selecionado) setBackground(linha % 2 == 0 ? Color.WHITE : LINHA_ALTERNADA);

            if (tabela.convertColumnIndexToModel(coluna) == 0 && valor instanceof Integer) {
                setText("#" + valor);
                setForeground(TEXTO_SUAVE);
                setFont(fonte(Font.BOLD, 12));
            }
            return this;
        }
    }

    // ---------------------------------------------------------------- Rolagem

    public static JScrollPane rolagem(Component conteudo) {
        JScrollPane rolagem = new JScrollPane(conteudo);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.getViewport().setBackground(Color.WHITE);
        rolagem.getVerticalScrollBar().setUI(new BarraRolagem());
        rolagem.getHorizontalScrollBar().setUI(new BarraRolagem());
        rolagem.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        rolagem.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        rolagem.getVerticalScrollBar().setUnitIncrement(18);
        return rolagem;
    }

    // Barra de rolagem fina e arredondada
    public static class BarraRolagem extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = BORDA_FORTE;
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return botaoVazio();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return botaoVazio();
        }

        private JButton botaoVazio() {
            JButton botao = new JButton();
            botao.setPreferredSize(new Dimension(0, 0));
            botao.setMinimumSize(new Dimension(0, 0));
            botao.setMaximumSize(new Dimension(0, 0));
            return botao;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle area) {
            g.setColor(c.getParent() != null ? c.getParent().getBackground() : Color.WHITE);
            g.fillRect(area.x, area.y, area.width, area.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle area) {
            if (area.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(isThumbRollover() ? TEXTO_SUAVE : BORDA_FORTE);
            g2.fill(new RoundRectangle2D.Double(area.x + 2, area.y + 2, area.width - 4, area.height - 4, 8, 8));
            g2.dispose();
        }
    }
}
