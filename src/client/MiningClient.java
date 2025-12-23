package client;

import common.Hasher;
import util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MiningClient {

    private Socket socket;

    public static void main(String[] args) {
        String host = "localhost";
        int port = 6666;
        MiningClient miningClient = new MiningClient();

        Scanner sc = new Scanner(System.in);
        System.out.println("Cliente inciado");
        System.out.println("¿Desea conectarse al servidor?(s/n)");
        if (sc.nextLine().equalsIgnoreCase("s")) {
            //CREAMOS HILO A PARTE PARA NO PERDER EL MAIN
            Thread hiloConexion = new Thread(() -> {
                miningClient.conectarServidor(host, port);
            });
            hiloConexion.start();

            while (true) {
                String comando = sc.nextLine();
                if (comando.equalsIgnoreCase("salir")) {
                    miningClient.desconectarServidor();
                    System.exit(0);
                }
            }
        }
    }


    private volatile boolean seguirMinando = true;

    public void conectarServidor(String host, int port) {
        //ABRIMOS LE SOCKET Y LOS CANALES DE COMUNICACIÓN (POR EL SOCKET SOMOS CAPAZ DE ENVIAR AL HANDLER DEL SERVIDOR MENSAJES CLAVE).
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("Conectando con servidor...");
            this.socket = socket;

            //PEDIMOS CONECATAR (DICIENDO QUE ESTOY LISTO)
            out.println("connect");
            System.out.println("Saludo enviado. Esperando respuesta...");
            //ESPERAMOS LA RESPUESTA DESPUÉS DE PEDIR CONEXIÓN (Debería ser "ack")
            String respuesta = in.readLine();

            if ("ack".equals(respuesta)) {
                System.out.println("¡EL servidor nos ha aceptado!(" + respuesta + ")");
                System.out.println("Esperando tarea de minería...");
                //AQUI ABRIMOS UN BLUCLE PARA QUE SE QUEDE ESCUCHANDO INFINITAMENTE
                String msg;
                while ((msg = in.readLine()) != null) {
                    //ESPERAMOS MENSAJES
                    if (msg != null && msg.startsWith("new_request")) {
                        String bloque = msg.replaceFirst("new_request", "");

                        LogUtil.logClients("¡Tarea recibida!: " + bloque);

                        seguirMinando = true;
                        new Thread(() -> minar(bloque, out)).start();
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public void desconectarServidor() {
        try {
            seguirMinando = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        } catch (IOException e) {
            System.err.println("Error al cerrar: " + e.getMessage());
        }
    }


    public void minar(String bloque, PrintWriter out) {
        long nonce = 0;
        while (seguirMinando) {
            String hash = Hasher.calculateMD5(bloque + nonce);

            if (hash.startsWith("000000")) {
                LogUtil.logClients("¡HASH ENCONTRADA! NONCE: " + nonce);
                out.println("sol;" + nonce + ";" + hash);
                break;
            }
            nonce++;

            LogUtil.mostrarPrompt = false;
            if (!seguirMinando) {
                LogUtil.logClients("Hilo de minería detenido.");
                System.out.println("Conexión cerrada con el servidor.");
            }
        }
    }
}
