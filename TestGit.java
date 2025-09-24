import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TestGit {
    public static void main(String[] args) {
        testBasicRepoOperations();
        testBlobFileCreation();
        testIndexOperations();
        MyRepo.cleanupLocalFiles();
    }

    // Tests basic repository operations including file search and directory removal
    public static void testBasicRepoOperations() {
        System.out.println("Testing Basic Repository Operations");

        MyRepo repo = new MyRepo("NewRepo", true);
        File dir1 = new File("NewRepo/dir1");
        dir1.mkdir();
        File file1 = new File("NewRepo/dir1/file1.txt");

        try {
            file1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("File exists test:");
        System.out.println(repo.includesFile("file1.txt")); // true
        System.out.println(repo.includesFile("file2.txt")); // false

        System.out.println("File search test:");
        System.out.println(repo.findFile("file1.txt")); // path to file1.txt
        System.out.println(repo.findFile("file2.txt")); // null

        System.out.println("Directory removal test:");
        System.out.println(repo.removeDirectory(dir1.getName())); // true

        System.out.println("Repository cleanup test:");
        System.out.println(repo.cleanup()); // true
        System.out.println();
    }

    // Tests blob file creation and compression functionality

    public static void testBlobFileCreation() {
        System.out.println("Testing Blob File Creation");

        MyRepo repo2 = new MyRepo("NewRepo", true);
        File dir2 = new File("NewRepo/dir2");
        dir2.mkdir();
        File file2 = new File("NewRepo/dir2/file2.txt");

        try {
            file2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write content to file
        try {
            BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(file2));
            bw.write("Hello World");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("File inclusion tests:");
        System.out.println(repo2.includesFile("file1.txt")); // false
        System.out.println(repo2.includesFile("file2.txt")); // true

        System.out.println("File finding tests:");
        System.out.println(repo2.findFile("file1.txt")); // null
        System.out.println(repo2.findFile("file2.txt")); // path to file2.txt

        // Test blob file creation and verification
        try {
            repo2.createBlobFile(file2.getName());
            File blobFile = new File(repo2.gitFolder.getPath() + "/objects/"
                    + Sha1Generator.generateSha1(Compression.compress("Hello World\n")));
            System.out.println("Blob file exists: " + blobFile.exists()); // true
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Directory and repo cleanup:");
        System.out.println(repo2.removeDirectory(dir2.getName())); // true
        System.out.println(repo2.cleanup()); // true
        System.out.println();
    }

    // Tests index operations including adding files to the git index
    public static void testIndexOperations() {
        System.out.println("Testing Index Operations");

        MyRepo repo3 = new MyRepo("NewRepo", true);
        File dir3 = new File("NewRepo/dir3");
        dir3.mkdir();
        File file3 = new File("NewRepo/dir3/file3.txt");
        File file4 = new File("NewRepo/git/file4.txt");

        // Create files
        try {
            file3.createNewFile();
            file4.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write content to files
        try {
            BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(file3));
            bw.write("Hello World");
            bw.close();
            BufferedWriter bw2 = new BufferedWriter(new java.io.FileWriter(file4));
            bw2.write("Hello World");
            bw2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create blob files and add to index
        try {
            repo3.createBlobFile(file3.getName());
            repo3.createBlobFile(file4.getName());
            String blobHash = Sha1Generator.generateSha1(Compression.compress("Hello World\n"));

            System.out.println("Adding files to index:");
            System.out.println(repo3.addFileToIndex(blobHash, file3.getName())); // true
            System.out.println(repo3.addFileToIndex(blobHash, file4.getName())); // true

            System.out.println("Index contents:");
            BufferedReader br = new BufferedReader(new FileReader("NewRepo/git/index"));
            String line = br.readLine();
            System.out.println(line); // blob hash and file name
            line = br.readLine();
            System.out.println(line); // blob hash and file name
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Final cleanup:");
        System.out.println(repo3.removeDirectory(dir3.getName())); // true
        System.out.println(repo3.cleanup()); // true

    }

}
