package client;

import common.Hasher;
import util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiningClient {

    private Socket socket;
    //CREAMOS POOL DE THREADS PARA PONER UN TOPE DE HILOS FUNCIONANDO
    private ExecutorService ejecutor = Executors.newFixedThreadPool(4);
    //CREAMOS VARIABLE ATÓMICA PARA DETENER HILOS A MODO DE MUTEX (INMEDIATO)
    private final AtomicBoolean seguirMinando = new AtomicBoolean(false);

    public static void main(String[] args) {
        String host = "localhost";
        int port = 6666;
        MiningClient miningClient = new MiningClient();

        Scanner sc = new Scanner(System.in);
        System.out.println("Cliente inciado");
        System.out.println("¿Desea conectarse al servidor?(s/n)");
        if (sc.nextLine().equalsIgnoreCase("s")) {
            //CREAMOS HILO A PARTE PARA NO PERDER EL MAIN Y PODER SALIR
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

                        LogUtil.logC("¡Tarea recibida!: " + bloque);

                        int numHilos = 4;
                        seguirMinando.set(true);

                        for (int i = 0; i < numHilos; i++) {
                            long nonceInicial = i * 1000000000L;
                            //CON EJECUTOR PONEMOS EN MARCHA CADA HILO
                            ejecutor.execute(() -> minar(bloque, out, nonceInicial));
                            System.out.println("Hilo " + i + " iniciado desde el nonce: " + nonceInicial);
                        }
                        LogUtil.setPrompt();
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public void desconectarServidor() {
        try {
            seguirMinando.set(false);
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        } catch (IOException e) {
            System.err.println("Error al cerrar: " + e.getMessage());
        }
    }


    public void minar(String bloque, PrintWriter out, Long nonce) {
        while (seguirMinando.get()) {
            String hash = Hasher.calculateSHA(bloque + nonce);
            if (hash.startsWith("000000")) {

                //COMPRUEBA QUE EL HILO ESTE EN TRUE PARA VERIFICAR QUE ALGUIEN NO LO HA SACADO ANTES Y SI NO LO PONDRA FALSE 2 EN UNO
                if (seguirMinando.compareAndSet(true, false)) {
                    LogUtil.logG("¡HASH ENCONTRADA! NONCE: " + nonce);
                    synchronized (out) {
                        out.println("sol;" + nonce + ";" + hash);
                    }
                }
                break;
            }
            nonce++;
        }
        System.out.println("Hilo de minería detenido.");
    }
}
