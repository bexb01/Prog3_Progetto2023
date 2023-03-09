package com.example.server;

import model.Model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server implements Runnable{

    //Server: handles connection with clients
    private static int PORT;
    private ServerSocket serverSocket;
    private AtomicBoolean running = new AtomicBoolean(true);
    ExecutorService executor = Executors.newFixedThreadPool(10);
    private Model model;

    public Server(int port , Model model) {
        this.PORT = port;
        this.model = model;
    }

    public void run() {
        try {
            //set-up
            serverSocket = new ServerSocket(PORT);
            //SBAGLIATO AGGIORNARE LISTA MODEL COLLEGATA CON IL CONTROLLER PER POI STAMPARE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
            this.model.printLog("Server in ascolto sulla porta " + PORT);
            //create thread
            while (running.get()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientH = new ClientHandler(clientSocket, this.model);
                Thread t = new Thread(clientH);
                executor.execute(t);
                //crea metodo shutdown server che parte con la exit on close (premi x) e cambia l'atomic boolean a false
            }
        } catch (Exception e) {
            System.err.println("Error while accepting client connection: " + e.getMessage());
        }finally {
            try {
                if (running.get()){
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error while stopping server: " + e.getMessage());
            }
        }

    }

    public void shutdownServer() throws IOException {     //Add exitonclose operations and how to handle when the server is closed but the client stays open

        try {
            // Stop accepting new requests
            running.set(false);
            // Wait termination
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Error while closing server connection: ");
            e.printStackTrace();
        }finally{
            serverSocket.close();
        }
    }
}
//se uno sta scrivendo nel mailbox gli altri devono stare fermi
//gestire cartelle in mutua esclusione
//client parte e senza aprire ancora gui chiede nome e in base a quello connette account
//client che manda mail a email non esistente(deve mandarla) poi il server la riceve e manda in dietro al client una mail tra i ricevuti in cui dice che quell'email non esiste