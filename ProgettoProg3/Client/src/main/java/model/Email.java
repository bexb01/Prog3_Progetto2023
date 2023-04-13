package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Email implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private String sender;
    private ArrayList<String> receivers;
    private String subject;
    private String text;
    private Date date;

    public Email() {
    }

    public Email(int id, String sender, ArrayList<String> receivers, String subject, String text, Date date) {
        this.id = id;
        this.sender = sender;
        this.receivers = receivers;
        this.subject = subject;
        this.text = text;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public ArrayList<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(ArrayList<String> receivers) {
        this.receivers = receivers;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.join(" - ", List.of(this.sender,this.subject));
    }
}
