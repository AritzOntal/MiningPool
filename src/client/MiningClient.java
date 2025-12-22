package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MiningClient {
        public static void main(String[] args) {
            String host = "localhost";
            int port = 6666;
            MiningClient miningClient = new MiningClient();

            Scanner sc = new Scanner(System.in);
            System.out.println ("Cliente inciado");
            System.out.println ("¿Desea conectarse al servidor?(s/n)");
            if(sc.nextLine().equals("s")){
                miningClient.conectarServidor(host, port);

            } else {
                return;
            }
        }

    public void conectarServidor(String host, int port) {
    //ABRIMOS LE SOCKET Y LOS CANALES DE COMUNICACIÓN (POR EL SOCKET SOMOS CAPAZ DE ENVIAR AL HANDLER DEL SERVIDOR MENSAJES CLAVE).
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Conectado al servidor.");

    //ENVIAMOS MENSAJE CLAVE CONNECT
            out.println("connect");
            System.out.println("Mensaje 'connect' enviado. Esperando respuesta...");

            //LEEMOS LA RESPUESTA (Debería ser "ack")
            String respuesta = in.readLine();
            if ("ack".equals(respuesta)) {
                System.out.println("¡Servidor nos ha aceptado! (Recibido: " + respuesta + ")");

                // Aquí es donde el cliente se queda esperando el "new_request"
                while (true) {
                    System.out.println("Esperando tarea de minería...");
                    String tarea = in.readLine(); // El hilo se queda aquí parado esperando al servidor

                    if (tarea != null && tarea.startsWith("new_request")) {
                        System.out.println("¡Tarea recibida!: " + tarea);
                        // TODO LLAMAR A ALGO QUE EMPIECE A MINAR LA TAREA.
                    }
                }
            } else {
                System.out.println("El servidor respondió algo inesperado: " + respuesta);
            }

        } catch (IOException e) {
            System.out.println("Error en la conexión: " + e.getMessage());
        }
    }
}
