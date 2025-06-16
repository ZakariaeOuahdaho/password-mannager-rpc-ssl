

import java.io.Serializable;

public class Response implements Serializable {
    private boolean success;
    private String message;
    private Object data;

    // Constructeurs
    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }

    // Méthode toString optionnelle
    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
