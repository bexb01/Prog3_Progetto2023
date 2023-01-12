package com.example.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

import controller.ServerController;
import javafx.application.Platform;
import model.Email;

public class ClientHandler implements Runnable {

    //ClientHandler: thread per ogni client che riceve le richieste dai client
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    //private PrintWriter out;
    private boolean running;
    private ServerController serverCont;
    private String usernameClient;
    private String clientInboxFile;    //path della inbox del client

    public ClientHandler(Socket clientSocket, ServerController controller) {
        this.clientSocket = clientSocket;
        this.serverCont = controller;
        try {
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error while creating input/output streams for client: " + e.getMessage());
        }
        this.running = true;
    }

    @Override
    public synchronized void run() {
        // Start listening for client messages

        while (running) {
            try {
                Object clientObject;
                if((clientObject = in.readObject()) != null) {
                    if(clientObject instanceof Email) {
                        Email e = (Email) clientObject;
                        switch (e.getOptions()) {
                            case "send":
                                Platform.runLater(() -> {
                                    this.serverCont.stampaLog("Inviata email da " + e.getSender() + " a " + e.getReceivers());  //gestore arraylist dei receivers
                                });
                                //invia email al/ai receiver/s
                                break;
                            case "delete":
                                Platform.runLater(() -> {
                                    this.serverCont.stampaLog("Elimino email di " + e.getSender());
                                });
                                //elimina email del sender
                                break;
                        }
                    }else if(clientObject instanceof String){
                        String mex = (String)clientObject;
                        if(!mex.equals("get")) {
                            this.usernameClient = mex;
                            Platform.runLater(() -> {
                                this.serverCont.stampaLog(mex + " si è connesso.");
                            });
                            clientInboxFile = createFileInbox(mex);   //per creare il cazzo di file dell'inbox
                        }else{
                            Platform.runLater(() -> {
                                this.serverCont.stampaLog("Quel coglione di " + usernameClient + " mi ha chiesto tutte le sue mail");
                            });
                            getAllEmails();
                        }
                    }
                }

                // Send response to the client
                //out.println("Hello, client!");  //per printwriter (messaggi di semplici stringhe al posto di oggetti)
            } catch (IOException e) {
                // If an IOException is thrown, it means that the connection with the client has been lost
                // Stop the client handler and log the event
                Platform.runLater(() -> {this.serverCont.stampaLog(e.getMessage());});
                running = false;
            } catch (ClassNotFoundException e) {
                System.out.println("Error while reading object from client: " + e.getMessage());
            }
        }

        // Close the client socket and streams
        try {
            clientSocket.close();
            in.close();
            out.close();
            Platform.runLater(() -> {this.serverCont.stampaLog("Client disconnected");});
        } catch (IOException e) {
            System.out.println("Error while closing client socket and streams: " + e.getMessage());
        }
    }

    public static String createFileInbox(String username)
            throws IllegalArgumentException, IOException {

        if (username == null)
            throw new IllegalArgumentException("[Illegal Argument]: username to be created must be not null");

        // Creating User dir
        Files.createDirectories(Paths.get("files/" + username));

        // Creating inbox file
        File inbox = new File("files/" + username + "/" + "inbox.txt");

        inbox.createNewFile();
        return "files/" + username + "/" + "inbox.txt";
    }

    private void getAllEmails() {
        try {
            FileReader fr = new FileReader(clientInboxFile);
            Scanner reader = new Scanner(fr);
            String data;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            ArrayList<String> list = new ArrayList<>();
            ArrayList<Email> listaEmail = new ArrayList<>();
            while (reader.hasNextLine()) {
                data = reader.nextLine();
                list.add(data.split(",")[2].replaceAll("[{-}]", ""));
                Email e = new Email(Integer.parseInt(data.split(",")[0].replaceAll("[{-}]", "")),
                        data.split(",")[1].replaceAll("[{-}]", ""),
                        list,
                        new Email(),    //???
                        data.split(",")[4].replaceAll("[{-}]", ""),
                        data.split(",")[5].replaceAll("[{-}]", ""),
                        formatter.parse(data.split(",")[6].replaceAll("[{-}]", "")),
                        data.split(",")[7].replaceAll("[{-}]", ""));
                listaEmail.add(e);
                list.clear();
                //System.out.println(e.getSender() + " " + e.getReceivers() + " " + e.getSubject() + " " + e.getText() + " " + e.getDate());
            }
            reader.close();
            fr.close();

            try {
                out.writeObject(listaEmail);
                Platform.runLater(() -> {
                    this.serverCont.stampaLog("Ho inviato tutte le mail a " + usernameClient+ ".");
                });
            }catch (Exception ex){
                System.err.println("3 - Error while communicating with the server: " + ex.getMessage());
            }
        }catch(IOException | ParseException e){
            System.out.println(e.getMessage());
        }
    }
}

