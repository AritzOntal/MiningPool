package server;

import util.LogUtil;

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

    //EL MÉTODO QUE USARÁ EL SERVIDOR PARA ENVIARLE MENSJAES AL CLIENTE
    public void enviarMensaje(String texto) {
        if (out != null) {
            LogUtil.logServer("Enviando bloque al nuevo cliente...");
            out.println(texto);
            out.flush();
        }
    }

    @Override
    public void run() {
        //CREAMOS CANALES DE ENTRADA Y SALIDA (IN Y OUT).
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            this.out = new PrintWriter(socket.getOutputStream(), true);

            // ESPERAMOS AL "CONNECT" DEL CLIENTE
            String msg = in.readLine();
            if (msg != null && msg.equalsIgnoreCase("connect")) {
                //ME PASO A MI MISMO PARA AÑADIRME A LA LISTA
                servidor.añadirALista(this);
                enviarMensaje("ack"); // Respondemos confirmación

                //AHORA QUE TENGO EL CLIENTE GUARDADO Y ESTOY LISTO ENVIO TRABAJO AL CLIENTE
                servidor.enviarTrabajoACliente(this);

                //EL HILO MANIENE LA CONEXIÓN Y ESPERA MÁS MENSAJES (mientras haya flujo de mensajes)
                while ((msg = in.readLine()) != null) {
                    //AQUÍ RECIBIREMOS LA SOLUCIÓN "sol"

                    if (msg != null && msg.startsWith("sol")) {
                        String[] partes = msg.split(";");

                        if (partes.length == 3) {
                            String nonce = partes[1];
                            String hash = partes[2];
                            System.out.println("Nonce del cliente: " + nonce);
                            servidor.enviarBroadcast(nonce, hash);
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Un cliente se ha ido.");

        } finally {
            servidor.eliminarDeLista(this);
        }
    }
}