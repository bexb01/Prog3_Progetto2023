package controller;

import model.ClientCommunication;
import javafx.scene.control.*;
import model.InboxHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import model.Email;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 * Classe Controller 
 */

public class ClientController {
    @FXML
    private Label lblFrom;

    @FXML
    private Label lblTo;

    @FXML
    private Label lblSubject;

    @FXML
    private Label lblUsername;

    @FXML
    private TextArea txtEmailContent;

    @FXML
    private TextArea txtDateSent;

    @FXML
    private ListView<Email> lstEmails;

    @FXML
    private TextField txtNewEmailReceivers;

    @FXML
    private TextField txtNewEmailSubject;

    @FXML
    private Button btnSendEmail;

    @FXML
    private Button btnDiscard;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNewEmail;
    @FXML
    private Button btnReply;
    @FXML
    private Button btnReplyAll;
    @FXML
    private Button btnForward;

    private ScheduledExecutorService RefreshClient;

    private InboxHandler inbxHandler;
    private ClientCommunication ClientComm;
    private Email selectedEmail;
    private Email emptyEmail;
    private String username;

    public ClientController(String username, InboxHandler inbx) {
        this.username = username;
        this.inbxHandler = inbx;
        this.ClientComm = new ClientCommunication(username, inbx);
    }

