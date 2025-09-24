import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

//https://ssojet.com/hashing/sha-1-in-java/

public class Sha1Generator {
    /**
     * Generates a SHA-1 hash from the input string
     * 
     * @param input The string to generate SHA-1 hash for
     * @return The SHA-1 hash as a string
     * @throws Exception if SHA-1 algorithm is not available or hashing fails
     */
    public static String generateSha1(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}