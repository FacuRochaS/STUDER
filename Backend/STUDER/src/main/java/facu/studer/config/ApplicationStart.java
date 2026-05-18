package facu.studer.config;

import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * ApplicationStart is a Spring component
 * that listens for the ContextRefreshedEvent.
 * When the application context is refreshed,
 * it prints an ASCII art banner to the console.
 */

@Component
@NoArgsConstructor
public class ApplicationStart implements ApplicationListener<ContextRefreshedEvent> {

    // ANSI helpers (truecolor)
    private static String fg(int r, int g, int b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }
    private static final String RESET = "\u001B[0m";

    // Tus colores (hex -> RGB)
    private static final String C_PRIMARY = fg(0x42, 0x85, 0xF4); // #4285f4
    private static final String C_CORRECT = fg(0x00, 0xBF, 0x63); // #00bf63
    private static final String C_ERROR   = fg(0xFF, 0x31, 0x31); // #ff3131
    private static final String C_WARNING = fg(0xFF, 0x75, 0x1F); // #ff751f
    private static final String C_INFO    = fg(0x7E, 0x57, 0xC2); // #7E57C2
    private static final String C_WHITE   = fg(0xFF, 0xFF, 0xFF); // blanco

    private static String rpad(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }

    private static int maxLen(String[] a) {
        int m = 0;
        for (String s : a) m = Math.max(m, s.length());
        return m;
    }

    private static String[] padToSameWidth(String[] a) {
        int w = maxLen(a);
        String[] out = new String[a.length];
        for (int i = 0; i < a.length; i++) out[i] = rpad(a[i], w);
        return out;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        String label = "Trabajo final · FACUNDO ROCHA SERET · Legajo 412108";

        // Label con colores alternados (usando tus 6 colores, el último ahora es blanco)
        String[] labelColors = { C_PRIMARY, C_CORRECT, C_ERROR, C_WARNING, C_INFO, C_WHITE };

        StringBuilder coloredLabel = new StringBuilder();
        int c = 0;
        for (char ch : label.toCharArray()) {
            coloredLabel.append(labelColors[c % labelColors.length]).append(ch);
            c++;
        }
        coloredLabel.append(RESET);

        // ASCII por letra: S T U D E R (OJO: cada bloque se va a paddear a ancho uniforme)
        String[] S = {
                "  _____ ",
                " / ___/|",
                "(   \\_ ",
                " \\__  |",
                " /  \\ |",
                " \\    |",
                "  \\___|"
        };

        String[] T = {
                "______ ",
                "|      |",
                "|      |",
                "|_|  |_|",
                "  |  |  ",
                "  |  |  ",
                "  |__|  "
        };

        String[] U = {
                " __ __ ",
                "||  |  |",
                "||  |  |",
                "||  |  |",
                "|  :  | ",
                "|     | ",
                " \\__,_| "
        };

        String[] D = {
                " ___      ",
                "||   \\    ",
                "||    \\   ",
                "||  D  || ",
                "||     || ",
                "||     || ",
                "||_____|| "
        };

        String[] E = {
                " ___  ",
                "/  _]|",
                "/  [_ ",
                "|    _]",
                "|   [_ ",
                "|     |",
                "|_____||"
        };

        String[] R = {
                " ____  ",
                "|    \\ ",
                "|  D  )",
                "|    / ",
                "|    \\ ",
                "|  .  \\",
                "|__|\\_|"
        };

        // Paddeamos cada letra a ancho uniforme (así no “deforma” al concatenar)
        S = padToSameWidth(S);
        T = padToSameWidth(T);
        U = padToSameWidth(U);
        D = padToSameWidth(D);
        E = padToSameWidth(E);
        R = padToSameWidth(R);

        StringBuilder out = new StringBuilder();

        for (int i = 0; i < 7; i++) {
            out.append(C_PRIMARY).append(S[i]).append(RESET).append(" ")
                    .append(C_CORRECT).append(T[i]).append(RESET).append(" ")
                    .append(C_ERROR).append(U[i]).append(RESET).append(" ")
                    .append(C_WARNING).append(D[i]).append(RESET).append(" ")
                    .append(C_INFO).append(E[i]).append(RESET).append(" ")
                    .append(C_WHITE).append(R[i]).append(RESET);

            if (i == 0) out.append("   ").append(coloredLabel);

            out.append("\n");
        }

        System.out.println(out.toString());
    }
}


