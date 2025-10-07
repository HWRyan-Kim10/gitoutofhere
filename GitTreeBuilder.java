import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Builds tree objects for directories and updates the index with entries.
 * Lines use:
 *   blob <SHA1> <path>
 *   tree <SHA1> <path>
 *
 * Usage:
 *   MyRepo repository = new MyRepo("NewRepo", true);
 *   GitTreeBuilder builder = new GitTreeBuilder(repository);
 *   String treeHash = builder.addDirectory("myProgram");
 *   System.out.println("Tree hash: " + treeHash);
 */
public class GitTreeBuilder {

    private MyRepo repository;

    public GitTreeBuilder(MyRepo repository) {
        this.repository = repository;
    }

    /**
     * Adds a directory given a relative path from the repo root.
     * Creates blobs for files, tree objects for subdirectories, and writes
     * a tree object for this directory into git/objects under its SHA-1.
     * Also appends entries for every file and subdirectory to git/index.
     */
    public String addDirectory(String directoryPath) {
        File dir = new File(repository.repoFolder, directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Directory not found or not a directory: " + directoryPath);
        }

        ArrayList<String> indexLines = new ArrayList<>();
        String treeHash = buildTreeForDirectory(dir, directoryPath, indexLines);

        appendToIndex(indexLines);
        return treeHash;
    }

    // Recursively builds a tree for dir and returns the tree hash
    private String buildTreeForDirectory(File dir, String relPath, ArrayList<String> indexLines) {
        ArrayList<String> treeLines = new ArrayList<>();

        // List and sort children by name (beginner bubble sort)
        File[] childrenArray = dir.listFiles();
        if (childrenArray == null) {
            childrenArray = new File[0];
        }
        ArrayList<File> children = new ArrayList<>();
        for (int i = 0; i < childrenArray.length; i++) {
            children.add(childrenArray[i]);
        }
        for (int i = 0; i < children.size() - 1; i++) {
            for (int j = i + 1; j < children.size(); j++) {
                if (children.get(i).getName().compareTo(children.get(j).getName()) > 0) {
                    File temp = children.get(i);
                    children.set(i, children.get(j));
                    children.set(j, temp);
                }
            }
        }

        // Files first
        for (int i = 0; i < children.size(); i++) {
            File child = children.get(i);
            if (child.isFile()) {
                String fileRelPath = relPath + "/" + child.getName();
                String blobHash = repository.createBlobFromFile(child); // reuse helper
                String line = "blob " + blobHash + " " + fileRelPath;
                treeLines.add(line);
                indexLines.add(line);
            }
        }

        // Directories next
        for (int i = 0; i < children.size(); i++) {
            File child = children.get(i);
            if (child.isDirectory()) {
                String subRelPath = relPath + "/" + child.getName();
                String subTreeHash = buildTreeForDirectory(child, subRelPath, indexLines);
                String line = "tree " + subTreeHash + " " + subRelPath;
                treeLines.add(line);
            }
        }

        // Join lines with newlines
        String treeContent = "";
        for (int i = 0; i < treeLines.size(); i++) {
            treeContent = treeContent + treeLines.get(i) + "\n";
        }

        // Write the tree object using MyRepo helper
        String treeHash = repository.writeObject(treeContent);
        return treeHash;
    }

    // Appends a batch of lines to git/index
    private void appendToIndex(ArrayList<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        File indexFile = new File(repository.gitFolder, "index");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile, true));
            for (int i = 0; i < lines.size(); i++) {
                bw.write(lines.get(i));
                bw.newLine();
            }
            bw.close();
        } catch (Exception e) {
            throw new RuntimeException("Error updating index", e);
        }
    }
}
