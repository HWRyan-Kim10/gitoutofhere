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

    // Main function to search this repo to see if a file exists by name
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

    // Recursive helper method to check subdirectories for filles by name
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

    // Main function to find the path of a file in the repo
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

    // Recursive helper method to find the path of a file in a subdirectory of the
    // repo
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

    // Removes the file in the repo
    public boolean removeFile(String fileName) {
        File fileToRemove = new File(findFile(fileName));
        return fileToRemove.delete();
    }

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

    public boolean removeDirectory(String directoryName) {
        return removeDirectory(directoryName, repoFolder.getPath());
    }

}
