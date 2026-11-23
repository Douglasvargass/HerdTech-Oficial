package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class Utilidade {
    // Formato de data usado em todo o sistema (dd/MM/aaaa)
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static Long converterLong (String dado) {
        try {
            return Long.parseLong(dado);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer converterInteger (String dado) {
        try {
            return Integer.parseInt(dado);
        } catch (Exception e) {
            return null;
        }
    }

    // Converte "dd/MM/aaaa" em LocalDate (retorna null se a data for inválida)
    public static LocalDate converterData (String dado) {
        try {
            return LocalDate.parse(dado.trim(), FORMATO_DATA);
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean dataValida (String dado) {
        return converterData(dado) != null;
    }

    // Mantém só os números (ex.: "123.456.789-00" -> "12345678900")
    public static String somenteNumeros (String dado) {
        return dado == null ? "" : dado.replaceAll("\\D", "");
    }

    // Formata o CPF salvo como número para exibição (ex.: 12345678900 -> 123.456.789-00)
    public static String formatarCpf (Long cpf) {
        if (cpf == null) return "";
        String numeros = String.format("%011d", cpf);
        return numeros.substring(0, 3) + "." + numeros.substring(3, 6) + "."
                + numeros.substring(6, 9) + "-" + numeros.substring(9);
    }
}
