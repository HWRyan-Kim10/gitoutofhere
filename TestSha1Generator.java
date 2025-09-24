public class TestSha1Generator {
    public static void main(String[] args) {
        String data = "Hello World";
        try {
            System.out.println("SHA-1 hash: " + Sha1Generator.generateSha1(data));
            if (Sha1Generator.generateSha1(data).equals("0a4d55a8d778e5022fab701977c5d840bbc486d0")) {
                System.out.println("SHA-1 generator works correctly");
            } else {
                System.out.println("SHA-1 generator does not work correctly");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
