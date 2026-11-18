package views;

import models.Cliente;
import utils.Utilidade;
import views.componentes.Formulario;
import views.componentes.Icones;
import views.componentes.Tema;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteView extends TelaLista {

    public ClienteView() {
        super("Clientes e Fazendas",
                "Cadastre os produtores e as fazendas onde o rebanho é criado.",
                "Novo Cliente",
                "Nenhum cliente cadastrado ainda. Clique em \"Novo Cliente\" para começar.",
                new String[]{"ID", "Nome", "CPF", "Nascimento", "Fazenda", "Endereço"});
        larguras(70, 200, 140, 110, 190, 260);
    }

    public void atualizarTabela(List<Cliente> clientes) {
        List<Object[]> linhas = new ArrayList<>();
        for (Cliente cliente : clientes) {
            linhas.add(new Object[]{
                    cliente.getId(),
                    cliente.getNome(),
                    Utilidade.formatarCpf(cliente.getCpf()),
                    cliente.getDataNasc(),
                    cliente.getNomeFazenda(),
                    cliente.getEndereco()
            });
        }
        preencher(linhas);
    }

    // Abre o formulário com os dados do cliente. Retorna null se o usuário cancelar.
    public Cliente informarDados(String titulo, Cliente cliente) {
        boolean novo = cliente.getId() == null;
        Formulario formulario = new Formulario(this,
                novo ? "Novo Cliente" : "Alterar Cliente",
                novo ? "Preencha os dados do produtor e da fazenda." : "Cliente #" + cliente.getId(),
                Icones.pessoa(22, Tema.PRIMARIA));

        JTextField nome = Tema.campoTexto(cliente.getNome());
        JFormattedTextField cpf = Tema.campoMascara("###.###.###-##",
                cliente.getCpf() == null ? null : Utilidade.formatarCpf(cliente.getCpf()));
        JFormattedTextField dataNasc = Tema.campoMascara("##/##/####", cliente.getDataNasc());
        JTextField nomeFazenda = Tema.campoTexto(cliente.getNomeFazenda());
        JTextField endereco = Tema.campoTexto(cliente.getEndereco());

        formulario.adicionarCampo("Nome completo", nome);
        formulario.adicionarLinha("CPF", cpf, "Data de Nascimento", dataNasc);
        formulario.adicionarCampo("Nome da Fazenda", nomeFazenda);
        formulario.adicionarCampo("Endereço", endereco, "Ex.: Rodovia SC-390, km 12 - Tubarão/SC");

        boolean salvar = formulario.exibir();
        formulario.dispose();
        if (!salvar) return null;

        String numerosCpf = Utilidade.somenteNumeros(cpf.getText());

        cliente.setNome(nome.getText().trim());
        cliente.setCpf(numerosCpf.length() == 11 ? Utilidade.converterLong(numerosCpf) : null);
        cliente.setDataNasc(Tema.textoMascara(dataNasc));
        cliente.setNomeFazenda(nomeFazenda.getText().trim());
        cliente.setEndereco(endereco.getText().trim());

        return cliente;
    }
}
