


import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.List;

public class ServerHandler implements Runnable {
    private SSLSocket clientSocket;
    private DatabaseManager dbManager;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ServerHandler(SSLSocket socket, DatabaseManager dbManager) {
        this.clientSocket = socket;
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Request request = (Request) in.readObject();
                Response response = handleRequest(request);
                out.writeObject(response);
                out.flush();
            }
        } catch (EOFException e) {
            System.out.println("Client déconnecté");
        } catch (Exception e) {
            System.err.println("Erreur dans le handler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private Response handleRequest(Request request) {
        System.out.println("Action reçue: " + request.getAction());

        switch (request.getAction()) {
            case "REGISTER":
                return handleRegister((User) request.getData());
            case "LOGIN":
                return handleLogin((User) request.getData());
            case "ADD_PASSWORD":
                return handleAddPassword(request);
            case "GET_PASSWORDS":
                return handleGetPasswords((String) request.getData());
            case "DELETE_PASSWORD":
                return handleDeletePassword(request);
            default:
                return new Response(false, "Action non reconnue", null);
        }
    }

    private Response handleRegister(User user) {
        System.out.println("Tentative d'inscription pour: " + user.getUsername());
        if (dbManager.addUser(user)) {
            return new Response(true, "Inscription réussie", null);
        } else {
            return new Response(false, "Nom d'utilisateur déjà utilisé", null);
        }
    }

    private Response handleLogin(User user) {
        System.out.println("Tentative de connexion pour: " + user.getUsername());
        if (dbManager.validateUser(user.getUsername(), user.getPassword())) {
            user.setLoggedIn(true);
            return new Response(true, "Connexion réussie", null);
        } else {
            return new Response(false, "Identifiants invalides", null);
        }
    }

    private Response handleAddPassword(Request request) {
        try {
            PasswordEntry entry = (PasswordEntry) request.getData();
            String username = entry.getOwner();

            System.out.println("Ajout d'un mot de passe pour: " + username);

            if (dbManager.addPassword(username, entry)) {
                return new Response(true, "Mot de passe ajouté avec succès", null);
            } else {
                return new Response(false, "Erreur lors de l'ajout du mot de passe", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Erreur: " + e.getMessage(), null);
        }
    }

    private Response handleGetPasswords(String username) {
        List<PasswordEntry> userPasswords = dbManager.getUserPasswords(username);
        return new Response(true, "Liste des mots de passe récupérée", userPasswords);
    }

    private Response handleDeletePassword(Request request) {
        try {
            // Supposons que le request.getData() contienne un objet avec username et service
            // Vous devrez peut-être créer une classe spécifique pour ce cas
            String username = (String) request.getData();
            String service = (String) request.getData(); // Ajustez selon votre implémentation

            if (dbManager.deletePassword(username, service)) {
                return new Response(true, "Mot de passe supprimé avec succès", null);
            } else {
                return new Response(false, "Impossible de supprimer le mot de passe", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Erreur lors de la suppression", null);
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture des connexions: " + e.getMessage());
        }
    }
}
