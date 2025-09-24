public class TestCompression {
    public static void main(String[] args) {
        String data = "Hello World";
        try {
            String compressedData = Compression.compress(data);
            System.out.println("Compressed data: " + compressedData);
            String decompressedData = Compression.decompress(compressedData);
            System.out.println("Decompressed data: " + decompressedData);
            if (data.equals(decompressedData)) {
                System.out.println("Compression and decompression work correctly");
            } else {
                System.out.println("Compression and decompression do not work correctly");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
