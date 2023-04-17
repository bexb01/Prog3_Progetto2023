package controller;

import java.io.*;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    /**
     * Apre la connessione del server
     */
    public void onStartButtonClick(){
        this.server = new Server(8189);
        this.pServer = new Thread(server);
        pServer.setDaemon(true);
        pServer.start();
        logList.add("[WARNING] Starting Server...");
        btnStart.setDisable(true);
        btnClose.setDisable(false);

    }

    /**
     * Chiude la connessione del server
     */
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
        static int id;
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
        }

        /**
         * Riceve dal client il nome utente
         */
        private void getUsername()
                throws IOException, ClassNotFoundException {
            Object username;
            if((username = in.readObject()) != null) {
                if(username instanceof String) {
                    this.usernameClient= (String)username;
                }
            }
        }

        /**
         * Restituisce l'id delle email assegnato dal server
         */
        synchronized public static int getId(){
            try{
                BufferedReader br = new BufferedReader(new FileReader("files/id.txt" ));
                String linea = br.readLine();
                id = Integer.parseInt(linea);
                FileWriter fw = new FileWriter("files/id.txt" );
                id++;
                fw.write(Integer.toUnsignedString(id));
                fw.close();
            } catch(IOException e){
                System.err.println("Errore get ID: " + e);
            }
            return(id-1);
        }

        /**
         * Trova l'indice del lock relativo all'utente passato come parametro
         */
        private int getLockIndex(String username){
            int i = 0;
            while(i < Server.users.size()){
                if(Server.users.get(i).equals(username)){
                    return i;
                }
                i++;
            }
            return i;
        }

        /**
         * Scrive email nel file inbox di mittente e destinatari
         */
        private void sendEmail()
                throws IOException, ClassNotFoundException {
            Object addEmail;
            if((addEmail = in.readObject()) != null) {
                if(addEmail instanceof Email email) {
                    System.out.println(email.getId());
                    String fileSander = "files/" + email.getSender() + "/inbox.txt";
                    //SENDER
                    email.setID(getId());
                    int lockIndex = getLockIndex(email.getSender());
                    System.out.println("lock index: " + lockIndex);
                    server.getLocks().get(lockIndex).writeLock().lock();
                    try (FileWriter writer = new FileWriter(fileSander, true)) {
                        writer.write(email.toJson()+"\n");
                        writer.close(); //rilascio la risorsa utilizzata dal writer, inoltre notifica al so che il file non è piu in uso
                    } catch (IOException e) {
                        System.out.println("An error occurred while writing to the file.");
                        e.printStackTrace();
                    }
                    server.getLocks().get(lockIndex).writeLock().unlock();
                    System.out.println("email scritta su file del sender: " + fileSander);
                    //RECEIVERS
                    for (String s : email.getReceivers()) {
                        lockIndex = getLockIndex(s);
                        System.out.println("lock index: " + lockIndex);
                        Platform.runLater(() -> {logList.add("Inviata email da " + email.getSender() + " a " + s);});
                        //send email to receivers (scrive email sul file del receiver)
                        String FileReceiver = "files/" + s + "/inbox.txt";
                        if(!email.getSender().equals(s)) {
                            email.setID(getId());
                            server.getLocks().get(lockIndex).writeLock().lock();
                            //lock.lock();__> lock del receiver (nel ciclo for quindi fatto per ogni receiver)
                            try(FileWriter writer = new FileWriter(FileReceiver, true)) {
                                    writer.write(email.toJson() + "\n");
                                    writer.close();//rilascio la risorsa utilizzata dal writer, inoltre notifica al so che il  file non e' piu in uso
                                    System.out.println("email scritta su file del receiver: " + FileReceiver);
                            } catch (FileNotFoundException e) {
                                System.out.println("file del receiver non trovato. controllare sander client per maggiori informazioni");
                                email.setID(getId());
                                //ho gia il lock
                                //setto la mail madata dal sistema
                                Email SisRecPosta=new Email();
                                ArrayList<String> rec= new ArrayList<String>();
                                rec.add(email.getSender());
                                SisRecPosta.setReceivers(rec);
                                SisRecPosta.setSender("Sistema di recapito posta");
                                String oldText= "\t"+email.getText()+"\n\t"+email.getDate()+"\n\n";
                                String fileNFoundText= "Il tuo messaggio non è stato recapitato a " + s + " perchè l'indirizzo risulta inesistente. Prova a ricontrollare l'indirizzo email per eventuali errori.\n\n Sei pregato di non rispondere a questa mail in quanto \"Sistema di recapito posta\" non è un indirizzo valido.";
                                SisRecPosta.setText(oldText+fileNFoundText);
                                SisRecPosta.setDate(new Date());
                                SisRecPosta.setSubject(email.getSubject());
                                try (FileWriter writer = new FileWriter(fileSander, true)) {
                                    writer.write(SisRecPosta.toJson()+"\n");
                                    writer.close(); //rilascio la risorsa utilizzata dal writer, inoltre notifica al so che il file non è piu in uso
                                } catch (IOException er) {
                                    System.out.println("An error occurred while writing to the file.");
                                    er.printStackTrace();
                                }
                                System.out.println("email di sistema scritta su file del sender: " + fileSander);
                            }
                            server.getLocks().get(lockIndex).writeLock().unlock();
                        }
                    }
                }
            }
        }

        /**
         * Rimuove email dal file inbox
         */
        private void deleteEmail()
                throws IOException, ClassNotFoundException{
            Object deleteEmail;
            if((deleteEmail = in.readObject()) != null) {
                if(deleteEmail instanceof Email) {
                    Email ToDelEmail = (Email) deleteEmail;
                    int idFromFile=0;
                    int idToDelEm= ToDelEmail.getId();
                    String jsonToDelEm= ToDelEmail.toJson();
                    String filePath = "files/" + usernameClient + "/inbox.txt"; //---> change with clientinboxfile?
                    int lockIndex = getLockIndex(usernameClient);
                    System.out.println("lock index: " + lockIndex);
                    server.getLocks().get(lockIndex).writeLock().lock();
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
                            server.getLocks().get(lockIndex).writeLock().unlock();
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

        /**
         * Legge le email presenti nel file inbox e le invia al client
         */
        private void getAllEmails() throws IOException, ClassNotFoundException{
            try {
                Object numberEmail;
                if((numberEmail = in.readObject()) != null) {
                    if(numberEmail instanceof Integer) {
                        int num = (Integer)numberEmail;
                        int lockIndex = getLockIndex(usernameClient);
                        System.out.println("lock index: " + lockIndex);
                        server.getLocks().get(lockIndex).readLock().lock();
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
                        server.getLocks().get(lockIndex).readLock().unlock();
                        try {
                            if(listaEmail.size()==0){
                                out.writeObject(666);
                            }else {
                                out.writeObject(listaEmail);
                            }
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


        /**
         * Crea il file inbox per l'utente passato come parametro
         */
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
        private static ArrayList<String> users;
        protected final List<ReentrantReadWriteLock> locks;
        ExecutorService executor = Executors.newFixedThreadPool(10);

        public Server(int port) {
            PORT = port;
            users = new ArrayList<>(Arrays.asList("Jake.Peralta@unito.it", "Alphonse.Elric@unito.it", "Amy.Santiago@unito.it", "Chuck.Bartowski@unito.it",
                            "Sarah.Walker@unito.it", "Tony.Stark@unito.it", "Edward.Elric@unito.it", "Pepper.Potts@unito.it"));
            this.locks = new ArrayList<>();
            for(int i=0; i<= users.size(); i++){
                locks.add(new ReentrantReadWriteLock());
            }
        }

        public List<ReentrantReadWriteLock> getLocks(){
            return locks;
        }

        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);
                //create thread
                while (running.get()) {
                    System.out.println("1-Chiedo roba");
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientH = new ClientHandler(clientSocket);
                    Thread t = new Thread(clientH);
                    executor.execute(t);
                }
                // Wait termination
                executor.shutdown();
                executor.awaitTermination(10, TimeUnit.SECONDS);
                serverSocket.close();
            } catch (Exception e) {
                System.err.println("Error while accepting client connection: " + e.getMessage());
            }
        }

        /**
         * Cambia lo stato della variabile per far spegnere il server
         */
        public void shutdownServer() throws IOException {
            // Stop accepting new requests
            running.set(false);

        }
    }
}





