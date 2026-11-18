package views.componentes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

// "Pílula" colorida de status (Hoje, Amanhã, Em breve...). Também serve como célula de tabela.
public class Etiqueta extends JLabel implements TableCellRenderer {
    private static final int ALTURA = 22;

    private Color cor = Tema.TEXTO_SUAVE;
    private Color fundoLinha;

    public Etiqueta() {
        setFont(Tema.fonte(Font.BOLD, 11));
        setBorder(new EmptyBorder(0, 14, 0, 14));
    }

    public Etiqueta(String texto, Color cor) {
        setFont(Tema.fonte(Font.BOLD, 11));
        definir(texto, cor);
    }

    public void definir(String texto, Color cor) {
        this.cor = cor;
        setText(texto);
        setForeground(cor);
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Insets espaco = getInsets();
        int largura = getFontMetrics(getFont()).stringWidth(getText() == null ? "" : getText()) + 20;
        return new Dimension(largura + espaco.left + espaco.right, ALTURA + 2);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Tema.suavizar(g2);
        if (fundoLinha != null) {
            g2.setColor(fundoLinha);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        String texto = getText() == null ? "" : getText();
        FontMetrics medidas = g2.getFontMetrics(getFont());
        int largura = medidas.stringWidth(texto) + 20;
        int x = getInsets().left;
        int y = (getHeight() - ALTURA) / 2;

        g2.setColor(Tema.clarear(cor, 0.87));
        g2.fill(new RoundRectangle2D.Double(x, y, largura, ALTURA, ALTURA, ALTURA));
        g2.setColor(cor);
        g2.setFont(getFont());
        g2.drawString(texto, x + 10, y + (ALTURA + medidas.getAscent() - medidas.getDescent()) / 2);
        g2.dispose();
    }

    @Override
    public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado,
                                                   boolean foco, int linha, int coluna) {
        if (valor instanceof SituacaoVacina) {
            SituacaoVacina situacao = (SituacaoVacina) valor;
            definir(situacao.getRotulo(), situacao.getCor());
        } else {
            definir(valor == null ? "" : valor.toString(), Tema.TEXTO_SUAVE);
        }
        fundoLinha = selecionado ? tabela.getSelectionBackground()
                : (linha % 2 == 0 ? Color.WHITE : Tema.LINHA_ALTERNADA);
        return this;
    }
}
