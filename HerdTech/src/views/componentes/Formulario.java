package views.componentes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Janela de cadastro/edição: todos os campos numa tela só (antes era um JOptionPane por campo)
public class Formulario extends DialogoBase {
    private final JPanel corpo = new JPanel(new GridBagLayout());
    private int linha = 0;
    private boolean confirmado;

    public Formulario(Component pai, String titulo, String subtitulo, Icon icone) {
        super(pai, titulo, Tema.PRIMARIA);

        // Cabeçalho
        JPanel cabecalho = new JPanel(new BorderLayout(14, 0));
        cabecalho.setOpaque(false);
        cabecalho.setBorder(new EmptyBorder(20, 24, 6, 14));

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(Tema.titulo(titulo, 19));
        textos.add(Tema.textoSuave(subtitulo, 13));

        Botao fechar = new Botao("", Icones.fechar(18, Tema.TEXTO_SUAVE), Botao.Estilo.FANTASMA);
        fechar.setBorder(new EmptyBorder(8, 8, 8, 8));
        fechar.setToolTipText("Fechar (Esc)");
        fechar.addActionListener(e -> aoCancelar());
        JPanel colunaFechar = new JPanel(new BorderLayout());
        colunaFechar.setOpaque(false);
        colunaFechar.add(fechar, BorderLayout.NORTH);

        cabecalho.add(Icones.bloco(icone, Tema.PRIMARIA, 46), BorderLayout.WEST);
        cabecalho.add(textos, BorderLayout.CENTER);
        cabecalho.add(colunaFechar, BorderLayout.EAST);
        permitirArrastar(cabecalho);

        // Corpo com os campos
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(14, 24, 22, 24));

        // Rodapé
        Botao cancelar = new Botao("Cancelar", Botao.Estilo.CONTORNO);
        cancelar.addActionListener(e -> aoCancelar());
        Botao salvar = new Botao("Salvar", Icones.confirmar(16, Color.WHITE), Botao.Estilo.PRIMARIO);
        salvar.addActionListener(e -> {
            confirmado = true;
            setVisible(false);
        });
        getRootPane().setDefaultButton(salvar);

        raiz.add(cabecalho, BorderLayout.NORTH);
        raiz.add(corpo, BorderLayout.CENTER);
        raiz.add(rodape(cancelar, salvar), BorderLayout.SOUTH);
    }

    @Override
    protected void aoCancelar() {
        confirmado = false;
        setVisible(false);
    }

    // Campo ocupando a largura toda
    public void adicionarCampo(String rotulo, JComponent campo) {
        adicionarCampo(rotulo, campo, null);
    }

    public void adicionarCampo(String rotulo, JComponent campo, String dica) {
        corpo.add(montarCampo(rotulo, campo, dica), restricao(0, 2));
        linha++;
    }

    // Dois campos lado a lado
    public void adicionarLinha(String rotulo1, JComponent campo1, String rotulo2, JComponent campo2) {
        GridBagConstraints esquerda = restricao(0, 1);
        esquerda.insets = new Insets(0, 0, 14, 7);
        GridBagConstraints direita = restricao(1, 1);
        direita.insets = new Insets(0, 7, 14, 0);
        corpo.add(montarCampo(rotulo1, campo1, null), esquerda);
        corpo.add(montarCampo(rotulo2, campo2, null), direita);
        linha++;
    }

    private JPanel montarCampo(String rotulo, JComponent campo, String dica) {
        JPanel bloco = new JPanel(new BorderLayout(0, 6));
        bloco.setOpaque(false);
        bloco.add(Tema.rotuloCampo(rotulo), BorderLayout.NORTH);
        bloco.add(campo, BorderLayout.CENTER);
        if (dica != null) {
            JLabel textoDica = Tema.textoSuave(dica, 12);
            bloco.add(textoDica, BorderLayout.SOUTH);
        }
        return bloco;
    }

    private GridBagConstraints restricao(int coluna, int largura) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = coluna;
        c.gridy = linha;
        c.gridwidth = largura;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 14, 0);
        return c;
    }

    // Mostra a janela e espera o usuário: true = Salvar, false = Cancelar
    public boolean exibir() {
        confirmado = false;
        centralizar();
        setSize(Math.max(getWidth(), 540), getHeight());
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return confirmado;
    }
}
