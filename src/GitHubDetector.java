import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GitHubDetector {

    public static String getGitHubRemote(Path gitPath) {

        try {

            Path configFile;

            if (Files.isDirectory(gitPath)) {

                configFile = gitPath.resolve("config");

            } else {

                String content = Files.readString(gitPath).trim();

                if (!content.startsWith("gitdir:")) {
                    return null;
                }

                String gitDir = content.substring(7).trim();

                Path parent = gitPath.getParent();

                Path actualGitDir = parent.resolve(gitDir).normalize();

                configFile = actualGitDir.resolve("config");
            }

            if (!Files.exists(configFile)) {
                return null;
            }

            List<String> lines = Files.readAllLines(configFile);

            boolean insideRemote = false;

            for (String line : lines) {

                line = line.trim();

                if (line.startsWith("[remote ")) {
                    insideRemote = true;
                    continue;
                }

                if (line.startsWith("[") && !line.startsWith("[remote ")) {
                    insideRemote = false;
                }

                if (insideRemote && line.startsWith("url")) {

                    int equals = line.indexOf('=');

                    if (equals == -1) {
                        continue;
                    }

                    String url = line.substring(equals + 1).trim();

                    if (url.toLowerCase().contains("github.com")) {
                        return url;
                    }
                }
            }

        } catch (Exception e) {
            return null;
        }

        return null;
    }
}