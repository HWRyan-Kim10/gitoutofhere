import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


/**
 * Builds tree objects for directories and updates the index with entries
 * Format of each tree line:
 *   blob <SHA1> <path>
 *   tree <SHA1> <path>
 *
 * Usage:
 *   MyRepo repo = new MyRepo("NewRepo", true);
 *   GitTreeBuilder builder = new GitTreeBuilder(repo);
 *   String treeHash = builder.addDirectory("myProgram"); // relative to repo root
 *   System.out.println("Tree hash: " + treeHash);
 */
public class GitTreeBuilder {

    private MyRepo repo;

    public GitTreeBuilder(MyRepo repo) {
        this.repo = repo;
    }

    /**
     * Adds a directory given a relative path from the repo root.
     * Creates blobs for files, tree objects for subdirectories, and writes
     * a tree object for the directory itself into git/objects under its SHA-1.
     * Also appends entries for every file and subdirectory to git/index.
     *
     * @param directoryPath relative path from repo root, for example "myProgram"
     * @return SHA-1 hash of the tree object representing the directory
     */
    public String addDirectory(String directoryPath) {
        File dir = new File(repo.repoFolder, directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Directory not found or not a directory: " + directoryPath);
        }

        ArrayList<String> indexLines = new ArrayList<>();
        String treeHash = buildTreeForDirectory(dir, directoryPath, indexLines);

        // Append all gathered index lines
        appendToIndex(indexLines);

        return treeHash;
    }

    // Recursively builds a tree for dir and returns the tree hash
    private String buildTreeForDirectory(File dir, String relPath, ArrayList<String> indexLines) {
        ArrayList<String> treeLines = new ArrayList<>();

        // List and sort children by name for deterministic ordering
        File[] children = dir.listFiles();
        if (children == null) {
            children = new File[0];
        }
        ArrayList<File> childList = new ArrayList<>();

    // Add each child manually to the list
    for (int i = 0; i < children.length; i++) {
        childList.add(children[i]);
    }

    // Sort the list alphabetically by name (this will make sure SHA1 hash stays consistent for same contents)
    for (int i = 0; i < childList.size() - 1; i++) {
        for (int j = i + 1; j < childList.size(); j++) {
            if (childList.get(i).getName().compareTo(childList.get(j).getName()) > 0) {
                File temp = childList.get(i);
                childList.set(i, childList.get(j));
                childList.set(j, temp);
            }
        }
    }


        // First handle files, then subdirectories
        for (File child : childList) {
            if (child.isFile()) {
                String fileRelPath = relPath + "/" + child.getName();
                String blobHash = createBlobForFile(child);
                String line = "blob " + blobHash + " " + fileRelPath;
                treeLines.add(line);
                indexLines.add(line);
            }
        }

        for (File child : childList) {
            if (child.isDirectory()) {
                String subRelPath = relPath + "/" + child.getName();
                String subTreeHash = buildTreeForDirectory(child, subRelPath, indexLines);
                String line = "tree " + subTreeHash + " " + subRelPath;
                treeLines.add(line);
                indexLines.add(line);
            }
        }

        String treeContent = "";
        for (int i = 0; i < treeLines.size(); i++) {
            treeContent = treeContent + treeLines.get(i) + "\n";
        }
        String treeHash = writeObject(treeContent);
        return treeHash;
    }

    // Creates a blob object for a file and returns its SHA-1
    private String createBlobForFile(File file) {
        try {
            // Read file in the same style as MyRepo.createBlobFile
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            String contents = sb.toString();
            String compressed = Compression.compress(contents);
            String sha1 = Sha1Generator.generateSha1(compressed);

            // Write compressed contents to objects/<sha1> if not already present
            File objFile = new File(repo.gitFolder, "objects/" + sha1);
            if (!objFile.exists()) {
                objFile.createNewFile();
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(objFile))) {
                    bw.write(compressed);
                }
            }

            return sha1;
        } catch (Exception e) {
            throw new RuntimeException("Error creating blob for file: " + file.getPath(), e);
        }
    }

    // Writes a tree or blob object into objects and returns its SHA-1
    // Input should be the plain text content of the object
    private String writeObject(String content) {
        try {
            String compressed = Compression.compress(content);
            String sha1 = Sha1Generator.generateSha1(compressed);

            File objFile = new File(repo.gitFolder, "objects/" + sha1);
            if (!objFile.exists()) {
                objFile.createNewFile();
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(objFile))) {
                    bw.write(compressed);
                }
            }

            return sha1;
        } catch (Exception e) {
            throw new RuntimeException("Error writing object", e);
        }
    }

    // Appends a batch of lines to git/index
    private void appendToIndex(ArrayList<String> lines) {
        if (lines.isEmpty()){
            return;
        } 
        File index = new File(repo.gitFolder, "index");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(index, StandardCharsets.UTF_8, true))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating index", e);
        }
    }
}

