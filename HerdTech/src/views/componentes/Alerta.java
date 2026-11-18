package views.componentes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Mensagens de sucesso, erro e confirmação no visual do sistema (substituem o JOptionPane)
public final class Alerta extends DialogoBase {

    private enum Tipo {
        SUCESSO(Tema.PRIMARIA, "Tudo certo!"),
        ERRO(Tema.VERMELHO, "Ops! Algo deu errado"),
        CONFIRMACAO(Tema.LARANJA, "Tem certeza?");

        private final Color cor;
        private final String chamada;

        Tipo(Color cor, String chamada) {
            this.cor = cor;
            this.chamada = chamada;
        }
    }

    private boolean confirmado;

    private Alerta(Component pai, Tipo tipo, String titulo, String mensagem, String textoOk, String textoCancelar) {
        super(pai, titulo, tipo.cor);

        // Ícone redondo
        Icon simbolo;
        if (tipo == Tipo.SUCESSO) simbolo = Icones.confirmar(26, tipo.cor);
        else if (tipo == Tipo.ERRO) simbolo = Icones.exclamacao(26, tipo.cor);
        else simbolo = Icones.interrogacao(26, tipo.cor);

        JComponent circulo = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.suavizar(g2);
                g2.setColor(Tema.clarear(tipo.cor, 0.86));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                simbolo.paintIcon(this, g, (getWidth() - 26) / 2, (getHeight() - 26) / 2);
            }
        };
        circulo.setPreferredSize(new Dimension(54, 54));
        JPanel colunaIcone = new JPanel(new BorderLayout());
        colunaIcone.setOpaque(false);
        colunaIcone.add(circulo, BorderLayout.NORTH);

        // Textos
        JLabel contexto = new JLabel(titulo == null ? "" : titulo.toUpperCase());
        contexto.setFont(Tema.fonte(Font.BOLD, 11));
        contexto.setForeground(Tema.TEXTO_SUAVE);

        JLabel chamada = new JLabel(tipo.chamada);
        chamada.setFont(Tema.fonte(Font.BOLD, 19));
        chamada.setForeground(Tema.SECUNDARIA);

        JTextArea texto = new JTextArea(mensagem == null ? "" : mensagem.trim());
        texto.setFont(Tema.fonte(Font.PLAIN, 14));
        texto.setForeground(Tema.TEXTO);
        texto.setEditable(false);
        texto.setFocusable(false);
        texto.setOpaque(false);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setColumns(30);
        texto.setBorder(null);
        // Define a largura antes do pack para a quebra de linha calcular a altura certa
        texto.setSize(new Dimension(340, Short.MAX_VALUE));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{contexto, chamada, texto}) c.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(contexto);
        textos.add(Box.createVerticalStrut(4));
        textos.add(chamada);
        textos.add(Box.createVerticalStrut(10));
        textos.add(texto);

        JPanel corpo = new JPanel(new BorderLayout(18, 0));
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(24, 24, 22, 28));
        corpo.add(colunaIcone, BorderLayout.WEST);
        corpo.add(textos, BorderLayout.CENTER);
        permitirArrastar(corpo);

        // Botões
        Botao ok = new Botao(textoOk, tipo == Tipo.CONFIRMACAO ? Botao.Estilo.PERIGO
                : tipo == Tipo.ERRO ? Botao.Estilo.SECUNDARIO : Botao.Estilo.PRIMARIO);
        ok.addActionListener(e -> {
            confirmado = true;
            dispose();
        });

        JPanel rodape;
        if (textoCancelar != null) {
            Botao cancelar = new Botao(textoCancelar, Botao.Estilo.CONTORNO);
            cancelar.addActionListener(e -> aoCancelar());
            rodape = rodape(cancelar, ok);
        } else {
            rodape = rodape(ok);
        }

        raiz.add(corpo, BorderLayout.CENTER);
        raiz.add(rodape, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);

        centralizar();
        setSize(Math.max(getWidth(), 460), getHeight());
        setLocationRelativeTo(getOwner());
    }

    public static void sucesso(Component pai, String titulo, String mensagem) {
        new Alerta(pai, Tipo.SUCESSO, titulo, mensagem, "OK", null).setVisible(true);
    }

    public static void erro(Component pai, String titulo, String mensagem) {
        new Alerta(pai, Tipo.ERRO, titulo, mensagem, "Entendi", null).setVisible(true);
    }

    public static boolean confirmar(Component pai, String titulo, String mensagem, String textoConfirmar) {
        Alerta alerta = new Alerta(pai, Tipo.CONFIRMACAO, titulo, mensagem, textoConfirmar, "Cancelar");
        alerta.setVisible(true);
        return alerta.confirmado;
    }
}
