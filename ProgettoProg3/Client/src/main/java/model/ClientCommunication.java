package model;

import javafx.application.Platform;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ClientCommunication implements Runnable{

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private int numberEmail;
    private InboxHandler inboxMail;

    public ClientCommunication(String username, InboxHandler inbx) {
        this.username = username;
        this.inboxMail = inbx;
        this.numberEmail = 0;
    }

    public ClientCommunication() {
    }

    @Override
    public void run() {
        //sends their username to server
        requestEmail(); //asks all emails to server
    }

    private boolean openConnection(){
        try {
            this.socket = new Socket("127.0.0.1", 8189);    //gestire connessione in assenza del server
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("returno true");
            return true;
        }catch (SocketException e) {
            System.out.println("returno false");
            System.err.println("Connection closed by server: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("returno false");
            System.err.println("Error while opening clientcommunication connection: " + e.getMessage());
            return false;
        }
    }

    private void requestEmail() {
        if(!openConnection()){
            System.out.println("connessione fallita: retry");
            return;
        }
        try {
            out.writeObject("get");
            out.writeObject(username);
            out.writeObject(numberEmail);
        }catch (Exception ex){
            System.err.println("Error while requesting emails from the server: " + ex.getMessage());
        }
        try {
            Object serverObject;
            //IMPLEMENT server sends array of emails on opening
            if ((serverObject = in.readObject()) != null) {
                if (serverObject instanceof ArrayList<?>){
                    ArrayList<Email> list = (ArrayList<Email>)serverObject;
                    for(int i = 0; i < list.size(); i++){
                        numberEmail++;
                        Email e = list.get(i);
                        Platform.runLater(() -> {inboxMail.addEmailToInbox(e);});
                    }
                        Platform.runLater(() -> {JOptionPane.showMessageDialog(null, "Hai ricevuto nuove Email",
                                "Attenzione!", JOptionPane.INFORMATION_MESSAGE);});

                }
            }
        }catch(IOException e) {
            System.err.println("Error while loading email to inbox: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Error while reading object from server: " + e.getMessage());
            throw new RuntimeException(e);
        }
        closeConnection();
    }

    private void closeConnection() {
        try{
            this.socket.close();    //gestire connessione in assenza del server
            out.close();
            in.close();
            System.out.println("connessione chiusa T_T");

        } catch(IOException e){
            System.out.println("Error while closing client socket and streams: " + e.getMessage());
        }
    }

    public ArrayList<String> parseReceivers(String receivers){
        //qui mi arriva una string contenete tutti i receivers non distinti
        ArrayList<String> arrLReceivers = new ArrayList<>(); //creo arrayList
        if (receivers.endsWith(";")) {
            receivers = receivers.substring(0, receivers.length() - 1);  // rimuove l'ultimo carattere solo se è ";"
        }else if (receivers.endsWith("; ")) {
            receivers = receivers.substring(0, receivers.length() - 2);  // rimuove ultimi caratteri solo se è "; "
        }
        String[] arrReceivers = receivers.split("(; |;)"); //creo e popolo array di stringhe coin i receivers divisi con "; " o ";"
        arrLReceivers.addAll(Arrays.asList(arrReceivers));  //aggiuno all'arrL l'array come Lista
        return arrLReceivers;
    }
    public boolean sendEmailToServer(int id, String sender, String receivers, String subject, String text, Date d){

        if(!openConnection())
            return false;
        ArrayList<String> arrLReceivers = parseReceivers(receivers);
        Email newEmail = new Email(id, sender, arrLReceivers, subject, text, d);
        try {
            out.writeObject("send");
            out.writeObject(newEmail);
            numberEmail++;
            closeConnection();

        }catch (Exception ex){
            System.err.println("Error while sending email to the server: " + ex.getMessage());
            return false;
        }
        return true;

    }

    public boolean deleteEmail(int id, String sender, String receivers, String subject, String text, Date d){   //COMPLETA parte server
        if(!openConnection())
            return false;
        ArrayList<String> arrLReceivers = parseReceivers(receivers);
        System.out.println("clientCommunication deleteEmail email inviata al server per cancellare"); //da togliere
        Email newEmail = new Email(id, sender, arrLReceivers, subject, text, d);
        try {
            out.writeObject("delete");
            out.writeObject(username);
            out.writeObject(newEmail);
            numberEmail--;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        closeConnection();
        return true;
    }

}
