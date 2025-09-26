import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class MyRepo {
    File repoFolder;
    File gitFolder;
    File readme;

    /**
     * Constructor for creating a new git repository
     * 
     * @param name                The name of the repository folder to create
     * @param shouldIncludeREADME Whether to create a README.md file in the
     *                            repository
     */
    public MyRepo(String name, boolean shouldIncludeREADME) {
        // Make repo folder
        repoFolder = new File(name);
        if (!doesRepoExist()) {
            repoFolder.mkdir();
            // Make readme file if needed
            if (shouldIncludeREADME) {
                readme = new File(name + "/README.md");

                try {
                    readme.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // Setup the git folder
            try {
                setupGitFolder(name);
                System.out.println("Git Repository Created");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("Git Repository Already Exists");
        }

    }

    /**
     * Check if the git repository exists by verifying the git folder structure
     * 
     * @return true if the repository exists with proper git structure, otherwise
     *         false
     */
    private boolean doesRepoExist() {
        if (!repoFolder.exists()) {
            return false;
        }
        boolean gitFolderExists = false;
        File[] dirFiles = repoFolder.listFiles();
        for (File file : dirFiles) {
            if (file.getName().equals("git")) {
                gitFolderExists = true;
            }
        }
        boolean objectsDirectoryExists = false;
        boolean HEADExists = false;
        boolean indexExists = false;
        if (gitFolderExists) {
            for (File file : new File(repoFolder.getPath() + "/git").listFiles()) {
                if (file.getName().equals("objects")) {
                    objectsDirectoryExists = true;
                }
                if (file.getName().equals("HEAD")) {
                    HEADExists = true;
                }
                if (file.getName().equals("index")) {
                    indexExists = true;
                }
            }
        }
        return indexExists && HEADExists && objectsDirectoryExists;
    }

    /**
     * Deletes all files and folders in the repo, then deletes the repo folder
     * 
     * @return true if cleanup was successful, otherwise false
     */
    public boolean cleanup() {
        File[] repoContents = repoFolder.listFiles();
        for (File file : repoContents) {
            if (!file.isDirectory()) {
                file.delete();
            } else {
                removeDirectory(file.getName(), repoFolder.getPath());
            }
        }
        return repoFolder.delete();
    }

    /**
     * Sets up the .git folder structure with index, HEAD, and objects folder
     * 
     * @param repoName The name of the repository folder
     * @throws IOException if files cannot be created
     */
    private void setupGitFolder(String repoName) throws IOException {
        // Make hidden .git folder
        gitFolder = new File(repoName + "/git");
        gitFolder.mkdir();

        File indexFile = new File(gitFolder.getPath() + "/index");
        File objectsFolder = new File(gitFolder.getPath() + "/objects/");
        File HEAD = new File(gitFolder.getPath() + "/HEAD/");

        // make index file
        indexFile.createNewFile();
        HEAD.createNewFile();
        // make objects directory
        objectsFolder.mkdir();
    }

    /**
     * Main function to search this repo to see if a file exists by name
     * 
     * @param fileName The name of the file to search for
     * @return true if the file exists in the repository, otherwise false
     */
    public boolean includesFile(String fileName) {
        File[] files = repoFolder.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                if (file.getName().equals(fileName)) {
                    return true;
                }
            } else {
                if (includesFile(fileName, file.getPath())) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Recursive helper method to check subdirectories for files by name
     * 
     * @param fileName The name of the file to search for
     * @param filePath The path of the directory to search in
     * @return true if the file exists in the specified directory or its
     *         subdirectories, otherwise false
     */
    public boolean includesFile(String fileName, String filePath) {
        File[] files = new File(filePath).listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                if (file.getName().equals(fileName)) {
                    return true;
                }
            } else {
                if (includesFile(fileName, file.getPath()))
                    return true;
            }

        }
        return false;
    }

    /**
     * Main function to find the path of a file in the repo
     * 
     * @param fileName The name of the file to find
     * @return The full path to the file if found, null if not found
     */
    public String findFile(String fileName) {
        File[] files = repoFolder.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                if (file.getName().equals(fileName)) {
                    return file.getPath();
                }
            } else {
                String result = findFile(fileName, file.getPath());
                if (result != null) {
                    return result;
                }
            }

        }
        return null;
    }

    /**
     * Recursive helper method to find the path of a file in a subdirectory of the
     * repo
     * 
     * @param fileName The name of the file to find
     * @param filePath The path of the directory to search in
     * @return The full path to the file if found, null if not found
     */
    public String findFile(String fileName, String filePath) {
        File[] files = new File(filePath).listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                if (file.getName().equals(fileName)) {
                    return file.getPath();
                }
            } else {
                String result = findFile(fileName, file.getPath());
                if (result != null) {
                    return result;
                }
            }

        }
        return null;
    }

    /**
     * Removes the file in the repo
     * 
     * @param fileName The name of the file to remove
     * @return true if the file was successfully deleted, otherwise false
     */
    public boolean removeFile(String fileName) {
        File fileToRemove = new File(findFile(fileName));
        return fileToRemove.delete();
    }

    /**
     * Removes the directory in the repo and all files in it
     * 
     * @param directoryName The name of the directory to remove
     * @param path          The path where the directory is located
     * @return true if the directory was successfully deleted, otherwise false
     */
    public boolean removeDirectory(String directoryName, String path) {
        File directoryToRemove = new File(path + "/" + directoryName);
        File[] directoryContents = directoryToRemove.listFiles();
        for (File file : directoryContents) {
            if (!file.isDirectory()) {
                file.delete();
            } else {
                removeDirectory(file.getName(), file.getParent());
            }
        }
        try {
            return directoryToRemove.delete();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;

    }

    /**
     * Creates a compressed blob file from the specified file and stores it in the
     * objects folder
     * 
     * @param fileNameString The name of the file to create a blob from
     * @return true if the blob was successfully created, otherwise false
     */
    public boolean createBlobFile(String fileNameString) {
        File file = new File(findFile(fileNameString));

        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));

            StringBuilder sb = new StringBuilder();
            while (br.ready()) {
                sb.append(br.readLine());
                sb.append("\n");
            }
            br.close();
            String fileContents = sb.toString();
            fileContents = Compression.compress(fileContents);
            String sha1 = Sha1Generator.generateSha1(fileContents);

            File blobFile = new File(gitFolder.getPath() + "/objects/" + sha1);
            blobFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(blobFile));
            bw.write(fileContents);
            bw.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;

    }

    /**
     * Adds a file entry to the index with its blob hash
     * 
     * @param blobHashString The SHA-1 hash of the blob file
     * @param fileNameString The name of the file to add to the index
     * @return true if the file was successfully added to the index, otherwise false
     */
    public boolean addFileToIndex(String blobHashString, String fileNameString) {
        File file = new File(findFile(fileNameString));
        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(gitFolder.getPath() + "/index", true));
            bw.write(blobHashString + " " + findFile(fileNameString));
            bw.newLine();
            bw.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method to remove directory in the repo by name only
     * 
     * @param directoryName The name of the directory to remove from the repository
     * @return true if the directory was successfully deleted,otherwise false
     */
    public boolean removeDirectory(String directoryName) {
        return removeDirectory(directoryName, repoFolder.getPath());
    }

    public static void cleanupLocalFiles() {
        File[] files = new File(".").listFiles();
        for (File file : files) {
            if (file.isDirectory() && new File(file.getPath() + "/git").exists()) {
                MyRepo repo = new MyRepo(file.getName(), false);
                repo.cleanup();
            } else if (!file.isDirectory() && !file.getName().endsWith(".java") && !file.getName().equals(".gitignore")
                    && !file.getName().equals("README.md") && !file.getName().equals("LICENSE.md")) {
                file.delete();
            }
        }
    }

}
