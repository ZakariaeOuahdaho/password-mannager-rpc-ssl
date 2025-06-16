

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private boolean isLoggedIn;

    // Constructeurs
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isLoggedIn() { return isLoggedIn; }

    // Setters
    public void setPassword(String password) { this.password = password; }
    public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }

    // Méthode equals pour comparaison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    // Méthode hashCode correspondante
    @Override
    public int hashCode() {
        return username.hashCode();
    }

    // Méthode toString
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", isLoggedIn=" + isLoggedIn +
                '}';
    }
}
