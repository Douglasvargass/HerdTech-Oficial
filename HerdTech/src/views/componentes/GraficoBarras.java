package views.componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Gráfico de barras horizontais simples (usado na tela inicial e nos relatórios)
public class GraficoBarras extends JComponent {
    private static final int ALTURA_LINHA = 38;

    private final List<String> rotulos = new ArrayList<>();
    private final List<Integer> valores = new ArrayList<>();
    private final List<Color> cores = new ArrayList<>();
    private final Color corPadrao;

    public GraficoBarras(Color corPadrao) {
        this.corPadrao = corPadrao;
        setFont(Tema.fonte(Font.PLAIN, 13));
    }

    public void limpar() {
        rotulos.clear();
        valores.clear();
        cores.clear();
    }

    public void adicionar(String rotulo, int valor, Color cor) {
        rotulos.add(rotulo);
        valores.add(valor);
        cores.add(cor == null ? corPadrao : cor);
    }

    // Preenche com um mapa (rótulo -> quantidade), do maior para o menor
    public void definir(Map<String, Integer> dados) {
        limpar();
        dados.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(item -> adicionar(item.getKey(), item.getValue(), null));
        revalidate();
        repaint();
    }

    public void atualizar() {
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(320, Math.max(80, rotulos.size() * ALTURA_LINHA + 6));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Tema.suavizar(g2);
        int largura = getWidth();

        if (rotulos.isEmpty()) {
            g2.setFont(Tema.fonte(Font.PLAIN, 13));
            g2.setColor(Tema.TEXTO_SUAVE);
            String texto = "Sem dados para exibir";
            FontMetrics m = g2.getFontMetrics();
            g2.drawString(texto, (largura - m.stringWidth(texto)) / 2, getHeight() / 2);
            g2.dispose();
            return;
        }

        int maximo = 1;
        for (int valor : valores) maximo = Math.max(maximo, valor);

        int larguraRotulo = Math.min(170, (int) (largura * 0.36));
        int larguraValor = 44;
        int inicioBarra = larguraRotulo + 12;
        int larguraBarra = Math.max(20, largura - inicioBarra - larguraValor);

        Font fonteRotulo = Tema.fonte(Font.PLAIN, 13);
        Font fonteValor = Tema.fonte(Font.BOLD, 13);

        for (int i = 0; i < rotulos.size(); i++) {
            int y = i * ALTURA_LINHA;
            int centro = y + ALTURA_LINHA / 2;

            // Rótulo (cortado com "..." se for muito grande)
            g2.setFont(fonteRotulo);
            g2.setColor(Tema.TEXTO);
            FontMetrics m = g2.getFontMetrics();
            String rotulo = cortar(rotulos.get(i), m, larguraRotulo);
            g2.drawString(rotulo, 0, centro + (m.getAscent() - m.getDescent()) / 2);

            // Trilho e barra
            g2.setColor(new Color(0xF1F5F9));
            g2.fill(new RoundRectangle2D.Double(inicioBarra, centro - 6, larguraBarra, 12, 12, 12));
            double preenchido = Math.max(12, larguraBarra * valores.get(i) / (double) maximo);
            if (valores.get(i) == 0) preenchido = 0;
            g2.setColor(cores.get(i));
            g2.fill(new RoundRectangle2D.Double(inicioBarra, centro - 6, preenchido, 12, 12, 12));

            // Valor
            g2.setFont(fonteValor);
            g2.setColor(Tema.SECUNDARIA);
            m = g2.getFontMetrics();
            String valor = String.valueOf(valores.get(i));
            g2.drawString(valor, largura - m.stringWidth(valor), centro + (m.getAscent() - m.getDescent()) / 2);
        }
        g2.dispose();
    }

    private String cortar(String texto, FontMetrics medidas, int largura) {
        if (medidas.stringWidth(texto) <= largura) return texto;
        String reticencias = "...";
        while (texto.length() > 1 && medidas.stringWidth(texto + reticencias) > largura) {
            texto = texto.substring(0, texto.length() - 1);
        }
        return texto + reticencias;
    }
}
