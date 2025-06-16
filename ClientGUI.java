


import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.List;

public class ClientGUI extends JFrame {
    private PasswordClient client;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // Composants login
    private JPanel loginPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Composants liste mots de passe
    private JPanel passwordPanel;
    private JList<String> passwordList;
    private DefaultListModel<String> listModel;
    private JPopupMenu passwordPopupMenu;
    private List<PasswordEntry> currentPasswords;

    public ClientGUI() {
        client = new PasswordClient();
        setupGUI();
        connectToServer();
    }

    private void setupGUI() {
        setTitle("Gestionnaire de mots de passe sécurisé");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        setupLoginPanel();
        setupPasswordPanel();

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(passwordPanel, "PASSWORDS");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    private void setupLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Connexion");
        JButton registerButton = new JButton("S'inscrire");

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Nom d'utilisateur:"), gbc);

        gbc.gridy = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        loginPanel.add(new JLabel("Mot de passe:"), gbc);

        gbc.gridy = 3;
        loginPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridy = 4;
        loginPanel.add(buttonPanel, gbc);

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegister());
    }

    private void setupPasswordPanel() {
        passwordPanel = new JPanel(new BorderLayout());

        // Liste des mots de passe
        listModel = new DefaultListModel<>();
        passwordList = new JList<>(listModel);
        passwordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Menu contextuel
        passwordPopupMenu = new JPopupMenu();
        JMenuItem copyPasswordItem = new JMenuItem("Copier le mot de passe");
        JMenuItem copyUsernameItem = new JMenuItem("Copier le nom d'utilisateur");
        JMenuItem deletePasswordItem = new JMenuItem("Supprimer le mot de passe");

        copyPasswordItem.addActionListener(e -> copyPasswordToClipboard());
        copyUsernameItem.addActionListener(e -> copyUsernameToClipboard());
        deletePasswordItem.addActionListener(e -> deletePassword());

        passwordPopupMenu.add(copyPasswordItem);
        passwordPopupMenu.add(copyUsernameItem);
        passwordPopupMenu.add(deletePasswordItem);

        // Listener pour le clic droit
        passwordList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = passwordList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        passwordList.setSelectedIndex(index);
                        passwordPopupMenu.show(passwordList, e.getX(), e.getY());
                    }
                }
            }
        });

        // Bouton d'ajout
        JButton addButton = new JButton("Ajouter un mot de passe");
        addButton.addActionListener(e -> showAddPasswordDialog());

        // Bouton de déconnexion
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.addActionListener(e -> logout());

        // Layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(logoutButton);

        passwordPanel.add(new JScrollPane(passwordList), BorderLayout.CENTER);
        passwordPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void connectToServer() {
        try {
            client.connect();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de connexion au serveur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.trim().isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un nom d'utilisateur et un mot de passe",
                    "Erreur de connexion",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (client.login(username, password)) {
                refreshPasswordList();
                cardLayout.show(mainPanel, "PASSWORDS");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Identifiants incorrects",
                        "Erreur de connexion",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de connexion : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.trim().isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un nom d'utilisateur et un mot de passe",
                    "Erreur d'inscription",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (client.register(username, password)) {
                JOptionPane.showMessageDialog(this,
                        "Inscription réussie",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Échec de l'inscription",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur d'inscription : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshPasswordList() {
        listModel.clear();

        try {
            currentPasswords = client.getPasswords();

            for (PasswordEntry entry : currentPasswords) {
                listModel.addElement(entry.getService() + " - " + entry.getUsername());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de récupération des mots de passe : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddPasswordDialog() {
        JTextField serviceField = new JTextField();
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        Object[] message = {
                "Service:", serviceField,
                "Nom d'utilisateur:", userField,
                "Mot de passe:", passField
        };

        int option = JOptionPane.showConfirmDialog(this, message,
                "Ajouter un mot de passe", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String service = serviceField.getText();
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (client.addPassword(service, username, password)) {
                refreshPasswordList();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'ajout du mot de passe",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyPasswordToClipboard() {
        int selectedIndex = passwordList.getSelectedIndex();
        if (selectedIndex >= 0 && currentPasswords != null) {
            PasswordEntry entry = currentPasswords.get(selectedIndex);
            copyToClipboard(entry.getPassword());
            JOptionPane.showMessageDialog(this,
                    "Mot de passe copié dans le presse-papiers",
                    "Copié", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void copyUsernameToClipboard() {
        int selectedIndex = passwordList.getSelectedIndex();
        if (selectedIndex >= 0 && currentPasswords != null) {
            PasswordEntry entry = currentPasswords.get(selectedIndex);
            copyToClipboard(entry.getUsername());
            JOptionPane.showMessageDialog(this,
                    "Nom d'utilisateur copié dans le presse-papiers",
                    "Copié", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deletePassword() {
        int selectedIndex = passwordList.getSelectedIndex();
        if (selectedIndex >= 0 && currentPasswords != null) {
            PasswordEntry entry = currentPasswords.get(selectedIndex);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Voulez-vous vraiment supprimer ce mot de passe ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // À implémenter dans PasswordClient
                // if (client.deletePassword(entry.getService())) {
                //     refreshPasswordList();
                // } else {
                //     JOptionPane.showMessageDialog(this,
                //         "Erreur lors de la suppression du mot de passe",
                //         "Erreur", JOptionPane.ERROR_MESSAGE);
                // }
            }
        }
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    private void logout() {
        client.disconnect();
        cardLayout.show(mainPanel, "LOGIN");
        usernameField.setText("");
        passwordField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}
