import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class TestGitTreeWorking {

    public static void main(String[] args) {
        System.out.println("=== GitTreeStager Tester ===");
        runStagerDemo();
    }

    private static void runStagerDemo() {
        //Create repository
        MyRepo repository = new MyRepo("StageRepo", true);

        //Build sample directory tree under StageRepo
        // StageRepo/
        //   myProgram/
        //     README.md
        //     Hello.txt
        //     scripts/
        //       Cat.java
        File myProgramFolder = new File("StageRepo/myProgram");
        File scriptsFolder = new File("StageRepo/myProgram/scripts");
        myProgramFolder.mkdirs();
        scriptsFolder.mkdirs();

        File readmeFile = new File(myProgramFolder, "README.md");
        File helloFile = new File(myProgramFolder, "Hello.txt");
        File catFile = new File(scriptsFolder, "Cat.java");

        try {
            readmeFile.createNewFile();
            helloFile.createNewFile();
            catFile.createNewFile();

            BufferedWriter readmeWriter = new BufferedWriter(new FileWriter(readmeFile));
            readmeWriter.write("This is the readme for myProgram");
            readmeWriter.close();

            BufferedWriter helloWriter = new BufferedWriter(new FileWriter(helloFile));
            helloWriter.write("Hello from myProgram");
            helloWriter.close();

            BufferedWriter catWriter = new BufferedWriter(new FileWriter(catFile));
            catWriter.write("class Cat {}");
            catWriter.close();
        } 
        catch (Exception e) {
            System.out.println("Error while creating or writing initial files");
            e.printStackTrace();
            return;
        }

        // Stage myProgram directory
        GitTreeWorking stager = new GitTreeWorking(repository);
        String firstTreeHash = stager.buildTreeFromDirectory("myProgram");
        System.out.println("First tree hash created");
        System.out.println(firstTreeHash);

        //Show index contents
        System.out.println();
        System.out.println("--- git/index after first stage ---");
        printFile(new File(repository.gitFolder, "index"));

        //Show working tree file for myProgram
        System.out.println();
        System.out.println("--- working tree file for myProgram ---");
        // GitTreeWorking writes trees into git/trees using the path with slashes replaced by underscores
        File workingTreeFile = new File(repository.gitFolder, "trees/myProgram.tree");
        printFile(workingTreeFile);

        //Add a new file to show that tree hashes update
        File dogFile = new File(scriptsFolder, "Dog.java");
        try {
            dogFile.createNewFile();
            BufferedWriter dogWriter = new BufferedWriter(new FileWriter(dogFile));
            dogWriter.write("class Dog {}");
            dogWriter.close();
        } 
        catch (Exception e) {
            System.out.println("Error while creating Dog.java");
            e.printStackTrace();
        }

        //Stage again and compare tree hashes
        String secondTreeHash = stager.buildTreeFromDirectory("myProgram");
        System.out.println();
        System.out.println("Second tree hash after adding Dog.java");
        System.out.println(secondTreeHash);

        // Show updated index and working tree
        System.out.println();
        System.out.println("--- git/index after second stage ---");
        printFile(new File(repository.gitFolder, "index"));

        System.out.println();
        System.out.println("--- working tree file for myProgram after second stage ---");
        printFile(workingTreeFile);

        //Cleanup at the end
        System.out.println();
        System.out.println("Cleaning up repository");
        boolean removedMyProgram = repository.removeDirectory("myProgram");
        System.out.println("Removed myProgram folder: " + removedMyProgram);
        boolean cleaned = repository.cleanup();
        System.out.println("Repository cleanup complete: " + cleaned);

        System.out.println();
        System.out.println("=== Test complete ===");
    }

    private static void printFile(File file) {
        if (!file.exists()) {
            System.out.println("File does not exist: " + file.getPath());
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Error while reading file");
            e.printStackTrace();
        }
    }
}
