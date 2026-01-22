package server;

import common.Hasher;
import util.LogUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class MiningServer {

    //LISTA PARA SABER LOS CLIENTES CONECTADOS
    private final List<ClienteHandler> clientes = new CopyOnWriteArrayList<>();
    private String bloque;

    public MiningServer(String bloqueAleatorio) {
        this.bloque = bloqueAleatorio;
    }

    public static String generarPaqueteAleatorio(int cantidadTransaccines) {
        String[] usuarios = {"Pedro", "Juan", "Felipe", "Ana", "Maria"};
        Random aleatorio = new Random();
        StringBuilder paquete = new StringBuilder();

        for(int i = 0; i < cantidadTransaccines; i++) {
            String origen = usuarios[aleatorio.nextInt(usuarios.length)];
            String destino = usuarios[aleatorio.nextInt(usuarios.length)];

            while (origen.equals(destino)) {
                destino = usuarios[aleatorio.nextInt(usuarios.length)];
            }

            double cantidad = 1 + (500 * aleatorio.nextInt(500));

            paquete.append(origen)
                    .append("->")
                    .append(destino)
                    .append(":")
                    .append(String.format("%.2f", cantidad))
                    .append("|");

        }
        return paquete.toString();
    }


    public synchronized void procesarRespuestas(String nonce, String hash) {
        String clienHash = verificarHash(bloque + nonce);
        System.out.println("Comprobando hash...");
        if (clienHash.startsWith("000000")) {
            LogUtil.logG("¡HASH VALIDADO! ");
            System.out.println("Deteniendo clientes...");
            for (ClienteHandler cliente : clientes) {
                cliente.enviarMensaje("stop");
            }
            System.out.println();
            System.out.println("Clientes detenidos.");
            System.out.println();

            //ACTUALIZAMOS PAQUETE PARA QUE EL ANTERIOR YA NO VALGA
            this.bloque = generarPaqueteAleatorio(3);
            System.out.println("Nuevo reto generado: ");
            LogUtil.logC(bloque);

            for (ClienteHandler cliente : clientes) {
                cliente.enviarMensaje("new_request" + this.bloque);
            }
            System.out.println("Esperando Nonce...");
        }
    }

    public String verificarHash(String bloqueWithNonce){
        String hash = Hasher.calculateSHA(bloqueWithNonce);
        return hash;
    }

    public void anadirALista(ClienteHandler h) {
        LogUtil.logY("Cliente añadido a la lista");
        clientes.add(h);
    }

    public void eliminarDeLista(ClienteHandler h) {
        clientes.remove(h);
        System.out.println("Un cliente se ha ido :(");
    }


    public void enviarTrabajoACliente(ClienteHandler cliente) {
        System.out.println("Trabajo enviado a clientes.");
        System.out.println("Esperando Nonce...");
        cliente.enviarMensaje("new_request" + bloque);
    }


    public static void main(String[] args) {
        System.out.println("Servidor de minería iniciado...");
        String bloque = generarPaqueteAleatorio(3);

        MiningServer server = new MiningServer(bloque);

        int port = 6666;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor escuchando en el puerto: " + port);
            System.out.println("Nuevo reto generado: ");
            LogUtil.logC(bloque);
            System.out.println("Esperando cliente...");

            while (true) { //BUCLE INFINITO
                Socket socketCliente = serverSocket.accept();
                ClienteHandler handler = new ClienteHandler(socketCliente, server);

                //COMO ESTE OBJETO ES RUNNAMBLE PODEMOS LANZARLO CON START (NUEVO HILO)
                new Thread(handler).start();
            }

        }  catch (IOException e) {
            e.printStackTrace();
        }
    }
}