package views.componentes;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Janela modal sem a barra do Windows, com faixa colorida no topo e que pode ser arrastada
public abstract class DialogoBase extends JDialog {
    protected final JPanel raiz = new JPanel(new BorderLayout());
    private Point inicioArraste;

    protected DialogoBase(Component pai, String titulo, Color destaque) {
        super(Tema.janela(pai), titulo, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        raiz.setBackground(Color.WHITE);
        raiz.setBorder(new CompoundBorder(
                new LineBorder(Tema.BORDA_FORTE, 1),
                new MatteBorder(4, 0, 0, 0, destaque)));
        setContentPane(raiz);

        // ESC fecha a janela
        raiz.registerKeyboardAction(e -> aoCancelar(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    protected void aoCancelar() {
        dispose();
    }

    // Permite arrastar a janela segurando o componente informado (ex.: cabeçalho)
    protected void permitirArrastar(JComponent alca) {
        MouseAdapter arrastar = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                inicioArraste = e.getLocationOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (inicioArraste == null) return;
                Point agora = e.getLocationOnScreen();
                Point local = getLocation();
                setLocation(local.x + agora.x - inicioArraste.x, local.y + agora.y - inicioArraste.y);
                inicioArraste = agora;
            }
        };
        alca.addMouseListener(arrastar);
        alca.addMouseMotionListener(arrastar);
    }

    protected JPanel rodape(Botao... botoes) {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rodape.setBackground(Tema.LINHA_ALTERNADA);
        rodape.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Tema.BORDA), new EmptyBorder(14, 18, 14, 14)));
        for (Botao botao : botoes) rodape.add(botao);
        return rodape;
    }

    protected void centralizar() {
        pack();
        setLocationRelativeTo(getOwner());
    }
}
