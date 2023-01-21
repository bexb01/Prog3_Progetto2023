package model;

import com.example.server.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedList;

public class Model {

    private Server server;
    private final ObservableList<String> logList = FXCollections.observableList(new LinkedList<>());

    public Model(){}

    public void startServer(){
        this.server = new Server(8189,this);
        Thread pServer = new Thread(server);
        //pServer.setDaemon(true);  non va si chiude quando chiudi interfaccia (ultimo thread non deamon)
        pServer.start();
    }

    public Server getServer() {
        return server;
    }

    public ObservableList<String> getLogList() {
        return logList;
    }

    public synchronized void printLog(String s){       //tutti i thread delle varie risposte al server
        logList.add(s);
    }   //i log vengono aggiunti alla lista e vengono chiamati i listener
        //listwiew e observable list si occupano di aggiornare (non serve scrivere i vari passaggi utili per farlo)
}
