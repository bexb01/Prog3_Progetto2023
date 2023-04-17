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

    public ClientCommunication() {
    }

    @Override
    public void run() {
        //sends their username to server
        requestEmail(); //asks all emails to server
    }

    /**
     * Apre la connessione con il server
     */
    private boolean openConnection(){
        try {
            this.socket = new Socket("127.0.0.1", 8189);    //gestire connessione in assenza del server
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            return true;
        }catch (SocketException e) {
            System.out.println("return false");
            System.err.println("Connection closed by server: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("return false");
            System.err.println("Error while opening clientcommunication connection: " + e.getMessage());
            return false;
        }
    }

    /**
     * Chiude la connessione con il server
     */
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

    /**
     * Manda al server la richiesta della inbox
     */
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
                    int oldNumEmail=numberEmail;
                    ArrayList<Email> list = (ArrayList<Email>)serverObject;
                    for(int i = 0; i < list.size(); i++){
                        System.out.println(numberEmail+ " prima request");
                        numberEmail++;
                        System.out.println(numberEmail+ " dopo request");
                        Email e = list.get(i);
                        Platform.runLater(() -> {inboxMail.addEmailToInbox(e);});

                    }
                        if(oldNumEmail!=0 && list.size()==1){ //se nella inbox c'erano piu di 0 email e ne ggiungiamo una
                            Email es = list.get(0);
                            if(!(es.getSender().equals(username))) {//con sander diverso dal quello del client allora nuova email
                                Platform.runLater(() -> {
                                    JOptionPane.showMessageDialog(null, "Hai ricevuto una nuova Email da:\n" + es.getSender(),
                                            username, JOptionPane.INFORMATION_MESSAGE);
                                });
                            }
                        }else if(oldNumEmail==0 && list.size()==1){//se nella inbox c'erano 0 email e ne riceviamo 1
                            Email es = list.get(0);
                            if(!(es.getSender().equals(username))) {//con sander diverso dal quello del client allora nuova email
                                Platform.runLater(() -> {
                                    JOptionPane.showMessageDialog(null, "Hai ricevuto una nuova Email da:\n" + es.getSender(),
                                            username, JOptionPane.INFORMATION_MESSAGE);
                                });
                            }

                        }else if(oldNumEmail==0 ){//se avevi 0 email e ne ricevi piu di una allora in teoria e' la prima volta che aggiorni la inbox?
                            Platform.runLater(() -> {JOptionPane.showMessageDialog(null, "tutte le email aggiunte alla posta",
                                    username, JOptionPane.INFORMATION_MESSAGE);});
                        }
                        //in ogni caso in cui il client ne invia una non fa uscire nessun messaggio
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

    /**
     * Crea un array contenente tutti i destinatari presenti nella stringa
     */
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

}
