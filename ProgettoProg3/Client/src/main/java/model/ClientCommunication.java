package model;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class ClientCommunication implements Runnable{

    private Socket socket;
    //private static BufferedReader in;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private boolean running;
    private String username;
    private InboxHandler inboxMail;

    //ClientCommunication: gestisce collegamento al server
    public ClientCommunication(String username, InboxHandler inbx) {
        this.username = username;
        this.inboxMail = inbx;
    }

    public ClientCommunication() {
    }

    @Override
    public void run() {
        running = true;
        try {
            this.socket = new Socket("127.0.0.1", 8189);    //gestire connessione in assenza del server
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            //inboxMail = new InboxHandler();
            inviaUsername();  //passa il suo username al server
            richiediEmail();  //richiede al server tutte le sue mail

            while(running) {
                try {
                    Object clientObject;
                    if ((clientObject = in.readObject()) != null) {
                        if (clientObject instanceof ArrayList<?>){
                            ArrayList<Email> list = (ArrayList<Email>)clientObject;
                            for(int i = 0; i < list.size(); i++){
                                inboxMail.aggiungiEmailInbox(list.get(i));
                            }
                        }
                    }
                }catch(IOException e) {
                    System.err.println("1 - Error while communicating with the server: " + e.getMessage());
                }

                // Read response from the server
                //String serverResponse = in.readLine();
                //System.out.println("Received response from server: " + serverResponse);
            }
        } catch (Exception e) {
            System.err.println("2 - Error while communicating with the server: " + e.getMessage());
        }

        /*try {      //close connection
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception ex) {
        }*/
    }

    private void richiediEmail() {
        try {
            Object obj = "get";
            out.writeObject(obj);
        }catch (Exception ex){
            System.err.println("3 - Error while communicating with the server: " + ex.getMessage());
        }
    }

    public void inviaEmailServer(String sender, String receivers, Email e, String subject, String text, Date d, String options){
        ArrayList<String> list = new ArrayList<>();  //gestire ciclo per aggiungere molteplici receivers
        list.add(receivers);
        //mettere controllo se esiste email forwarded (?)
        Email newEmail = new Email(0, sender, list, null, subject, text, d, options);
        try {
            out.writeObject(newEmail);
        }catch (Exception ex){
            System.err.println("4 - Error while communicating with the server: " + ex.getMessage());
        }
    }

    public void inviaUsername(){
        try {
            Object obj = username;
            out.writeObject(obj);
        }catch (Exception ex){
            System.err.println("5 - Error while communicating with the server: " + ex.getMessage());
        }
    }
}
