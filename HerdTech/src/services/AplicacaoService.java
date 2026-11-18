package services;

import dao.AplicacaoDAO;
import exceptions.AplicacaoException;
import models.Aplicacao;
import models.Gado;
import utils.Utilidade;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AplicacaoService {
    // O Service usa o DAO para falar com o banco
    private final AplicacaoDAO aplicacaoDAO = new AplicacaoDAO();

    // Lista usada pelas tabelas da janela
    public ArrayList<Aplicacao> listarTodos() {
        return aplicacaoDAO.listar();
    }

    // Para cada animal + vacina, fica só a aplicação mais recente
    // (as anteriores já foram "renovadas" e não contam como dose pendente)
    public static ArrayList<Aplicacao> ultimasDoses(List<Aplicacao> aplicacoes) {
        Map<String, Aplicacao> ultimas = new LinkedHashMap<>();
        for (Aplicacao aplicacao : aplicacoes) {
            String chave = aplicacao.getGado().getId() + "|" + aplicacao.getVacina().trim().toLowerCase();
            Aplicacao atual = ultimas.get(chave);
            if (atual == null || maisRecente(aplicacao, atual)) ultimas.put(chave, aplicacao);
        }
        return new ArrayList<>(ultimas.values());
    }

    private static boolean maisRecente(Aplicacao a, Aplicacao b) {
        LocalDate dataA = Utilidade.converterData(a.getDataAplicacao());
        LocalDate dataB = Utilidade.converterData(b.getDataAplicacao());
        if (dataA != null && dataB != null && !dataA.equals(dataB)) return dataA.isAfter(dataB);
        if (dataA != null && dataB == null) return true;
        if (dataA == null && dataB != null) return false;
        return a.getId() > b.getId();
    }

    public String listar() {
        String lista = "";
        ArrayList<Aplicacao> aplicacoes = aplicacaoDAO.listar();

        if (aplicacoes.size() > 0) {
            for (Aplicacao aplicacao : aplicacoes) {
                lista += aplicacao + "\n";
            }
        } else {
            lista = "Nenhuma Aplicação de Vacina encontrada!";
        }
        return lista;
    }

    public Boolean cadastrar (Aplicacao aplicacao) throws AplicacaoException {
        if (validarDados(aplicacao.getGado(), aplicacao.getVacina(), aplicacao.getDataAplicacao(), aplicacao.getProximaVacina(), aplicacao.getQuemAplicou())) {
            return aplicacaoDAO.cadastrar(aplicacao);
        }
        return false;
    }

    private Boolean validarDados (Gado gado, String vacina, String dataAplicacao, String proximaAplicacao, String quemAplicou) throws AplicacaoException {
        String erros = "";

        if (gado == null) erros += "\n- Selecione o Animal que recebeu a vacina!";
        if (vacina.trim().isEmpty()) erros += "\n- Campo 'Nome da Vacina' é obrigatório!";
        if (dataAplicacao.trim().isEmpty()) erros += "\n- Campo 'Data da Aplicação' é obrigatório!";
        else if (!Utilidade.dataValida(dataAplicacao)) erros += "\n- Campo 'Data da Aplicação' inválido (use dd/mm/aaaa)!";
        if (proximaAplicacao.trim().isEmpty()) erros += "\n- Campo 'Próxima vacina (data)' é obrigatório!";
        else if (!Utilidade.dataValida(proximaAplicacao)) erros += "\n- Campo 'Próxima vacina (data)' inválido (use dd/mm/aaaa)!";
        if (quemAplicou.trim().isEmpty()) erros += "\n- Campo 'Quem aplicou? (Veterinário ou Dono)' é obrigatório!";

        if (!erros.isEmpty()) {
            throw new AplicacaoException("Ops! Erros Encontrados: " + erros);
        }
        return true;
    }

    public Aplicacao buscarPorId (Integer id) {
        return aplicacaoDAO.buscarPorId(id);
    }

    public Boolean excluir(Integer id) {
        return aplicacaoDAO.excluir(id);
    }

    public Boolean alterar(Aplicacao aplicacao) throws AplicacaoException {
        if (validarDados(aplicacao.getGado(), aplicacao.getVacina(), aplicacao.getDataAplicacao(), aplicacao.getProximaVacina(), aplicacao.getQuemAplicou())) {
            return aplicacaoDAO.alterar(aplicacao);
        }
        return false;
    }
}
