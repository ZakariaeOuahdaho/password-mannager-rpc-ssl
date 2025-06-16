
import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.List;
import java.util.ArrayList;

public class PasswordClient {
    private SSLSocket sslSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String currentUser;
    private static final String HOST = "localhost";
    private static final int PORT = 9443;

    public PasswordClient() {
        this.currentUser = null;
    }

    public void connect() throws Exception {
        try {
            // Création du contexte SSL
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // Chargement du KeyStore (utilisé comme TrustStore)
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream keystoreStream = new FileInputStream("server-keystore.p12")) {
                if (keystoreStream == null) {
                    throw new FileNotFoundException("Keystore non trouvé dans les ressources");
                }
                trustStore.load(keystoreStream, "password".toCharArray());
            }

            // Initialisation des TrustManagers
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            // Configuration du contexte SSL
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

            // Création de la socket SSL
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(HOST, PORT);

            // Configuration des protocoles sécurisés
            sslSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});

            // Préparation des flux de communication
            out = new ObjectOutputStream(sslSocket.getOutputStream());
            in = new ObjectInputStream(sslSocket.getInputStream());

            System.out.println("Connecté au serveur SSL");

        } catch (Exception e) {
            System.err.println("Erreur de connexion SSL : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean login(String username, String password) {
        try {
            // Vérifier si le client est connecté
            if (sslSocket == null || sslSocket.isClosed()) {
                connect(); // Reconnexion si nécessaire
            }

            // Création de l'objet utilisateur
            User user = new User(username, password);

            // Envoi de la requête de connexion
            Request loginRequest = new Request("LOGIN", user);
            out.writeObject(loginRequest);
            out.flush();

            // Réception de la réponse
            Response response = (Response) in.readObject();

            // Vérification de la réponse
            if (response.isSuccess()) {
                this.currentUser = username;
                return true;
            } else {
                System.err.println("Échec de connexion : " + response.getMessage());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean register(String username, String password) {
        try {
            // Vérifier si le client est connecté
            if (sslSocket == null || sslSocket.isClosed()) {
                connect(); // Reconnexion si nécessaire
            }

            // Création de l'objet utilisateur
            User user = new User(username, password);

            // Envoi de la requête d'inscription
            Request registerRequest = new Request("REGISTER", user);
            out.writeObject(registerRequest);
            out.flush();

            // Réception de la réponse
            Response response = (Response) in.readObject();

            // Vérification de la réponse
            return response.isSuccess();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'inscription : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean addPassword(String service, String username, String password) {
        try {
            // Vérifier si un utilisateur est connecté
            if (currentUser == null) {
                System.err.println("Aucun utilisateur connecté");
                return false;
            }

            // Création de l'entrée de mot de passe
            PasswordEntry entry = new PasswordEntry(service, username, password);
            entry.setOwner(currentUser);

            // Envoi de la requête d'ajout de mot de passe
            Request addRequest = new Request("ADD_PASSWORD", entry);
            out.writeObject(addRequest);
            out.flush();

            // Réception de la réponse
            Response response = (Response) in.readObject();

            // Vérification de la réponse
            return response.isSuccess();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du mot de passe : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<PasswordEntry> getPasswords() {
        try {
            // Vérifier si un utilisateur est connecté
            if (currentUser == null) {
                System.err.println("Aucun utilisateur connecté");
                return new ArrayList<>();
            }

            // Envoi de la requête de récupération des mots de passe
            Request getPasswordsRequest = new Request("GET_PASSWORDS", currentUser);
            out.writeObject(getPasswordsRequest);
            out.flush();

            // Réception de la réponse
            Response response = (Response) in.readObject();

            // Vérification de la réponse
            if (response.isSuccess()) {
                return (List<PasswordEntry>) response.getData();
            } else {
                System.err.println("Erreur de récupération des mots de passe : " + response.getMessage());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des mots de passe : " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void disconnect() {
        try {
            if (sslSocket != null && !sslSocket.isClosed()) {
                sslSocket.close();
            }
            currentUser = null;
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }

    // Méthode principale de test
    public static void main(String[] args) {
        PasswordClient client = new PasswordClient();
        try {
            // Exemple de connexion et de test
            client.connect();

            // Test d'inscription
            boolean registrationResult = client.register("testuser", "testpassword");
            System.out.println("Inscription : " + registrationResult);

            // Test de connexion
            boolean loginResult = client.login("testuser", "testpassword");
            System.out.println("Connexion : " + loginResult);

            // Test d'ajout de mot de passe
            if (loginResult) {
                boolean addPasswordResult = client.addPassword("TestService", "testusername", "testpassword");
                System.out.println("Ajout de mot de passe : " + addPasswordResult);

                // Test de récupération des mots de passe
                List<PasswordEntry> passwords = client.getPasswords();
                System.out.println("Mots de passe récupérés : " + passwords.size());
            }

        } catch (Exception e) {
            System.err.println("Erreur de test : " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.disconnect();
        }
    }
}
