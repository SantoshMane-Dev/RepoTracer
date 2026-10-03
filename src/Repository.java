import java.nio.file.Path;

public class Repository {

    private final String name;
    private final Path path;
    private final String remoteUrl;

    public Repository(String name, Path path, String remoteUrl) {
        this.name = name;
        this.path = path;
        this.remoteUrl = remoteUrl;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }
}