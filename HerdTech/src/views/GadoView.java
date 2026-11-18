package views;

import models.Cliente;
import models.Gado;
import utils.Utilidade;
import views.componentes.Formulario;
import views.componentes.Icones;
import views.componentes.Tema;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class GadoView extends TelaLista {
    private static final String[] RACAS = {"Nelore", "Angus", "Jersey", "Holandesa", "Gir", "Girolando", "Brahman", "Hereford", "Senepol"};
    private static final String[] GENEROS = {"Fêmea", "Macho"};

    public GadoView() {
        super("Animais",
                "Controle do rebanho: raça, gênero, idade e a qual fazenda cada animal pertence.",
                "Novo Animal",
                "Nenhum animal cadastrado ainda. Clique em \"Novo Animal\" para começar.",
                new String[]{"ID", "Apelido", "Raça", "Gênero", "Nascimento", "Idade", "Dono", "Fazenda"});
        larguras(70, 150, 120, 90, 110, 120, 170, 180);
    }

    public void atualizarTabela(List<Gado> gados) {
        List<Object[]> linhas = new ArrayList<>();
        for (Gado gado : gados) {
            linhas.add(new Object[]{
                    gado.getId(),
                    gado.getNome(),
                    gado.getRaca(),
                    gado.getGenero(),
                    gado.getDataNasc(),
                    idade(gado.getDataNasc()),
                    gado.getDono().getNome(),
                    gado.getDono().getNomeFazenda()
            });
        }
        preencher(linhas);
    }

    // Abre o formulário com os dados do animal. Retorna null se o usuário cancelar.
    public Gado informarDados(String titulo, Gado gado, List<Cliente> clientes) {
        boolean novo = gado.getId() == null;
        Formulario formulario = new Formulario(this,
                novo ? "Novo Animal" : "Alterar Animal",
                novo ? "Informe o dono e os dados do animal." : "Animal #" + gado.getId(),
                Icones.gado(24, Tema.PRIMARIA));

        JComboBox<Cliente> dono = Tema.estilizarCombo(new JComboBox<>(clientes.toArray(new Cliente[0])));
        dono.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean selecionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
                if (valor instanceof Cliente) {
                    Cliente cliente = (Cliente) valor;
                    setText(cliente.getNome() + "  —  " + cliente.getNomeFazenda());
                }
                setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                return this;
            }
        });
        if (gado.getDono() != null) dono.setSelectedItem(gado.getDono());

        JTextField nome = Tema.campoTexto(gado.getNome());
        JComboBox<String> raca = opcoes(RACAS, gado.getRaca(), true);
        JComboBox<String> genero = opcoes(GENEROS, gado.getGenero(), false);
        JFormattedTextField dataNasc = Tema.campoMascara("##/##/####", gado.getDataNasc());

        formulario.adicionarCampo("Dono (Cliente / Fazenda)", dono);
        formulario.adicionarCampo("Apelido do Animal", nome);
        formulario.adicionarLinha("Raça", raca, "Gênero", genero);
        formulario.adicionarCampo("Data de Nascimento", dataNasc, "Formato dd/mm/aaaa");

        boolean salvar = formulario.exibir();
        formulario.dispose();
        if (!salvar) return null;

        Object racaDigitada = raca.getEditor().getItem();

        gado.setDono((Cliente) dono.getSelectedItem());
        gado.setNome(nome.getText().trim());
        gado.setRaca(racaDigitada == null ? "" : racaDigitada.toString().trim());
        gado.setGenero(genero.getSelectedItem() == null ? "" : genero.getSelectedItem().toString());
        gado.setDataNasc(Tema.textoMascara(dataNasc));

        return gado;
    }

    // Lista de opções; se o valor salvo não estiver na lista, ele é incluído
    private JComboBox<String> opcoes(String[] lista, String valorAtual, boolean editavel) {
        JComboBox<String> combo = Tema.estilizarCombo(new JComboBox<>(lista));
        combo.setEditable(editavel);
        if (valorAtual != null && !valorAtual.trim().isEmpty()) {
            boolean existe = false;
            for (String item : lista) existe |= item.equalsIgnoreCase(valorAtual);
            if (!existe) combo.addItem(valorAtual);
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).equalsIgnoreCase(valorAtual)) combo.setSelectedIndex(i);
            }
        }
        return combo;
    }

    // Idade do animal em texto (ex.: "1 ano e 3 meses")
    private String idade(String dataNasc) {
        LocalDate nascimento = Utilidade.converterData(dataNasc);
        if (nascimento == null || nascimento.isAfter(LocalDate.now())) return "-";

        Period periodo = Period.between(nascimento, LocalDate.now());
        int anos = periodo.getYears(), meses = periodo.getMonths();
        if (anos == 0 && meses == 0) return periodo.getDays() + (periodo.getDays() == 1 ? " dia" : " dias");

        String texto = "";
        if (anos > 0) texto += anos + (anos == 1 ? " ano" : " anos");
        if (anos > 0 && meses > 0) texto += " e ";
        if (meses > 0) texto += meses + (meses == 1 ? " mês" : " meses");
        return texto;
    }
}
