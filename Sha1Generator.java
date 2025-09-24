import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

//https://ssojet.com/hashing/sha-1-in-java/
public class Sha1Generator {
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