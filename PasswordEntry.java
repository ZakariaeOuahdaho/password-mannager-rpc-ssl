

import java.io.Serializable;

public class PasswordEntry implements Serializable {
    private String service;
    private String username;
    private String password;
    private String owner;
    private String notes;
    private long creationDate;

    public PasswordEntry(String service, String username, String password) {
        this.service = service;
        this.username = username;
        this.password = password;
        this.creationDate = System.currentTimeMillis();
    }

    // Getters
    public String getService() { return service; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getNotes() { return notes; }
    public long getCreationDate() { return creationDate; }
    public String getOwner() { return owner; }

    // Setters
    public void setService(String service) { this.service = service; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setOwner(String owner) { this.owner = owner; }

    @Override
    public String toString() {
        return service + " - " + username;
    }
}
