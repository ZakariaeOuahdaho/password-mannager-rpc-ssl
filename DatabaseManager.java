
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.util.*;

public class DatabaseManager {
    private static final String DATA_FILE = "passwords.json";
    private Map<String, User> users;
    private Map<String, List<PasswordEntry>> passwords;

    public DatabaseManager() {
        users = new HashMap<>();
        passwords = new HashMap<>();
        loadData();
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            saveData();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(reader);

            // Charger les utilisateurs
            JSONObject usersJson = (JSONObject) data.get("users");
            for (Object key : usersJson.keySet()) {
                String username = (String) key;
                JSONObject userJson = (JSONObject) usersJson.get(username);
                User user = new User(username, (String) userJson.get("password"));
                users.put(username, user);
            }

            // Charger les mots de passe
            JSONObject passwordsJson = (JSONObject) data.get("passwords");
            for (Object key : passwordsJson.keySet()) {
                String username = (String) key;
                JSONArray passwordsList = (JSONArray) passwordsJson.get(username);
                List<PasswordEntry> userPasswords = new ArrayList<>();

                for (Object passObj : passwordsList) {
                    JSONObject passJson = (JSONObject) passObj;
                    PasswordEntry entry = new PasswordEntry(
                            (String) passJson.get("service"),
                            (String) passJson.get("username"),
                            (String) passJson.get("password")
                    );
                    userPasswords.add(entry);
                }
                passwords.put(username, userPasswords);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private synchronized void saveData() {
        JSONObject data = new JSONObject();

        // Sauvegarder les utilisateurs
        JSONObject usersJson = new JSONObject();
        for (Map.Entry<String, User> entry : users.entrySet()) {
            JSONObject userJson = new JSONObject();
            userJson.put("password", SecurityManager.hashPassword(entry.getValue().getPassword()));
            usersJson.put(entry.getKey(), userJson);
        }
        data.put("users", usersJson);

        // Sauvegarder les mots de passe
        JSONObject passwordsJson = new JSONObject();
        for (Map.Entry<String, List<PasswordEntry>> entry : passwords.entrySet()) {
            JSONArray passwordsList = new JSONArray();
            for (PasswordEntry passEntry : entry.getValue()) {
                JSONObject passJson = new JSONObject();
                passJson.put("service", passEntry.getService());
                passJson.put("username", passEntry.getUsername());
                passJson.put("password", SecurityManager.encryptPassword(passEntry.getPassword()));
                passwordsList.add(passJson);
            }
            passwordsJson.put(entry.getKey(), passwordsList);
        }
        data.put("passwords", passwordsJson);

        try (FileWriter file = new FileWriter(DATA_FILE)) {
            file.write(data.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des données: " + e.getMessage());
        }
    }

    public synchronized boolean addUser(User user) {
        if (users.containsKey(user.getUsername())) {
            return false;
        }
        String hashedPassword = SecurityManager.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);
        users.put(user.getUsername(), user);
        passwords.put(user.getUsername(), new ArrayList<>());
        saveData();
        return true;
    }

    public synchronized boolean validateUser(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            System.out.println("Utilisateur non trouvé: " + username);
            return false;
        }

        String hashedPassword = SecurityManager.hashPassword(password);
        String storedPassword = user.getPassword();

        return storedPassword.equals(hashedPassword);
    }

    public synchronized boolean addPassword(String username, PasswordEntry entry) {
        if (!users.containsKey(username)) {
            return false;
        }

        // Encrypt password before storing
        String encryptedPassword = SecurityManager.encryptPassword(entry.getPassword());
        entry.setPassword(encryptedPassword);

        List<PasswordEntry> userPasswords = passwords.get(username);
        if (userPasswords == null) {
            userPasswords = new ArrayList<>();
            passwords.put(username, userPasswords);
        }

        userPasswords.add(entry);
        saveData();
        return true;
    }

    public synchronized List<PasswordEntry> getUserPasswords(String username) {
        List<PasswordEntry> userPasswords = passwords.getOrDefault(username, new ArrayList<>());

        // Déchiffrer les mots de passe avant de retourner
        for (PasswordEntry entry : userPasswords) {
            String decryptedPassword = SecurityManager.decryptPassword(entry.getPassword());
            entry.setPassword(decryptedPassword);
        }

        return userPasswords;
    }

    public String getUserPassword(String username) {
        User user = users.get(username);
        return user != null ? user.getPassword() : null;
    }

    public synchronized boolean deletePassword(String username, String service) {
        List<PasswordEntry> userPasswords = passwords.get(username);
        if (userPasswords == null) return false;

        boolean removed = userPasswords.removeIf(p -> p.getService().equals(service));
        if (removed) {
            saveData();
        }
        return removed;
    }
}
