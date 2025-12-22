package server;

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


    public void enviarTrabajoGlobal () {
        System.out.println("Repartiendo bloque: " + bloque);
        for (ClienteHandler cliente : clientes) {
            cliente.enviarMensaje("new_request" + bloque);
        }
    }

    public void añadirALista(ClienteHandler h) {
        clientes.add(h);
    }

    public void eliminarDeLista(ClienteHandler h) {
        clientes.remove(h);
    }


    public void enviarTrabajoACliente(ClienteHandler cliente) {
        System.out.println("Enviando bloque al nuevo cliente...");
        cliente.enviarMensaje("new_request" + bloque);
    }

    public static void main(String[] args) {
        System.out.println("Servidor de minería iniciado...");
        String bloque = generarPaqueteAleatorio(3);

        MiningServer server = new MiningServer(bloque);

        int port = 6666;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor escuchando en el puerto: " + port);

            while (true) { //BUCLE INFINITO

                Socket socketCliente = serverSocket.accept();
                System.out.println("Añadiendo cliente...");
                ClienteHandler handler = new ClienteHandler(socketCliente, server);

                //COMO ESTE OBJETO ES RUNNAMBLE PODEMOS LANZARLO CON START (NUEVO HILO)
                new Thread(handler).start();
                System.out.println("Cliente añadido");
                server.enviarTrabajoACliente(handler);
            }

        }  catch (IOException e) {
            e.printStackTrace();
        }
    }
}