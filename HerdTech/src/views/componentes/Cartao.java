package views.componentes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

// Painel branco com cantos arredondados, borda e sombra suave (como os cards do Front)
public class Cartao extends JPanel {
    private static final int RAIO = 18;

    private Color inicioGradiente;
    private Color fimGradiente;

    public Cartao() {
        this(new BorderLayout());
    }

    public Cartao(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(new EmptyBorder(20, 22, 22, 22));
    }

    // Fundo em degradê (usado no destaque da tela inicial)
    public void setGradiente(Color inicio, Color fim) {
        this.inicioGradiente = inicio;
        this.fimGradiente = fim;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Tema.suavizar(g2);
        int w = getWidth(), h = getHeight();

        // Sombra
        g2.setColor(new Color(15, 23, 42, 10));
        g2.fill(new RoundRectangle2D.Double(0, 2, w, h - 2, RAIO, RAIO));
        g2.setColor(new Color(15, 23, 42, 6));
        g2.fill(new RoundRectangle2D.Double(0, 1, w, h - 1, RAIO, RAIO));

        RoundRectangle2D forma = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 3, RAIO, RAIO);
        if (inicioGradiente != null) {
            g2.setPaint(new GradientPaint(0, 0, inicioGradiente, w, h, fimGradiente));
        } else {
            g2.setColor(Tema.CARTAO);
        }
        g2.fill(forma);
        g2.setColor(Tema.BORDA);
        g2.draw(forma);
        g2.dispose();
    }
}
