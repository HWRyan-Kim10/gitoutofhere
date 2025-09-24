
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

//https://stackoverflow.com/questions/16351668/compression-and-decompression-of-string-data-in-java

public class Compression {
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
