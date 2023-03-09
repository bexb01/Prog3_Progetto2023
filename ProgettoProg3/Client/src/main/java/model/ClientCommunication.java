package model;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientCommunication implements Runnable{

    private Socket socket;
    //private static BufferedReader in;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private String username;
    private InboxHandler inboxMail;

    //ClientCommunication: handles the connection with the server
    public ClientCommunication(String username, InboxHandler inbx) {
        this.username = username;
        this.inboxMail = inbx;
    }

    public ClientCommunication() {
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket("127.0.0.1", 8189);    //gestire connessione in assenza del server
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            //inboxMail = new InboxHandler();
            sendUsername();  //sends their username to server
            requestEmail();  //asks all emails to server

                try {
                    Object clientObject;
                    //IMPLEMENT server sends array of emails on opening
                    if ((clientObject = in.readObject()) != null) {
                        if (clientObject instanceof ArrayList<?>){
                            ArrayList<Email> list = (ArrayList<Email>)clientObject;
                            for(int i = 0; i < list.size(); i++){
                                inboxMail.addEmailToInbox(list.get(i));
                            }
                        /*} else if(clientObject instanceof Email){
                            Email e = (Email) clientObject;
                            switch (e.getOptions()) {
                                case 1:     //receive
                                    //deve ricevere e inserire nella inbox la mail
                                    for(String s: e.getReceivers())
                                        //MAYBE ERROR IS HERE
                                        //trova un modo per mandare un messaggio da un utente ad un altro utente
                                        if(s==username){
                                            inboxMail.addEmailToInbox(e);
                                            out.writeObject(s + " ha ricevuto un email da " + e.getSender());
                                        }

                            }*/
                        }
                        //task sen

                    }
                }catch(IOException e) {
                    System.err.println("Error while loading email to inbox: " + e.getMessage());
                }

                // Read response from the server
                //String serverResponse = in.readLine();
                //System.out.println("Received response from server: " + serverResponse);
        } catch (Exception e) { //FORSE NON SERVE
            System.err.println("2 - Error while communicating with the server: " + e.getMessage());
        }

        /*try {      //close connection DA RIMETTERE?
            out.close();
            in.close();
            socket.close();
        } catch (Exception ex) {
            System.err.println("Error while closing client socket: " + ex);
        }*/
    }

    private void requestEmail() {
        try {
            out.writeObject("get");
        }catch (Exception ex){
            System.err.println("Error while requesting emails from the server: " + ex.getMessage());
        }
    }

    public void sendEmailToServer(int id, String sender, String receivers, Email e, String subject, String text, Date d, String options){
        ArrayList<String> list = new ArrayList<>();  //gestire ciclo per aggiungere molteplici receivers
        list.add(receivers);
        //mettere controllo se esiste email forwarded (?)
        Email newEmail = new Email( id, sender, list, null, subject, text, d, options);
        try {
            out.writeObject(newEmail);
        }catch (Exception ex){
            System.err.println("Error while sending email to the server: " + ex.getMessage());
        }
    }

    public void sendUsername(){
        try {
            Object obj = username;
            out.writeObject(obj);
        }catch (Exception ex){
            System.err.println("Error while sending username to the server: " + ex.getMessage());
        }
    }

    private void loadEmailToInbox(){

    }
    /* needs something like this
    public void shutdown() throws IOException {     //Add exitonclose operations and how to handle when the server is closed but the client stays open
            // Stop accepting new requests
            running.set(false);
            //close client socket
            socket.close();
    }
    */
}
