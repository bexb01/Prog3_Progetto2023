package com.example.server;

import controller.ServerController;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server implements Runnable{

    //Server: gestisce le connessioni con i client
    private static int PORT;
    private ServerSocket serverSocket;
    private AtomicBoolean exit = new AtomicBoolean(true);    //togliere static
    ExecutorService executor = Executors.newFixedThreadPool(10);

    public ServerController controller;

    public Server(int port, ServerController controller){
        this.PORT = port;
        this.controller = controller;
    }

    public void run() {
        try {
            //set-up
            serverSocket = new ServerSocket(PORT);
            this.controller.stampaLog("Server in ascolto sulla porta " + PORT);
            //create thread
            while (exit.get()) { //cambia parte thread
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientH = new ClientHandler(clientSocket, this.controller);
                Thread t = new Thread(clientH);
                executor.execute(t);
                //Platform.runLater(() -> {this.controller.stampaLog("Nuovo client connesso");});

                //crea metodo shutdown server che parte con la exit on close (premi x) e cambia l'atomic boolean a false
            }
        } catch (Exception e) {
            System.err.println("Error while accepting client connection: " + e.getMessage());
        }finally {
            try {
                if (exit.get()){
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error while stopping server: " + e.getMessage());
            }
        }
        //AGGIUNGI EXIT ON CLOSE
    }

    private static void handleClient(Socket clientSocket) { //task
        try{
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println("TEST SERVER");
            // Read serialized object from the client
            Object clientObject = in.readObject();

            // Print object to the console
            System.out.println("Received object from client: " + clientObject.toString());

            // Send response to the client
            out.println("Ciao dal server! Bella email, grazie");
        } catch (IOException /*| ClassNotFoundException */ex) {
            throw new RuntimeException(ex);
        } catch (Exception e) {
            System.err.println("Error while handling client connection: " + e.getMessage());
        }
    }

    public void shutdownServer() throws IOException {    //roba da gestire dopo

        try {
            // Stop accepting new requests
            exit.set(false);
            // Wait termination of all tasks running
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
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