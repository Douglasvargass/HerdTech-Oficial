package views.componentes;

import utils.Utilidade;

import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Situação da próxima dose de uma vacina (mesmas cores de status do Front)
public enum SituacaoVacina {
    ATRASADA("Atrasada", Tema.VERMELHO),
    HOJE("Hoje", Tema.VERMELHO),
    AMANHA("Amanhã", Tema.LARANJA),
    EM_BREVE("Em breve", Tema.AZUL),
    EM_DIA("Em dia", Tema.PRIMARIA),
    RENOVADA("Renovada", Tema.TEXTO_SUAVE),
    SEM_DATA("Sem data", Tema.TEXTO_SUAVE);

    // Até quantos dias antes a dose aparece como "Em breve"
    public static final int DIAS_EM_BREVE = 30;

    private final String rotulo;
    private final Color cor;

    SituacaoVacina(String rotulo, Color cor) {
        this.rotulo = rotulo;
        this.cor = cor;
    }

    public String getRotulo() {
        return rotulo;
    }

    public Color getCor() {
        return cor;
    }

    // Doses que precisam de atenção do produtor
    public boolean exigeAtencao() {
        return this == ATRASADA || this == HOJE || this == AMANHA || this == EM_BREVE;
    }

    public static SituacaoVacina de(String proximaVacina) {
        LocalDate data = Utilidade.converterData(proximaVacina);
        if (data == null) return SEM_DATA;

        long dias = ChronoUnit.DAYS.between(LocalDate.now(), data);
        if (dias < 0) return ATRASADA;
        if (dias == 0) return HOJE;
        if (dias == 1) return AMANHA;
        if (dias <= DIAS_EM_BREVE) return EM_BREVE;
        return EM_DIA;
    }

    // Texto amigável do prazo (ex.: "em 5 dias", "há 2 dias")
    public static String prazo(String proximaVacina) {
        LocalDate data = Utilidade.converterData(proximaVacina);
        if (data == null) return "";

        long dias = ChronoUnit.DAYS.between(LocalDate.now(), data);
        if (dias == 0) return "hoje";
        if (dias == 1) return "amanhã";
        if (dias == -1) return "ontem";
        if (dias < 0) return "há " + (-dias) + " dias";
        return "em " + dias + " dias";
    }
}
