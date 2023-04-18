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
    private static int numberEmail;
    private InboxHandler inboxMail;

    public ClientCommunication(String username, InboxHandler inbx) {
        this.username = username;
        this.inboxMail = inbx;
        numberEmail = 0;
    }

    @Override
    public void run() {
        requestEmail();
    }

    /**
     * Apre la connessione con il server
     */
    private boolean openConnection(){
        try {
            this.socket = new Socket("127.0.0.1", 8189);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            return true;
        }catch (SocketException e) {
            System.out.println("Connection with server not found");
            return false;
        } catch (Exception e) {
            System.err.println("Error while opening Clientcommunication connection: " + e.getMessage());
            return false;
        }
    }

    /**
     * Chiude la connessione con il server
     */
    private void closeConnection() {
        try{
            this.socket.close();
            out.close();
            in.close();
            //System.out.println("Connessione chiusa");
        } catch(IOException e){
            System.err.println("Error while closing client socket and streams: " + e.getMessage());
        }
    }

    /**
     * Manda al server la richiesta della inbox
     */
    private void requestEmail() {
        if(!openConnection()){
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
            if ((serverObject = in.readObject()) != null) {
                if (serverObject instanceof ArrayList<?>){
                    ArrayList<Email> list = (ArrayList<Email>)serverObject;
                    for(int i = 0; i < list.size(); i++){
                        numberEmail++;
                        Email e = list.get(i);
                        Platform.runLater(() -> {inboxMail.addEmailToInbox(e);});
                    }
                    //se ricevi un email
                    if(list.size()==1){
                        Email es = list.get(0);
                        if(!(es.getSender().equals(username))) {
                            Platform.runLater(() -> {
                                JOptionPane.showMessageDialog(null, "Hai ricevuto una nuova Email da:\n" + es.getSender(),
                                        username, JOptionPane.INFORMATION_MESSAGE);
                            });
                        }
                    }else if(list.size()>1){//carica la inbox, hai ricevuto piu di una mail o hai inviato una mail con receiver sbagliato e il server ti ha risposto
                        Platform.runLater(() -> {JOptionPane.showMessageDialog(null, "tutte le email aggiunte alla posta",
                                username, JOptionPane.INFORMATION_MESSAGE);});
                    }
                    //il client manda un messaggio quindi non visualizzi niente
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

    /**
     * Invia al server l'email da spedire
     */
    public boolean sendEmailToServer(int id, String sender, String receivers, String subject, String text, Date d){
        if(!openConnection())
            return false;
        ArrayList<String> arrLReceivers = parseReceivers(receivers);
        Email newEmail = new Email(id, sender, arrLReceivers, subject, text, d);
        try {
            out.writeObject("send");
            out.writeObject(newEmail);
            closeConnection();
        }catch (Exception ex){
            System.err.println("Error while sending email to the server: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Invia al server la email da rimuovere dalla inbox
     */
    public boolean deleteEmail(int id, String sender, String receivers, String subject, String text, Date d){
        if(!openConnection())
            return false;
        ArrayList<String> arrLReceivers = parseReceivers(receivers);
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

    /**
     * Crea un array contenente tutti i destinatari presenti nella stringa
     */
    public ArrayList<String> parseReceivers(String receivers){
        ArrayList<String> arrLReceivers = new ArrayList<>();
        //rimuove gli ultimi caratteri
        if (receivers.endsWith(";")) {
            receivers = receivers.substring(0, receivers.length() - 1);
        }else if (receivers.endsWith("; ")) {
            receivers = receivers.substring(0, receivers.length() - 2);
        }
        //rimuove dalla stringa i divisori utilizzati nella vista e popola l'array
        String[] arrReceivers = receivers.split("(; |;)");
        arrLReceivers.addAll(Arrays.asList(arrReceivers));
        return arrLReceivers;
    }

}
