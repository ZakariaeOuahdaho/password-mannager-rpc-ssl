

import java.io.Serializable;

public class Request implements Serializable {
    private String action;
    private Object data;

    public Request(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() { return action; }
    public Object getData() { return data; }

    // Méthode toString optionnelle pour le débogage
    @Override
    public String toString() {
        return "Request{" +
                "action='" + action + '\'' +
                ", data=" + data +
                '}';
    }
}
