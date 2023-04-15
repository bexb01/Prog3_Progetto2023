package controller;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.Email;

public class ServerController implements Initializable {
    @FXML
    private ListView<String> messagesListView;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnClose;
    private Server server;
    private Thread pServer;
    private final ObservableList<String> logList = FXCollections.observableList(new LinkedList<>());
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messagesListView.setItems(logList);
    }

    public void onStartButtonClick(){
        this.server = new Server(8189);
        this.pServer = new Thread(server);
        pServer.setDaemon(true);
        pServer.start();
        logList.add("[WARNING] Starting Server...");
        btnStart.setDisable(true);
        btnClose.setDisable(false);

    }

    public void onStopButtonClick() throws IOException {
        logList.add("[WARNING] Closing Server...");
        server.shutdownServer();
        while(pServer.isAlive()){

        }
        btnClose.setDisable(true);
        btnStart.setDisable(false);
        Platform.runLater(() -> {logList.add("[WARNING] Server closed...");});
    }

    public class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        private String usernameClient;
        private String clientInboxFile;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.in = new ObjectInputStream(clientSocket.getInputStream());
                this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println("Error while creating input/output streams for client: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                Object clientObject;
                if((clientObject = in.readObject()) != null) {
                    if(clientObject instanceof String) {
                        String mex = (String)clientObject;
                        switch (mex) {
                            case "send":
                                sendEmail();
                                break;
                            case "delete":
                                getUsername();
                                deleteEmail();
                                break;
                            case "get":
                                getUsername();
                                clientInboxFile = createFileInbox(usernameClient);
                                Platform.runLater(() -> {logList.add( usernameClient + " ha richiesto tutte le sue mail");});
                                getAllEmails();
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error while reading object from client" + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println("Error while reading object from client: " + e.getMessage());
            }
            // Close the client socket and streams
            closeConnection();
        }
        private void getUsername()
                throws IOException, ClassNotFoundException {
            Object username;
            if((username = in.readObject()) != null) {
                if(username instanceof String) {
                    this.usernameClient= (String)username;
                }
            }

        }
        private void sendEmail()
                throws IOException, ClassNotFoundException {
            Object addEmail;
            if((addEmail = in.readObject()) != null) {
                if(addEmail instanceof Email) {
                    Email email = (Email) addEmail;
                    String fileSander = "files/" + email.getSender() + "/inbox.txt";
                    for (String s : email.getReceivers()) {
                        logList.add("Inviata email da " + email.getSender() + " a " + s);
                        //send email to receivers (scrive email sul file del receiver)
                        String FileReceiver = "files/" + s + "/inbox.txt";
                        try (FileWriter writer = new FileWriter(FileReceiver, true)) {
                            writer.write(email.toJson() + "\n");
                            writer.close();//rilascio la risorsa utilizzata dal writer, inoltre notifica al so che il  file non e' piu in uso
                            System.out.println("email scritta su file del receiver: " + FileReceiver);
                        } catch (IOException e) {
                            System.out.println("An error occurred while writing to the file.");
                            e.printStackTrace();//manipolare l'errore nell'invio di una mail al sander che dice l'accaduto cosi ch potra reinviare la mai con indirizzo email corretto
                        }
                    }
                    try (FileWriter writer = new FileWriter(fileSander, true)) {
                        writer.write(email.toJson()+"\n");
                        writer.close();//rilascio la risorsa utilizzata dal writer, inoltre notifica al so che il  file non e' piu in uso
                        System.out.println("email scritta su file del sender: " + fileSander);

                    } catch (FileNotFoundException e) {
                        System.out.println("An error occurred while writing to the file.");
                        e.printStackTrace();
                    }
                }
            }
        }
        private void deleteEmail()  //DA MODIFICARE SOLO PARTE CHE CANCELLA EFFETTIVAMENTE DA FILE
                throws IOException, ClassNotFoundException{
            Object deleteEmail;
            if((deleteEmail = in.readObject()) != null) {
                if(deleteEmail instanceof Email) {
                    Email ToDelEmail = (Email) deleteEmail;
                    int idFromFile=0;
                    int idToDelEm= ToDelEmail.getId();
                    String jsonToDelEm= ToDelEmail.toJson();
                    //implement method to delete email
                    String filePath = "files/" + usernameClient + "/inbox.txt";
                    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                        String line = reader.readLine();
                        StringBuilder builder = new StringBuilder();
                        while (line != null) {
                            Email emailTemp=Email.fromJson(line);
                            idFromFile=emailTemp.getId();
                            if (idToDelEm != idFromFile) {
                                builder.append(line).append("\n");
                            }
                            line = reader.readLine();
                        }
                        String newContent = builder.toString();
                        try (FileWriter writer = new FileWriter(filePath)) {
                            writer.write(newContent);
                            System.out.println("Stringa " + jsonToDelEm + " eliminata dal file " + filePath);
                        } catch (IOException e) {
                            System.out.println("An error occurred while writing to the file.");
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        System.out.println("An error occurred while reading the file.");
                        e.printStackTrace();
                    }
                    Platform.runLater(() -> {logList.add(usernameClient + " ha eliminato la mail " + ToDelEmail.getId() + " mandata da " + ToDelEmail.getSender());});
                }
            }
        }

        private void getAllEmails() throws IOException, ClassNotFoundException{
            try {
                Object numberEmail;
                if((numberEmail = in.readObject()) != null) {
                    if(numberEmail instanceof Integer) {
                        int num = (Integer)numberEmail;
                        FileReader fr = new FileReader(clientInboxFile);
                        Scanner reader = new Scanner(fr);
                        String data;
                        ArrayList<Email> listaEmail = new ArrayList<>();
                        // Skip the first num lines
                        for (int i = 0; i < num; i++) {
                            if (reader.hasNextLine()) {
                                reader.nextLine();
                            }
                        }
                        while (reader.hasNextLine()) {
                            data = reader.nextLine();
                            if(data!=null) {
                                Email e = Email.fromJson(data);
                                listaEmail.add(e);
                            }
                        }
                        reader.close();
                        fr.close();
                        try {
                            out.writeObject(listaEmail);
                            Platform.runLater(() -> {logList.add("Ho inviato tutte le mail a " + usernameClient+ ".");});
                        }catch (Exception ex){
                            System.err.println("Errore nella ricezione della lista email " + ex.getMessage());
                        }
                    }
                }
            }catch(IOException e){
                System.out.println("Errore nella chiusura del file-reader" + e.getMessage());
            }
        }

        private void closeConnection(){
            try {
                // Close connection with client
                clientSocket.close();
                //close input-output streams:
                in.close();
                out.close();
            } catch (IOException e) {
                System.out.println("Error while closing client socket and streams: " + e.getMessage());
            }
        }

        public static String createFileInbox(String username)
                throws IllegalArgumentException, IOException {

            if (username == null)
                throw new IllegalArgumentException("[Illegal Argument]: username to be created must be not null");
            Files.createDirectories(Paths.get("files/" + username));
            File inbox = new File("files/" + username + "/" + "inbox.txt");
            inbox.createNewFile();
            return "files/" + username + "/" + "inbox.txt";
        }
    }


    public class Server implements Runnable{
        private static int PORT;
        private ServerSocket serverSocket;
        private AtomicBoolean running = new AtomicBoolean(true);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        public Server(int port) {
            this.PORT = port;

        }

        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);
                //create thread
                while (running.get()) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientH = new ClientHandler(clientSocket);
                    Thread t = new Thread(clientH);
                    executor.execute(t);
                }
                // Wait termination
                executor.shutdown();
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.err.println("Error while accepting client connection: " + e.getMessage());
            }
        }

        public void shutdownServer() throws IOException {
            // Stop accepting new requests
            running.set(false);

        }
    }
}