    /**
     * Inizializza la vista all'avvio
     */
    @FXML
    public void initialize(){
        selectedEmail = null;

        //binding tra lstEmails e inboxProperty
        lstEmails.itemsProperty().bind(inbxHandler.inboxProperty());
        lstEmails.setOnMouseClicked(this::showSelectedEmail);
        lblUsername.textProperty().bind(inbxHandler.emailAddressProperty());
        emptyEmail = null;
        updateDetailView(emptyEmail);

        //REFRESH
        RefreshClient = Executors.newSingleThreadScheduledExecutor();
        RefreshClient.scheduleAtFixedRate(new ClientCommunication(username, inbxHandler), 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Predispone la vista per la creazione di una nuova mail
     */
    @FXML
    protected void onCreateNewEmailButton(){
        lstEmails.setMouseTransparent(true);
        lblFrom.setText(username);
        txtNewEmailReceivers.setVisible(true);
        txtNewEmailReceivers.setDisable(false);
        txtNewEmailSubject.setVisible(true);
        txtNewEmailSubject.setDisable(false);
        txtEmailContent.setEditable(true);
        txtEmailContent.setText("");
        btnSendEmail.setVisible(true);
        btnDiscard.setVisible(true);
        btnDelete.setVisible(false);
        btnReply.setVisible(false);
        btnReplyAll.setVisible(false);
        btnForward.setVisible(false);
        btnNewEmail.setDisable(true);
        txtDateSent.setVisible(false);
        //lstEmails.;
    }

    /**
     * Invia la mail scritta nella vista
     */
    @FXML
    protected void onSendNewEmail(){
        String ragexEmail="^([\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,})(;\\s?[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,})*(;\s?|$)";
        if(!Objects.equals(txtNewEmailReceivers.getText(), "")){ //controllo indirizzo email non vuoto
            if(Pattern.matches(ragexEmail, txtNewEmailReceivers.getText())){ //controllo sintattico dell'indirizzo email del receiver (non controlla se esiste)
                if(!Objects.equals(txtNewEmailSubject.getText(), "")){
                    double idd=0;
                    //idd=(Math.random()*10000);
                    int id=(int)idd;            //creo il paramentro id dove vengono creati anche gli altri paramentri
                    inbxHandler.addNewEmail(id, lblFrom.getText(), txtNewEmailReceivers.getText(), txtNewEmailSubject.getText(), txtEmailContent.getText(), new Date());    //PROBLEMA LO FA PIù VOLTE
                    System.out.println("Nuova email aggiunta alla inbox!");
                    //Inviare email a server
                    boolean flag = ClientComm.sendEmailToServer(id, lblFrom.getText(), txtNewEmailReceivers.getText(), txtNewEmailSubject.getText(), txtEmailContent.getText(), new Date());
                    if(!flag){
                        JOptionPane.showMessageDialog(null, "Il client non è connesso al server",
                                "Attenzione!", JOptionPane.WARNING_MESSAGE);
                        System.out.println("Il client non è connesso al server");
                        return;
                    }
                    //cosi qui^ non devo usare getId()
                    txtNewEmailReceivers.setVisible(false);
                    txtNewEmailReceivers.setDisable(true);
                    txtNewEmailReceivers.setText("");
                    txtNewEmailSubject.setVisible(false);
                    txtNewEmailSubject.setDisable(true);
                    txtNewEmailSubject.setText("");
                    txtEmailContent.setText("");
                    txtEmailContent.setEditable(false);
                    lblTo.setText("");
                    lblSubject.setText("");
                    lblFrom.setText("");

                    btnSendEmail.setVisible(false);
                    btnDiscard.setVisible(false);
                    btnDelete.setVisible(false);
                    btnReply.setVisible(false);
                    btnReplyAll.setVisible(false);
                    btnForward.setVisible(false);
                    btnNewEmail.setDisable(false);
                    lstEmails.setMouseTransparent(false);
                    lstEmails.getSelectionModel().select(-1);
                }else {
                    JOptionPane.showMessageDialog(null, "L'oggetto della mail non deve essere vuoto" ,
                            "Attenzione!", JOptionPane.INFORMATION_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(null, "Controllare che l'indirizzo mail del receiver sia scritto correttamente e che rispetti i seguenti passi:\n" +
                                "- non deve terminare con spazi vuoti.\n" +
                                "- In caso di indirizzi multipli, devono essere divisi da '';'' o da ''; '' e non terminare con spazi vuoti.",
                        "Attenzione!", JOptionPane.INFORMATION_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(null, "indirizzo email del receiver inesistente",
                    "Attenzione!", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * ?
     */
    @FXML
    protected void onDiscardMouseClick(){
        txtNewEmailReceivers.setVisible(false);
        txtNewEmailReceivers.setDisable(true);
        txtNewEmailReceivers.setText("");
        txtNewEmailSubject.setVisible(false);
        txtNewEmailSubject.setDisable(true);
        txtNewEmailSubject.setText("");
        txtEmailContent.setText("");
        txtEmailContent.setEditable(false);
        lblTo.setText("");
        lblSubject.setText("");
        lblFrom.setText("");

        btnDiscard.setVisible(false);
        btnDelete.setVisible(false);
        btnReply.setVisible(false);
        btnReplyAll.setVisible(false);
        btnForward.setVisible(false);
        btnNewEmail.setDisable(false);
        btnSendEmail.setVisible(false);
        lstEmails.setMouseTransparent(false);
        lstEmails.getSelectionModel().select(-1);
    }

    /**
     * Elimina la mail selezionata nella vista
     */
    @FXML
    protected void onDeleteButtonClick() {
        inbxHandler.deleteEmail(selectedEmail);
        updateDetailView(emptyEmail);
        txtDateSent.setVisible(false);
        btnDelete.setVisible(false);
        btnReply.setVisible(false);
        btnReplyAll.setVisible(false);
        btnForward.setVisible(false);
        boolean flag = ClientComm.deleteEmail(selectedEmail.getId(), selectedEmail.getSender(), selectedEmail.getReceivers().toString(), selectedEmail.getSubject(), selectedEmail.getText(), selectedEmail.getDate());
        if(!flag){
            JOptionPane.showMessageDialog(null, "Il client non è connesso al server",
                    "Attenzione!", JOptionPane.WARNING_MESSAGE);
            System.out.println("Il client non è connesso al server");
            return;
        }
    }

    /**
     * Rispondi
     */
    @FXML
    protected void onReplyButtonClick(){
        lstEmails.setMouseTransparent(true);
        txtNewEmailReceivers.setVisible(true);
        txtNewEmailReceivers.setDisable(false);
        txtEmailContent.setEditable(true);
        //replaceAll(\n,\t\n); CAMBIARE COSI
        txtEmailContent.setText("Re:" + lblFrom.getText() + "\n\t" + txtEmailContent.getText()+ "\n\t" + txtDateSent.getText() + "\n");   //sistema visualizzazione testo
        txtNewEmailReceivers.setText(lblFrom.getText());    //CONTROLLARE
        lblFrom.setText(username);
        txtNewEmailSubject.setVisible(true);
        txtNewEmailSubject.setDisable(false);
        txtNewEmailSubject.setText("Re: " + lblSubject.getText());
        btnSendEmail.setVisible(true);
        btnDiscard.setVisible(true);
        btnDelete.setVisible(false);
        btnReply.setVisible(false);
        btnReplyAll.setVisible(false);
        btnForward.setVisible(false);
        btnNewEmail.setDisable(true);
        txtDateSent.setVisible(false);
    }

    /**
     * Rispondi a tutti
     */
    @FXML
    protected void onReplyAllButtonClick(){
        lstEmails.setMouseTransparent(true);
        txtNewEmailReceivers.setVisible(true);
        txtNewEmailReceivers.setDisable(false);
        String receivers = lblTo.getText();
        if(!username.equals(lblFrom.getText())) {
            System.out.println(username);
            System.out.println(lblFrom.getText());
            int index = receivers.indexOf(username);
            if(index>=0){
                int nextIndex = receivers.indexOf(";", index);
                if(nextIndex>=0){
                    String before = receivers.substring(0, index);
                    String after = receivers.substring(nextIndex + 1);
                    receivers = before + after;
                }
            }  else{
                int nextIndex = receivers.indexOf(";", index);
                String after = receivers.substring(nextIndex + 1);
                receivers = after;
            }
            txtNewEmailReceivers.setText(lblFrom.getText() + ";" + receivers + ";"); //DEVI PRENDERE TUTTI I receivers escluso te stesso
        } else
            txtNewEmailReceivers.setText(lblTo.getText());
        lblFrom.setText(username);
        txtNewEmailSubject.setVisible(true);
        txtNewEmailSubject.setDisable(false);
        txtNewEmailSubject.setText("Re: " + lblSubject.getText());
        txtEmailContent.setEditable(true);
        txtEmailContent.setText(txtEmailContent.getText() + "\n" + txtDateSent.getText() + "\n");   //sistema visualizzazione testo
        btnSendEmail.setVisible(true);
        btnDiscard.setVisible(true);
        btnDelete.setVisible(false);
        btnReply.setVisible(false);
        btnReplyAll.setVisible(false);
        btnForward.setVisible(false);
        btnNewEmail.setDisable(true);
        txtDateSent.setVisible(false);

    }

    /**
     * Inoltra
     */
    @FXML
    protected void onForwardButtonClick(){
        lblFrom.setText(username);
        txtNewEmailReceivers.setVisible(true);
        txtNewEmailReceivers.setDisable(false);
        txtNewEmailSubject.setVisible(true);
        txtNewEmailSubject.setDisable(false);
        txtNewEmailSubject.setText(lblSubject.getText());
        txtEmailContent.setEditable(true);
        txtEmailContent.setText(txtEmailContent.getText()+ "\n" + txtDateSent.getText() + "\n");
        btnSendEmail.setVisible(true);
        btnDiscard.setVisible(true);
        btnDelete.setVisible(false);
        btnReply.setVisible(false);
        btnReplyAll.setVisible(false);
        btnForward.setVisible(false);
        btnNewEmail.setDisable(true);
        txtDateSent.setVisible(false);
    }

     /**
     * Mostra la mail selezionata nella vista
     */
    protected void showSelectedEmail(MouseEvent mouseEvent) {
        btnDelete.setVisible(true);
        btnReply.setVisible(true);
        btnReplyAll.setVisible(true);
        btnForward.setVisible(true);
        Email email = lstEmails.getSelectionModel().getSelectedItem();
        selectedEmail = email;
        updateDetailView(email);
    }

     /**
     * Aggiorna la vista con la mail selezionata
     */
    protected void updateDetailView(Email email) {
        if(email != null) {
            lblFrom.setText(email.getSender());
            //lblTo.setText(String.join(", ", email.getReceivers()));
            ArrayList<String> list;
            list = email.getReceivers();
            lblTo.setText("");
            for(int i = 0; i < list.size(); i++){
                if(list.size() > 1) {
                    if (i != list.size() - 1)
                        lblTo.setText(lblTo.getText() + list.get(i) + ";");
                    else lblTo.setText(lblTo.getText() + " " + list.get(i));
                }else lblTo.setText(list.get(i));
            }
            lblSubject.setText(email.getSubject());
            txtEmailContent.setText(email.getText());
            txtDateSent.setVisible(true);
            txtDateSent.setText("Email inviata in data: " + email.getDate().toString());
        }else{
            lblFrom.setText("");
            lblTo.setText("");
            lblSubject.setText("");
            txtEmailContent.setText("");

        }
    }
}
