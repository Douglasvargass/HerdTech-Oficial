package views.componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

// Ícones vetoriais desenhados em código (grade de 24x24, traço arredondado)
public final class Icones {

    private interface Desenho {
        void desenhar(Graphics2D g);
    }

    private Icones() {
    }

    private static Icon criar(int tamanho, Color cor, Desenho desenho) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.suavizar(g2);
                g2.translate(x, y);
                g2.scale(tamanho / 24.0, tamanho / 24.0);
                g2.setColor(cor);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                desenho.desenhar(g2);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return tamanho;
            }

            @Override
            public int getIconHeight() {
                return tamanho;
            }
        };
    }

    private static void linha(Graphics2D g, double x1, double y1, double x2, double y2) {
        g.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    public static Icon inicio(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            Path2D casa = new Path2D.Double();
            casa.moveTo(3, 10);
            casa.lineTo(12, 3);
            casa.lineTo(21, 10);
            casa.lineTo(21, 19.5);
            casa.quadTo(21, 21, 19.5, 21);
            casa.lineTo(4.5, 21);
            casa.quadTo(3, 21, 3, 19.5);
            casa.closePath();
            g.draw(casa);
            linha(g, 9.5, 21, 9.5, 14);
            linha(g, 9.5, 14, 14.5, 14);
            linha(g, 14.5, 14, 14.5, 21);
        });
    }

    public static Icon pessoa(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            g.draw(new Ellipse2D.Double(8, 3, 8, 8));
            Path2D corpo = new Path2D.Double();
            corpo.moveTo(4, 21);
            corpo.curveTo(4, 16.5, 7.5, 14, 12, 14);
            corpo.curveTo(16.5, 14, 20, 16.5, 20, 21);
            g.draw(corpo);
        });
    }

    public static Icon gado(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            // Chifres
            Path2D chifres = new Path2D.Double();
            chifres.moveTo(7.5, 5.5);
            chifres.quadTo(4, 5.5, 3.5, 2.5);
            chifres.moveTo(16.5, 5.5);
            chifres.quadTo(20, 5.5, 20.5, 2.5);
            g.draw(chifres);
            // Orelhas
            g.draw(new Ellipse2D.Double(1.5, 7.5, 5, 3));
            g.draw(new Ellipse2D.Double(17.5, 7.5, 5, 3));
            // Cabeça
            Path2D cabeca = new Path2D.Double();
            cabeca.moveTo(8.2, 14);
            cabeca.lineTo(6.8, 7.2);
            cabeca.quadTo(6.5, 5, 8.8, 5);
            cabeca.lineTo(15.2, 5);
            cabeca.quadTo(17.5, 5, 17.2, 7.2);
            cabeca.lineTo(15.8, 14);
            g.draw(cabeca);
            // Focinho, narinas e olhos
            g.draw(new RoundRectangle2D.Double(7.5, 13.5, 9, 7.5, 6, 6));
            g.fill(new Ellipse2D.Double(9.6, 16.3, 1.7, 1.7));
            g.fill(new Ellipse2D.Double(12.7, 16.3, 1.7, 1.7));
            g.fill(new Ellipse2D.Double(9.2, 9, 1.8, 1.8));
            g.fill(new Ellipse2D.Double(13, 9, 1.8, 1.8));
        });
    }

    public static Icon seringa(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            g.rotate(Math.toRadians(45), 12, 12);
            g.draw(new RoundRectangle2D.Double(9.5, 5.5, 5, 10.5, 1.5, 1.5));
            linha(g, 12, 2.5, 12, 5.5);
            linha(g, 9, 2.5, 15, 2.5);
            linha(g, 12, 16, 12, 21.5);
            linha(g, 9.5, 9, 11.5, 9);
            linha(g, 9.5, 12, 11.5, 12);
        });
    }

    public static Icon calendario(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            g.draw(new RoundRectangle2D.Double(3, 4.5, 18, 16.5, 4, 4));
            linha(g, 16, 2.5, 16, 6.5);
            linha(g, 8, 2.5, 8, 6.5);
            linha(g, 3, 10, 21, 10);
        });
    }

    public static Icon grafico(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            linha(g, 6, 20, 6, 13);
            linha(g, 12, 20, 12, 4);
            linha(g, 18, 20, 18, 9);
        });
    }

    public static Icon local(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            Path2D pino = new Path2D.Double();
            pino.moveTo(12, 21.5);
            pino.curveTo(8, 17, 5, 13.5, 5, 9.5);
            pino.curveTo(5, 5.5, 8.2, 2.5, 12, 2.5);
            pino.curveTo(15.8, 2.5, 19, 5.5, 19, 9.5);
            pino.curveTo(19, 13.5, 16, 17, 12, 21.5);
            pino.closePath();
            g.draw(pino);
            g.draw(new Ellipse2D.Double(9.5, 7, 5, 5));
        });
    }

    public static Icon mais(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            linha(g, 12, 5, 12, 19);
            linha(g, 5, 12, 19, 12);
        });
    }

    public static Icon editar(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            Path2D lapis = new Path2D.Double();
            lapis.moveTo(16.5, 3.5);
            lapis.lineTo(20.5, 7.5);
            lapis.lineTo(8, 20);
            lapis.lineTo(3.5, 20.5);
            lapis.lineTo(4, 16);
            lapis.closePath();
            g.draw(lapis);
            linha(g, 14, 6, 18, 10);
        });
    }

    public static Icon lixeira(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            linha(g, 3, 6, 21, 6);
            Path2D cesto = new Path2D.Double();
            cesto.moveTo(5.5, 6);
            cesto.lineTo(6.5, 19.5);
            cesto.quadTo(6.7, 21, 8.2, 21);
            cesto.lineTo(15.8, 21);
            cesto.quadTo(17.3, 21, 17.5, 19.5);
            cesto.lineTo(18.5, 6);
            g.draw(cesto);
            Path2D tampa = new Path2D.Double();
            tampa.moveTo(9, 6);
            tampa.lineTo(9, 3.5);
            tampa.lineTo(15, 3.5);
            tampa.lineTo(15, 6);
            g.draw(tampa);
            linha(g, 10, 10.5, 10, 16.5);
            linha(g, 14, 10.5, 14, 16.5);
        });
    }

    public static Icon atualizar(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            g.draw(new Arc2D.Double(4, 4, 16, 16, 70, 290, Arc2D.OPEN));
            Path2D seta = new Path2D.Double();
            seta.moveTo(11, 1.5);
            seta.lineTo(14.6, 4.4);
            seta.lineTo(11.4, 7.6);
            g.draw(seta);
        });
    }

    public static Icon busca(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            g.draw(new Ellipse2D.Double(4, 4, 13, 13));
            linha(g, 15.5, 15.5, 20.5, 20.5);
        });
    }

    public static Icon sino(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            Path2D sino = new Path2D.Double();
            sino.moveTo(6, 17);
            sino.lineTo(6, 11);
            sino.curveTo(6, 7.5, 8.7, 5, 12, 5);
            sino.curveTo(15.3, 5, 18, 7.5, 18, 11);
            sino.lineTo(18, 17);
            g.draw(sino);
            linha(g, 4, 17, 20, 17);
            linha(g, 12, 3, 12, 5);
            g.draw(new Arc2D.Double(10, 18.5, 4, 3.5, 180, 180, Arc2D.OPEN));
        });
    }

    public static Icon confirmar(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            Path2D check = new Path2D.Double();
            check.moveTo(5.5, 12.5);
            check.lineTo(10, 17);
            check.lineTo(18.5, 7.5);
            g.draw(check);
        });
    }

    public static Icon fechar(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            linha(g, 6.5, 6.5, 17.5, 17.5);
            linha(g, 17.5, 6.5, 6.5, 17.5);
        });
    }

    public static Icon exclamacao(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            linha(g, 12, 5, 12, 13.5);
            g.fill(new Ellipse2D.Double(10.5, 17, 3, 3));
        });
    }

    public static Icon interrogacao(int tamanho, Color cor) {
        return criar(tamanho, cor, g -> {
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D curva = new Path2D.Double();
            curva.moveTo(8.5, 8.5);
            curva.curveTo(8.5, 3.5, 15.5, 3.5, 15.5, 8.5);
            curva.curveTo(15.5, 11.5, 12, 11.5, 12, 14.5);
            g.draw(curva);
            g.fill(new Ellipse2D.Double(10.6, 17.5, 2.8, 2.8));
        });
    }

    // Quadrado arredondado colorido com um ícone no centro (usado nos cartões)
    public static JComponent bloco(Icon icone, Color cor, int tamanho) {
        JComponent bloco = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.suavizar(g2);
                g2.setColor(Tema.clarear(cor, 0.88));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                icone.paintIcon(this, g, (getWidth() - icone.getIconWidth()) / 2, (getHeight() - icone.getIconHeight()) / 2);
            }
        };
        Dimension d = new Dimension(tamanho, tamanho);
        bloco.setPreferredSize(d);
        bloco.setMinimumSize(d);
        bloco.setMaximumSize(d);
        return bloco;
    }
}
