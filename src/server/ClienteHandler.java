package server;

import java.io.*;
import java.net.Socket;

public class ClienteHandler implements Runnable {
    private Socket socket;
    private MiningServer servidor;
    private PrintWriter out;

    //CONSTRUCTOR CON UNA COPIA DE LA CONEXION ENTRE SERVIDO Y CLIENTE
    public ClienteHandler(Socket socket, MiningServer servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }

    //EL MÉTODO QUE USARÁ EL SERVIDOR PARA ENVIARLE COSAS AL CLIENTE
    public void enviarMensaje(String texto) {
        if (out != null) {
            out.println(texto);
        }
    }

    @Override
    public void run() {

        //CREAMOS CANALES DE ENTRADA Y SALIDA (IN Y OUT).
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            this.out = new PrintWriter(socket.getOutputStream(), true);

            // ESPERAMOS AL "CONNECT" DEL CLIENTE
            String msg = in.readLine();
            if ("connect".equals(msg)) {
                //ME PASO A MI MISMO PARA AÑADIRME A LA LISTA
                servidor.añadirALista(this);
                enviarMensaje("ack"); // Respondemos confirmación
                System.out.println("Cliente validado y añadido a la lista.");

                //EL HILO MANIENE LA CONEXIÓN Y ESPERA MÁS MENSAJES (mientras haya flujo de mensajes)
                while ((msg = in.readLine()) != null) {
                    System.out.println("Mensaje del cliente: " + msg);
                    //AQUÍ RECIBIRÍAMOS LA SOLUCIÓN "sol"
                }
            }

        } catch (IOException e) {
            System.out.println("Un cliente se ha ido.");

        } finally {
            servidor.eliminarDeLista(this);
        }
    }
}