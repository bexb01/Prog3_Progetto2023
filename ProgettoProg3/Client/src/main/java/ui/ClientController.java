package ui;

import model.ClientCommunication;
import javafx.scene.control.*;
import model.InboxHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import model.Email;
import java.util.ArrayList;
import java.util.Date;
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

    private InboxHandler inbxHandler;
    private ClientCommunication ClientComm;
    private Email selectedEmail;
    private Email emptyEmail;
    private String username;

    public ClientController(String username, ClientCommunication clientComm, InboxHandler inbx) {
        this.username = username;
        this.ClientComm = clientComm;
        this.inbxHandler = inbx;
    }

    @FXML
    public void initialize(){
        //model.generateRandomEmails(10);
        selectedEmail = null;

        //binding tra lstEmails e inboxProperty
        lstEmails.itemsProperty().bind(inbxHandler.inboxProperty());
        lstEmails.setOnMouseClicked(this::showSelectedEmail);
        lblUsername.textProperty().bind(inbxHandler.emailAddressProperty());

        txtNewEmailReceivers.setVisible(false);
        txtNewEmailReceivers.setDisable(true);
        txtNewEmailSubject.setVisible(false);
        txtNewEmailSubject.setDisable(true);
        txtDateSent.setEditable(false);
        txtDateSent.setVisible(false);
        btnDiscard.setVisible(false);
        btnDelete.setVisible(false);

        /*emptyEmail = new Email();*/
        emptyEmail = null;

        updateDetailView(emptyEmail);
    }

    @FXML
    protected void onCreateNewEmailButton(){
        //lstEmails.setDisable(true); //funziona
        lstEmails.setMouseTransparent(true); //funziona anche meglio
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
        btnNewEmail.setDisable(true);
        txtDateSent.setVisible(false);
        //lstEmails.;
    }

    @FXML
    protected void onSendNewEmail(){
        // manca controllo sul testo della mail (?)
        // controllare se la mail esiste (?)
        String ragexEmail="^([\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,})(;\\s?[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,})*(;\\s*|$)"; //"; " finale non va ";" separatore ";" finale "; " separatore ";" finale "; " separatore "; " finale non va sempre per il finale ";" separatore "; " finale non va sempre per il finale
        if(txtNewEmailReceivers.getText() != "" && Pattern.matches(ragexEmail, txtNewEmailReceivers.getText())){ //controllo sintattico dell'indirizzo email del receiver (non controlla se esiste)
            if(txtNewEmailSubject.getText() != ""){
                double idd=0;
                idd=(Math.random()*10000);
                int id=(int)idd;            //creo il paramentro id dove vengono creati anche gli altri paramentri
                inbxHandler.addNewEmail(id, lblFrom.getText(), txtNewEmailReceivers.getText(), null, txtNewEmailSubject.getText(), txtEmailContent.getText(), new Date());
                System.out.println("Nuova email aggiunta alla inbox!");
                //Inviare email a server
                ClientComm.sendEmailToServer(id, lblFrom.getText(), txtNewEmailReceivers.getText(), null, txtNewEmailSubject.getText(), txtEmailContent.getText(), new Date(), "send");
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
                btnNewEmail.setDisable(false);
                lstEmails.setMouseTransparent(false);
                lstEmails.getSelectionModel().select(-1);
            }else System.out.println("La mail deve avere un oggetto!"); //implementare un pannello di errore?s
        }else {
            JOptionPane.showMessageDialog(null, "indirizzo email del receiver sintatticamente scorretto o inesistente",
                    "Errore", JOptionPane.ERROR_MESSAGE);
            System.out.println("indirizzo email del receiver sintatticamente scorretto o inesistente");
        } //implementare un pannello di errore?
    }

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
        btnNewEmail.setDisable(false);
        btnSendEmail.setVisible(false);
        lstEmails.setMouseTransparent(false);
        lstEmails.getSelectionModel().select(-1);
    }

    @FXML
    protected void onDeleteButtonClick() {
        inbxHandler.deleteEmail(selectedEmail);
        updateDetailView(emptyEmail);
        txtDateSent.setVisible(false);
        btnDelete.setVisible(false);
        ClientComm.sendEmailToServer(selectedEmail.getId(), selectedEmail.getSender(), selectedEmail.getReceivers().toString(), null, selectedEmail.getSubject(), selectedEmail.getText(), selectedEmail.getDate(), "delete");
    }

     /**
     * Mostra la mail selezionata nella vista
     */
    protected void showSelectedEmail(MouseEvent mouseEvent) {
        btnDelete.setVisible(true);
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
                        lblTo.setText(lblTo.getText() + list.get(i) + ", ");
                    else lblTo.setText(lblTo.getText() + "e " + list.get(i));
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
