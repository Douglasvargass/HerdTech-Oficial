package views.componentes;

import javax.swing.*;
import java.awt.*;

// Campo de pesquisa com lupa e texto de exemplo
public class CampoBusca extends JTextField {
    private final String exemplo;
    private final Icon lupa = Icones.busca(16, Tema.TEXTO_SUAVE);

    public CampoBusca(String exemplo) {
        this.exemplo = exemplo;
        Tema.estilizarCampo(this, new Insets(8, 36, 8, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        lupa.paintIcon(this, g, 14, (getHeight() - lupa.getIconHeight()) / 2);

        if (getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.suavizar(g2);
            g2.setColor(Tema.clarear(Tema.TEXTO_SUAVE, 0.25));
            g2.setFont(getFont());
            FontMetrics medidas = g2.getFontMetrics();
            g2.drawString(exemplo, getInsets().left, (getHeight() + medidas.getAscent() - medidas.getDescent()) / 2);
            g2.dispose();
        }
    }
}
