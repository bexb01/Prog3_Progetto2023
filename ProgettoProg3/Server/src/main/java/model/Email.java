package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Email implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private String sender;
    private ArrayList<String> receivers;
    private Email forwarded;
    private String subject;
    private String text;
    private Date date;
    private String options;

    public Email() {
    }

    public Email(int id, String sender, ArrayList<String> receivers, Email forwarded, String subject, String text, Date date, String options) {
        this.id = id;
        this.sender = sender;
        this.receivers = receivers;
        this.forwarded = forwarded;
        this.subject = subject;
        this.text = text;
        this.date = date;
        this.options = options;
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

    public Email getForwarded() {
        return forwarded;
    }

    public void setForwarded(Email forwarded) {
        this.forwarded = forwarded;
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

    public String getOptions() { return options; }

    public void setOptions(String options) { this.options = options; }

    public String toJson() { //trasforma l'oggetto Email in una stringa json
        Gson gson = new Gson(); //istanzio oggetto gson di tipo Gson
        Type fooType = new TypeToken<Email>() {}.getType(); //definisco il tipo di oggetto da serializzare tramite classe Type, specificando tra <> la classe del nostro oggetto
        String json = gson.toJson(this,fooType); //lancio metodo gson.toJson(mio_ogetto,ogetto_tipo) con nostro oggetto instanziato e il tipo, ci restituirà la stringa JSON.
        return json;
    }

    public Email fromJson(String jsString){
        Gson gson = new Gson();
        String jsStr = jsString;
        Type fooType =  new TypeToken() {}.getType();
        Email email = gson.fromJson(jsString,fooType);
        return email;
    }

    @Override
    public String toString() {
        return String.join(" - ", List.of(this.sender,this.subject));
    }
}
