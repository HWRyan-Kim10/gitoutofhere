import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class TestGitTreeBuilder {

    public static void main(String[] args) {
        System.out.println("=== GitTreeBuilder Tester ===");
        runTreeBuilderTest();
    }

    public static void runTreeBuilderTest() {
        System.out.println("Creating repository and example files");

        //Create a new repository
        MyRepo repository = new MyRepo("TreeRepo", true);

        //Make a small folder structure:
        // TreeRepo/
        //   main/
        //     a.txt
        //     utils/
        //       b.txt
        //       sub/
        //         c.txt
        File mainFolder = new File("TreeRepo/main");
        File utilsFolder = new File("TreeRepo/main/utils");
        File subFolder = new File("TreeRepo/main/utils/sub");

        mainFolder.mkdirs();
        utilsFolder.mkdirs();
        subFolder.mkdirs();

        File fileA = new File(mainFolder, "a.txt");
        File fileB = new File(utilsFolder, "b.txt");
        File fileC = new File(subFolder, "c.txt");

        try {
            fileA.createNewFile();
            fileB.createNewFile();
            fileC.createNewFile();

            // Write simple text into each file
            BufferedWriter writerA = new BufferedWriter(new FileWriter(fileA));
            writerA.write("Apple");
            writerA.close();

            BufferedWriter writerB = new BufferedWriter(new FileWriter(fileB));
            writerB.write("Banana");
            writerB.close();

            BufferedWriter writerC = new BufferedWriter(new FileWriter(fileC));
            writerC.write("Cherry");
            writerC.close();
        } catch (Exception e) {
            System.out.println("Error while creating or writing files:");
            e.printStackTrace();
            return;
        }

        //Build a tree for the "main" directory
        System.out.println("Building Git tree for 'main'");
        GitTreeBuilder treeBuilder = new GitTreeBuilder(repository);
        String mainTreeHash = treeBuilder.addDirectory("main");
        System.out.println("Tree hash created: " + mainTreeHash);

        //Display the contents of git/index
        System.out.println("\n--- git/index contents ---");
        File indexFile = new File(repository.gitFolder, "index");
        if (indexFile.exists()) {
            try {
                BufferedReader indexReader = new BufferedReader(new FileReader(indexFile));
                String line = indexReader.readLine();
                while (line != null) {
                    System.out.println(line);
                    line = indexReader.readLine();
                }
                indexReader.close();
            } catch (Exception e) {
                System.out.println("Error while reading index file:");
                e.printStackTrace();
            }
        } else {
            System.out.println("(Index file not found)");
        }

        //Check if all expected object files exist
        System.out.println("\nVerify created objects in git/objects");
        try {
            // Builder reads each line and adds a newline at the end
            String aText = "Apple\n";
            String bText = "Banana\n";
            String cText = "Cherry\n";

            String aBlobHash = Sha1Generator.generateSha1(Compression.compress(aText));
            String bBlobHash = Sha1Generator.generateSha1(Compression.compress(bText));
            String cBlobHash = Sha1Generator.generateSha1(Compression.compress(cText));

            File aBlobFile = new File(repository.gitFolder, "objects/" + aBlobHash);
            File bBlobFile = new File(repository.gitFolder, "objects/" + bBlobHash);
            File cBlobFile = new File(repository.gitFolder, "objects/" + cBlobHash);
            File treeObjectFile = new File(repository.gitFolder, "objects/" + mainTreeHash);

            System.out.println("a.txt blob exists: " + aBlobFile.exists() + " (" + aBlobHash + ")");
            System.out.println("b.txt blob exists: " + bBlobFile.exists() + " (" + bBlobHash + ")");
            System.out.println("c.txt blob exists: " + cBlobFile.exists() + " (" + cBlobHash + ")");
            System.out.println("Tree object exists: " + treeObjectFile.exists() + " (" + mainTreeHash + ")");
        } catch (Exception e) {
            System.out.println("Error while verifying object files:");
            e.printStackTrace();
        }

        //Cleanup (delete repo and files)
        System.out.println("\nCleaning up repository");
        boolean removedMainFolder = repository.removeDirectory("main");
        System.out.println("Removed main folder: " + removedMainFolder);
        boolean repoCleaned = repository.cleanup();
        System.out.println("Repository cleanup complete: " + repoCleaned);

        System.out.println("\n=== Test Finished ===");
    }
}

