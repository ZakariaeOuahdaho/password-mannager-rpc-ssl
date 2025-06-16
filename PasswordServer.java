

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.concurrent.*;

public class PasswordServer {
    private static final int PORT = 9443;
    private SSLServerSocket serverSocket;
    private DatabaseManager dbManager;
    private ExecutorService threadPool;
    private volatile boolean running;

    public PasswordServer() {
        this.dbManager = new DatabaseManager();
        this.threadPool = Executors.newCachedThreadPool();
        initializeSSLContext();
    }

    private SSLContext sslContext;

    private void initializeSSLContext() {
        try {
            // Charger le keystore
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream keystoreStream = new FileInputStream("server-keystore.p12")) {
                if (keystoreStream == null) {
                    throw new FileNotFoundException("Keystore non trouvé");
                }
                keyStore.load(keystoreStream, "password".toCharArray());
            }

            // Initialiser KeyManagerFactory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "password".toCharArray());

            // Créer le contexte SSL
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

        } catch (Exception e) {
            System.err.println("Erreur d'initialisation SSL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            // Créer SSLServerSocketFactory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);

            // Configuration sécurisée des protocoles
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});

            // Activer l'authentification du client (optionnel)
            serverSocket.setNeedClientAuth(false);

            running = true;
            System.out.println("Serveur sécurisé démarré sur le port " + PORT);

            // Boucle principale d'acceptation des connexions
            while (running) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

                    // Log de connexion
                    System.out.println("Nouvelle connexion de : " +
                            clientSocket.getInetAddress().getHostAddress());

                    // Gestion du client dans un thread du pool
                    threadPool.submit(new ServerHandler(clientSocket, dbManager));

                } catch (IOException e) {
                    if (running) {
                        System.err.println("Erreur d'acceptation de connexion : " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de démarrage du serveur : " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        try {
            // Fermeture du ServerSocket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Arrêt du thread pool
            if (threadPool != null) {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            }

            System.out.println("Serveur arrêté correctement");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'arrêt du serveur : " + e.getMessage());
        }
    }

    // Méthode de démarrage principale
    public static void main(String[] args) {
        PasswordServer server = new PasswordServer();
        server.start();
    }

    // Méthodes utilitaires supplémentaires
    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return PORT;
    }
}
