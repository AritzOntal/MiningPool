package util;

public class LogUtil {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String PROMPT = "\u001B[31m" + "\n> [Escribe 'salir' para desconectar]: " + ANSI_RESET;

    public static void logClients(String mensaje) {
        System.out.print("\r" + ANSI_CYAN + "[SISTEMA] " + mensaje + ANSI_RESET + PROMPT);
    }

    public static void logServer(String mensaje) {
        System.out.print("\r" + ANSI_YELLOW + mensaje + ANSI_RESET);
    }


}
