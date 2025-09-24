
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

//https://stackoverflow.com/questions/16351668/compression-and-decompression-of-string-data-in-java

public class Compression {
    /**
     * Compresses a string using GZIP compression
     * 
     * @param str The string to compress
     * @return The compressed string, or the original string if null/empty
     * @throws Exception if compression fails
     */
    public static String compress(String str) throws Exception {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return Base64.getEncoder().encodeToString(obj.toByteArray());
    }

    /**
     * Decompresses a GZIP compressed string back to original text
     * 
     * @param str The compressed string to decompress
     * @return The original decompressed string, or the original string if
     *         null/empty
     * @throws Exception if decompression fails or the string is not in valid GZIP
     *                   format
     */
    public static String decompress(String str) throws Exception {
        if (str == null || str.length() == 0) {
            return str;
        }
        byte[] byteArray = Base64.getDecoder().decode(str);
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(byteArray));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String outStr = "";
        String line;
        while ((line = bf.readLine()) != null) {
            outStr += line;
        }
        return outStr;
    }
}
