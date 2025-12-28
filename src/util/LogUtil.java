package util;

public class LogUtil {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String PROMPT = "\u001B[31m" + "\n> [Escribe 'salir' para desconectar]: " + ANSI_RESET;

    public static boolean mostrarPrompt = true;

    public static void logC(String mensaje) {
        System.out.println("\r" + ANSI_CYAN + mensaje + ANSI_RESET);
    }

    public static void logY(String mensaje) {
        System.out.println("\r" + ANSI_YELLOW + mensaje + ANSI_RESET);
    }

    public static void logG(String mensaje) {
        System.out.print("\r" + ANSI_GREEN + mensaje + ANSI_RESET);
    }

    public static void setPrompt() {
            System.out.print(PROMPT + ANSI_RESET);
        }
    }

