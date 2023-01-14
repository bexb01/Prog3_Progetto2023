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

    public void deleteEmail(Email email) {
        inboxContent.remove(email);
    }

    /*public void generateRandomEmails(int n) {
        String[] people = new String[] {"Paolo", "Alessandro", "Enrico", "Giulia", "Gaia", "Simone"};
        String[] subjects = new String[] {
                "Importante", "A proposito della nostra ultima conversazione", "Tanto va la gatta al lardo",
                "Non dimenticare...", "Domani scuola" };
        String[] texts = new String[] {
                "È necessario che ci parliamo di persona, per mail rischiamo sempre fraintendimenti",
                "Ricordati di comprare il latte tornando a casa",
                "L'appuntamento è per domani alle 9, ci vediamo al solito posto",
                "Ho sempre pensato valesse 42, tu sai di cosa parlo"
        };
        Random r = new Random();

        for (int i=0; i<n; i++) {
            ArrayList<String> list = new ArrayList<>();
            list.add(people[r.nextInt(0, 5)]);
            list.add(people[r.nextInt(0, 5)]);
            list.add(people[r.nextInt(0, 5)]);
            Email email = new Email(0, people[r.nextInt(people.length)],
                    list, null,
                    subjects[r.nextInt(subjects.length)],
                    texts[r.nextInt(texts.length)], new Date(), "");
            inboxContent.add(email);
        }
    }*/

    public void addNewEmail(int id,String sender, String receivers, Email e, String subject, String text, Date d){
        ArrayList<String> list = new ArrayList<>();  //gestire ciclo per aggiungere molteplici receivers
        list.add(receivers);
        //mettere controllo se esiste email forwarded (?)
        //gestire id per renderlo diverso per ogni email

        Email newEmail = new Email(id, sender, list, null, subject, text, d, "");
        inboxContent.add(newEmail);
    }

    public void addEmailToInbox(Email e){     //TEMPORANEO!!!!!
        inboxContent.add(e);
    }
}

