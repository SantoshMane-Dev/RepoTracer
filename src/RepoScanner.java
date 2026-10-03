import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RepoScanner {

    private final List<Repository> repositories = new ArrayList<>();

    public List<Repository> scan() {

        repositories.clear();

        for (Path root : FileSystems.getDefault().getRootDirectories()) {

            try {

                Files.walkFileTree(
                        root,
                        EnumSet.noneOf(FileVisitOption.class),
                        Integer.MAX_VALUE,
                        new FileVisitor<Path>() {

                            @Override
                            public FileVisitResult preVisitDirectory(
                                    Path dir,
                                    BasicFileAttributes attrs) {

                                String name = dir.getFileName() == null
                                        ? ""
                                        : dir.getFileName().toString();

                                if (shouldSkip(name)) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }

                                Path git = dir.resolve(".git");

                                if (Files.isDirectory(git)
                                        || Files.isRegularFile(git)) {

                                    String remote =
                                            GitHubDetector.getGitHubRemote(git);

                                    if (remote != null) {

                                        String repoName =
                                                dir.getFileName().toString();

                                        repositories.add(
                                                new Repository(
                                                        repoName,
                                                        dir,
                                                        remote
                                                )
                                        );

                                        return FileVisitResult.SKIP_SUBTREE;
                                    }
                                }

                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(
                                    Path file,
                                    BasicFileAttributes attrs) {

                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(
                                    Path file,
                                    IOException exc) {

                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(
                                    Path dir,
                                    IOException exc) {

                                return FileVisitResult.CONTINUE;
                            }
                        }
                );

            } catch (IOException ignored) {
            }
        }

        return repositories;
    }

    private boolean shouldSkip(String name) {

        String lower = name.toLowerCase();

        return lower.equals("bin")
                || lower.equals("node_modules")
                || lower.equals("target")
                || lower.equals("build")
                || lower.equals("dist")
                || lower.equals("out")
                || lower.equals(".gradle")
                || lower.equals(".m2")
                || lower.equals(".cache")
                || lower.equals("__pycache__")
                || lower.equals("$recycle.bin")
                || lower.equals("system volume information");
    }
}