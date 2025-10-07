import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Milestone 3.1: Recursive tree generation and working list
 *
 * Reuses MyRepo helpers:
 *   - repository.createBlobFromFile(file)  -> blob sha1
 *   - repository.writeObject(text)         -> object sha1 (tree)
 */
public class GitTreeWorking {

    private MyRepo repository;

    public GitTreeWorking(MyRepo repository) {
        this.repository = repository;
        ensureTreesFolder();
    }


    public String buildTreeFromDirectory(String startingDirRelPath) {
        File startDir = new File(repository.repoFolder, startingDirRelPath);
        if (!startDir.exists() || !startDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + startingDirRelPath);
        }

        ArrayList<String> indexAdditions = new ArrayList<>();
        String treeHash = buildWorkingTree(startDir, startingDirRelPath, startingDirRelPath, indexAdditions);

        appendIndexBlobs(indexAdditions);
        return treeHash;
    }

    // Recursively build working tree; return this directory's tree sha1
    private String buildWorkingTree(File currentDir, String baseRelPath, String curRelPath, ArrayList<String> indexAdditions) {
        ArrayList<File> children = listChildrenSorted(currentDir);
        ArrayList<String> treeLines = new ArrayList<>();

        // Files first
        for (int i = 0; i < children.size(); i++) {
            File child = children.get(i);
            if (child.isFile()) {
                String relFromBase = pathRelativeToBase(baseRelPath, curRelPath + "/" + child.getName());
                String blobSha1 = repository.createBlobFromFile(child);
                String line = "blob " + blobSha1 + " " + relFromBase;
                treeLines.add(line);
                indexAdditions.add(line);
            }
        }

        // Directories next
        for (int i = 0; i < children.size(); i++) {
            File child = children.get(i);
            if (child.isDirectory()) {
                String childRelPath = curRelPath + "/" + child.getName();
                String childTreeSha1 = buildWorkingTree(child, baseRelPath, childRelPath, indexAdditions);
                String relFromBase = pathRelativeToBase(baseRelPath, childRelPath);
                String line = "tree " + childTreeSha1 + " " + relFromBase;
                treeLines.add(line);
            }
        }

        writeWorkingTreeFile(curRelPath, treeLines);

        String treeText = joinWithNewlines(treeLines);
        String treeSha1 = repository.writeObject(treeText);
        return treeSha1;
    }

    // Ensure git/trees folder exists
    private void ensureTreesFolder() {
        File treesDir = new File(repository.gitFolder, "trees");
        if (!treesDir.exists()) {
            treesDir.mkdir();
        }
    }

    
    private ArrayList<File> listChildrenSorted(File dir) {
        File[] arr = dir.listFiles();
        if (arr == null) arr = new File[0];
        ArrayList<File> list = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) list.add(arr[i]);

        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).getName().compareTo(list.get(j).getName()) > 0) {
                    File tmp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, tmp);
                }
            }
        }
        return list;
    }

    // Convert fullRelPath (repo-relative) to a path relative to baseRelPath
    private String pathRelativeToBase(String baseRelPath, String fullRelPath) {
        if (fullRelPath.startsWith(baseRelPath + "/")) {
            return fullRelPath.substring(baseRelPath.length() + 1);
        }
        if (fullRelPath.equals(baseRelPath)) {
            return "."; // marker for the base directory itself
        }
        return fullRelPath;
    }

    // Write git/trees/<dir>.tree file with both blob and tree lines
    private void writeWorkingTreeFile(String curRelPath, ArrayList<String> lines) {
        try {
            String safeName;
            if (curRelPath.length() == 0) {
                safeName = "root";
            } 
            else {
                safeName = curRelPath.replace('/', '_');
            }
            File workingTreeFile = new File(repository.gitFolder, "trees/" + safeName + ".tree");
            BufferedWriter writer = new BufferedWriter(new FileWriter(workingTreeFile));
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Error writing working tree file for " + curRelPath, e);
        }
    }

    // Append ONLY blob lines to git/index (no tree lines)
    private void appendIndexBlobs(ArrayList<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        try {
            File indexFile = new File(repository.gitFolder, "index");
            BufferedWriter indexWriter = new BufferedWriter(new FileWriter(indexFile, true));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("blob ")) {
                    indexWriter.write(line);
                    indexWriter.newLine();
                }
            }
            indexWriter.close();
        } catch (Exception e) {
            throw new RuntimeException("Error appending to index", e);
        }
    }

    private String joinWithNewlines(ArrayList<String> lines) {
        String joined = "";
        for (int i = 0; i < lines.size(); i++) {
            joined = joined + lines.get(i) + "\n";
        }
        return joined;
    }
}
