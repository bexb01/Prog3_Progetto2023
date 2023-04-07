package com.example.server;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import model.Email;
import model.Model;

public class ClientHandler implements Runnable {

    //ClientHandler: thread per ogni client che riceve le richieste dai client
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    //private PrintWriter out;
    private AtomicBoolean running = new AtomicBoolean(true);
    //private boolean running;
    private Model model;
    private String usernameClient;
    private String clientInboxFile;    //client inbox's path

    public ClientHandler(Socket clientSocket, Model model) {
        this.clientSocket = clientSocket;
        this.model = model;
        try {
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error while creating input/output streams for client: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // listening for only one request
            try {
                Object clientObject;
                if((clientObject = in.readObject()) != null) {
                    if(clientObject instanceof Integer) {
                        int mex = (Integer) clientObject;
                        switch (mex) {
                            case 1: //send
                                Object addEmail;
                                if((addEmail = in.readObject()) != null) {
                                    if(addEmail instanceof Email) {
                                        Email email = (Email) clientObject;
                                        String fileSander = "files/" + email.getSender() + "/inbox.txt";
                                        for (String s : email.getReceivers()) {
                                            Platform.runLater(() -> {
                                                this.model.printLog("Inviata email da " + email.getSender() + " a " + s);
                                            });
                                            //send email to receivers (scrive email sul file del receiver)
                                            System.out.println(s);//da togliere
                                            String FileReceiver = "files/" + s + "/inbox.txt";
                                            try (FileWriter writer = new FileWriter(FileReceiver, true)) {
                                                writer.write(email.toJson()+"\n");
                                                writer.close();//rilascio la risorsa utilizzata dal writer, inoltre notifica al so che il  file non e' piu in uso
                                                System.out.println("email scritta su file del receiver: " + FileReceiver);
                                            } catch (IOException e) {
                                                System.out.println("An error occurred while writing to the file.");
                                                e.printStackTrace();//manipolare l'errore nell'invio di una mail al sander che dice l'accaduto cosi ch potra reinviare la mai con indirizzo email corretto
                                            }
                                        }

                                        try (FileWriter writer = new FileWriter(fileSander, true)) {
                                            writer.write(email.toJson()+"\n");
                                            writer.close();//rilascio la risorsa utilizzata dal writer, inoltre notifica al so che il  file non e' piu in uso
                                            System.out.println("email scritta su file del sander: " + fileSander);
                                        } catch (FileNotFoundException e) {
                                            System.out.println("An error occurred while writing to the file.");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                closeConnection();
                                break;

                            /*case 2: //receive
                                //IDK
                                        for (String s : e.getReceivers()) {
                                            Platform.runLater(() -> {
                                            this.model.printLog(s + " ha ricevuto una email da " + e.getSender());
                                            });
                                        }
                                break;
                             */
                            case 3: //delete
                                Object deleteEmail;
                                if((deleteEmail = in.readObject()) != null) {
                                    if(deleteEmail instanceof Email) {
                                        Email email = (Email) clientObject;
                                        //implement function to delete email
                                        Platform.runLater(() -> {
                                            this.model.printLog("Elimino email " + email.getId() + " di " + email.getSender());
                                        });
                                    }
                                }
                                //elimina email del sender
                                closeConnection();
                                break;
                        }
                    }else if(clientObject instanceof String){
                        String mex = (String)clientObject;
                        if(!mex.equals("get")) {
                            this.usernameClient = mex;
                            Platform.runLater(() -> {
                                this.model.printLog(mex + " si è connesso.");
                            });
                            clientInboxFile = createFileInbox(mex);   //per creare il cazzo di file dell'inbox
                        }else{
                            Platform.runLater(() -> {
                                this.model.printLog( usernameClient + " ha richiesto tutte le sue mail");
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
                Platform.runLater(() -> {this.model.printLog(e.getMessage());});
                running.set(false);
            } catch (ClassNotFoundException e) {
                Platform.runLater(() -> {this.model.printLog("Error while reading object from client: " + e.getMessage());});
            }

        // Close the client socket and streams
    }

    private void closeConnection(){

        try {
            // Log the closure of connection
            Platform.runLater(() -> {this.model.printLog("Client disconnected");});
            // Close connection with client
            clientSocket.close();

            //close input-output streams:
            in.close();
            out.close();
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
            ArrayList<Email> listaEmail = new ArrayList<>();
            while (reader.hasNextLine()) {
                data = reader.nextLine();
                if(data!=null) {
                    Gson gson = new Gson();
                    String jsStr = data;
                    Type fooType = new TypeToken<Email>() {
                    }.getType();
                    Email e = gson.fromJson(data, fooType);
                    listaEmail.add(e);
                    System.out.println(e.getSender() + " " + e.getReceivers() + " " + e.getSubject() + " " + e.getText() + " " + e.getDate());
                }
            }
            reader.close();
            fr.close();

            try {
                out.writeObject(listaEmail);
                Platform.runLater(() -> {
                    this.model.printLog("Ho inviato tutte le mail a " + usernameClient+ ".");
                });
            }catch (Exception ex){
                System.err.println("Errore nella ricezione della lista email " + ex.getMessage());
            }
        }catch(IOException e){
            System.out.println("Errore nella chiusura del file-reader" + e.getMessage());
        }
    }
}



