package model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class InboxHandler {
    private ListProperty<Email> inbox;
    private ObservableList<Email> inboxContent;
    private StringProperty emailAddress;

    public InboxHandler() {
    }

    public InboxHandler(String emailAddress) {
        this.inboxContent = FXCollections.observableList(new LinkedList<>());
        this.inbox = new SimpleListProperty<>();
        this.inbox.set(inboxContent);
        this.emailAddress = new SimpleStringProperty(emailAddress);
    }

    public ListProperty<Email> inboxProperty() {
        return inbox;
    }

    public StringProperty emailAddressProperty() {
        return emailAddress;
    }

    /**
     * Rimuove una Email dalla lista presente nella vista
     */
    public void deleteEmail(Email email) {
        inboxContent.remove(email);
    }

    /**
     * Aggiunge una email dalla lista di email
     */
    public void addNewEmail(int id,String sender, String receivers, String subject, String text, Date d){
        ArrayList<String> list = new ArrayList<>();
        list.add(receivers);
        Email newEmail = new Email(id, sender, list, subject, text, d);
    }

    /**
     * Aggiunge una email alla vista
     */
    public void addEmailToInbox(Email e){
        inboxContent.add(e);
    }
}

