import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static final String BLUE = "\033[44m\033[97m";
    private static final String RESET = "\033[0m";
    private static final String CYAN = "\033[36m";
    private static final String DIM = "\033[2m";
    private static final String CLEAR = "\033[H\033[2J";

    public static void main(String[] args) {

        RepoScanner scanner = new RepoScanner();

        while (true) {

            System.out.print("\nRepoTracer Activated.>>>> ");

            String command = readLine();

            if (command.equalsIgnoreCase("exit")) {
                break;
            }

            if (!command.equalsIgnoreCase("repo")) {
                System.out.println("Commands: repo, exit");
                continue;
            }

            System.out.println("\nScanning your system for GitHub repositories...");

            List<Repository> repositories = scanner.scan();

            if (repositories.isEmpty()) {
                System.out.println("\nNo GitHub repositories found.");
                continue;
            }

            // Group repositories by their parent folder
            Map<Path, List<Repository>> groups = new LinkedHashMap<>();

            for (Repository repo : repositories) {

                Path parent = repo.getPath().getParent();

                groups
                        .computeIfAbsent(parent, key -> new ArrayList<>())
                        .add(repo);
            }

            // Select parent folder
            Path selectedParent;

            if (groups.size() == 1) {

                selectedParent = groups.keySet().iterator().next();

            } else {

                List<Path> parents = new ArrayList<>(groups.keySet());

                selectedParent = selectParent(parents);

                if (selectedParent == null) {
                    continue;
                }
            }

            // Select repository inside parent
            Repository selectedRepository =
                    selectRepository(groups.get(selectedParent));

            if (selectedRepository == null) {
                continue;
            }

            // Select action
            String action = selectAction();

            if (action == null) {
                continue;
            }

            if (action.equals("VS Code")) {

                AppLauncher.openVSCode(
                        selectedRepository.getPath()
                );

            } else if (action.equals("Terminal")) {

                AppLauncher.openTerminal(
                        selectedRepository.getPath()
                );
            }
        }
    }

    private static Path selectParent(List<Path> parents) {

        List<String> names = new ArrayList<>();

        for (Path parent : parents) {
            names.add(parent.getFileName() != null
                    ? parent.getFileName().toString()
                    : parent.toString());
        }

        int selected = select(
                "SELECT PROJECT FOLDER",
                names,
                "↑ ↓ Navigate    Enter Select    Esc Back"
        );

        if (selected == -1) {
            return null;
        }

        return parents.get(selected);
    }

    private static Repository selectRepository(
            List<Repository> repositories) {

        List<String> names = new ArrayList<>();

        for (Repository repo : repositories) {
            names.add(repo.getName());
        }

        int selected = select(
                "SELECT REPOSITORY",
                names,
                "↑ ↓ Navigate    Enter Select    Esc Back"
        );

        if (selected == -1) {
            return null;
        }

        return repositories.get(selected);
    }

    private static String selectAction() {

        List<String> actions = List.of(
                "Open in VS Code",
                "Open in Terminal"
        );

        int selected = select(
                "OPEN REPOSITORY",
                actions,
                "↑ ↓ Navigate    Enter Select    Esc Back"
        );

        if (selected == -1) {
            return null;
        }

        return actions.get(selected);
    }

    private static int select(
            String title,
            List<String> items,
            String help) {

        int selected = 0;

        try {

            enableRawMode();

            while (true) {

                System.out.print(CLEAR);

                System.out.println();
                System.out.println("  " + CYAN + title + RESET);
                System.out.println("  " + "─".repeat(45));
                System.out.println();

                for (int i = 0; i < items.size(); i++) {

                    if (i == selected) {

                        System.out.println(
                                "  " +
                                BLUE +
                                "  › " +
                                items.get(i) +
                                "  " +
                                RESET
                        );

                    } else {

                        System.out.println(
                                "      " + items.get(i)
                        );
                    }
                }

                System.out.println();
                System.out.println("  " + "─".repeat(45));
                System.out.println("  " + DIM + help + RESET);
                System.out.flush();

                int key = System.in.read();

                // Arrow keys
                if (key == 27) {

                    int second = System.in.read();

                    // ESC by itself
                    if (second == -1) {
                        return -1;
                    }

                    // Arrow sequence: ESC [ A/B
                    if (second == 91) {

                        int third = System.in.read();

                        // Up
                        if (third == 65) {

                            selected--;

                            if (selected < 0) {
                                selected = items.size() - 1;
                            }
                        }

                        // Down
                        else if (third == 66) {

                            selected++;

                            if (selected >= items.size()) {
                                selected = 0;
                            }
                        }

                    } else {
                        // Escape
                        return -1;
                    }

                }

                // Enter
                else if (key == 10 || key == 13) {

                    return selected;
                }
            }

        } catch (Exception e) {

            return -1;

        } finally {

            disableRawMode();
        }
    }

    private static void enableRawMode() throws Exception {

        new ProcessBuilder(
                "sh",
                "-c",
                "stty -icanon min 1 -echo < /dev/tty"
        ).inheritIO().start().waitFor();
    }

    private static void disableRawMode() {

        try {

            new ProcessBuilder(
                    "sh",
                    "-c",
                    "stty sane < /dev/tty"
            ).inheritIO().start().waitFor();

        } catch (Exception ignored) {
        }
    }

    private static String readLine() {

        try {

            byte[] buffer = new byte[1024];
            int length = System.in.read(buffer);

            if (length <= 0) {
                return "";
            }

            return new String(buffer, 0, length).trim();

        } catch (Exception e) {

            return "";
        }
    }
}