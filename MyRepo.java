import java.io.File;
import java.io.IOException;

public class MyRepo {
    File repoFolder;
    File gitFolder;
    File readme;

    public MyRepo(String name, boolean shouldIncludeREADME) {
        // Make repo folder
        new File(name).mkdir();

        // Make hidden .git folder
        gitFolder = new File(name + "/.git");
        gitFolder.mkdir();

        // Make readme file if needed
        if (shouldIncludeREADME)
            readme = new File(name + "/README.md");
        try {
            readme.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
