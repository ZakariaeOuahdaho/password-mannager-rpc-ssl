
public class Main {
    public static void main(String[] args) {
        try {
            // Supprimez cette ligne ou commentez-la
            // System.setSecurityManager(new SecurityManager());

            // Démarrage du serveur
            PasswordServer server = new PasswordServer();

            // Ajout d'un hook d'arrêt propre
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Arrêt du serveur en cours...");
                server.shutdown();
            }));

            // Lancement du serveur
            server.start();

        } catch (Exception e) {
            System.err.println("Erreur fatale de démarrage du serveur : " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
