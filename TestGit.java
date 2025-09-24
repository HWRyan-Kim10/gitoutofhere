import java.io.File;
import java.io.IOException;

public class TestGit {
    public static void main(String[] args) {
        MyRepo repo = new MyRepo("NewRepo", true);
        File dir1 = new File("NewRepo/dir1");
        dir1.mkdir();
        File file1 = new File("NewRepo/dir1/file1.txt");
        try {
            file1.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(repo.includesFile("file1.txt")); // true
        System.out.println(repo.includesFile("file2.txt")); // false

        System.out.println(repo.findFile("file1.txt")); // path to file1.txt
        System.out.println(repo.findFile("file2.txt")); // null

        System.out.println(repo.removeDirectory(dir1.getName())); // also removes file1.txt using the removeFile method,
                                                                  // so tests that as well

        System.out.println(repo.cleanup());

        MyRepo repo2 = new MyRepo("NewRepo", true);
        File dir2 = new File("NewRepo/dir2");
        dir2.mkdir();
        File file2 = new File("NewRepo/dir2/file2.txt");
        try {
            file2.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(repo2.includesFile("file1.txt")); // false
        System.out.println(repo2.includesFile("file2.txt")); // true

        System.out.println(repo2.findFile("file1.txt")); // null
        System.out.println(repo2.findFile("file2.txt")); // path to file2.txt

        System.out.println(repo2.removeDirectory(dir2.getName())); // also removes file1.txt using the removeFile
                                                                   // method,
                                                                   // so tests that as well

        System.out.println(repo2.cleanup());
    }
}
