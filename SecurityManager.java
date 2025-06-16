

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecurityManager {
    private static final String XOR_KEY = "SECURE_KEY123"; // Clé pour le XOR
    private static final String AES_SECRET_KEY = "YourSecretKey123"; // Clé AES

    // Hashage de mot de passe avec SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    // Encryption XOR simple
    public static String encryptPassword(String password) {
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            encrypted.append((char) (password.charAt(i) ^ XOR_KEY.charAt(i % XOR_KEY.length())));
        }
        return Base64.getEncoder().encodeToString(encrypted.toString().getBytes());
    }

    // Decryption XOR
    public static String decryptPassword(String encryptedPassword) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
            String decoded = new String(decodedBytes);
            StringBuilder decrypted = new StringBuilder();
            for (int i = 0; i < decoded.length(); i++) {
                decrypted.append((char) (decoded.charAt(i) ^ XOR_KEY.charAt(i % XOR_KEY.length())));
            }
            return decrypted.toString();
        } catch (Exception e) {
            return encryptedPassword;
        }
    }

    // Méthode de chiffrement AES (optionnelle, plus sécurisée)
    public static String encryptAES(String strToEncrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(AES_SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes()));
        } catch (Exception e) {
            System.err.println("Erreur de chiffrement AES : " + e.getMessage());
            return null;
        }
    }

    // Méthode de déchiffrement AES (optionnelle)
    public static String decryptAES(String strToDecrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(AES_SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.err.println("Erreur de déchiffrement AES : " + e.getMessage());
            return null;
        }
    }

    // Génération de sel (optionnel pour sécurité supplémentaire)
    public static String generateSalt() {
        return Base64.getEncoder().encodeToString(
                java.security.SecureRandom.getSeed(16)
        );
    }
}
