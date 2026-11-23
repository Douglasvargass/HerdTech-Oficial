package views.componentes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

// Botão arredondado no estilo do Front (.btn-primary, .btn-secondary...)
public class Botao extends JButton {

    public enum Estilo {
        PRIMARIO,    // verde
        SECUNDARIO,  // azul escuro
        PERIGO,      // vermelho
        CONTORNO,    // branco com borda
        FANTASMA     // sem fundo
    }

    private final Estilo estilo;
    private boolean sobre;
    private boolean pressionado;

    public Botao(String texto, Estilo estilo) {
        this(texto, null, estilo);
    }

    public Botao(String texto, Icon icone, Estilo estilo) {
        super(texto, icone);
        this.estilo = estilo;

        setFont(Tema.fonte(Font.BOLD, 13));
        setForeground(corTexto());
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setIconTextGap(8);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 18, 10, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                sobre = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sobre = false;
                pressionado = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressionado = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressionado = false;
                repaint();
            }
        });
    }

    public Estilo getEstilo() {
        return estilo;
    }

    private Color corTexto() {
        switch (estilo) {
            case CONTORNO:
                return Tema.SECUNDARIA;
            case FANTASMA:
                return Tema.PRIMARIA;
            default:
                return Color.WHITE;
        }
    }

    private Color corFundo() {
        boolean ativo = sobre && isEnabled();
        switch (estilo) {
            case PRIMARIO:
                return ativo ? Tema.PRIMARIA_ESCURA : Tema.PRIMARIA;
            case SECUNDARIO:
                return ativo ? Tema.MARINHO : Tema.SECUNDARIA;
            case PERIGO:
                return ativo ? new Color(0xC0392B) : Tema.VERMELHO;
            case CONTORNO:
                return ativo ? Tema.LINHA_ALTERNADA : Color.WHITE;
            default:
                return ativo ? Tema.PRIMARIA_SUAVE : null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Tema.suavizar(g2);
        if (!isEnabled()) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));

        double deslocamento = pressionado ? 1 : 0;
        RoundRectangle2D forma = new RoundRectangle2D.Double(0.5, 0.5 + deslocamento, getWidth() - 1, getHeight() - 1.5, 12, 12);

        Color fundo = corFundo();
        if (fundo != null) {
            g2.setColor(fundo);
            g2.fill(forma);
        }
        if (estilo == Estilo.CONTORNO) {
            g2.setColor(sobre && isEnabled() ? Tema.BORDA_FORTE : Tema.BORDA);
            g2.draw(forma);
        }
        g2.dispose();

        // Texto e ícone desenhados pelo próprio JButton (com transparência se estiver desabilitado)
        Graphics2D gt = (Graphics2D) g.create();
        Tema.suavizar(gt);
        if (!isEnabled()) gt.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        gt.translate(0, deslocamento);
        super.paintComponent(gt);
        gt.dispose();
    }

    @Override
    public void setEnabled(boolean habilitado) {
        super.setEnabled(habilitado);
        // Mantém a cor do texto mesmo desabilitado (a transparência já indica o estado)
        setForeground(corTexto());
        setCursor(Cursor.getPredefinedCursor(habilitado ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }
}
