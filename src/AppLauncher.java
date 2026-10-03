import java.io.IOException;
import java.nio.file.Path;

public class AppLauncher {

    public static void openVSCode(Path path) {

        try {

            new ProcessBuilder(
                    "code",
                    path.toAbsolutePath().toString()
            ).start();

            System.out.println("Opening VS Code...");

        } catch (IOException e) {

            System.out.println(
                    "Could not open VS Code. Make sure 'code' is in PATH."
            );
        }
    }

    public static void openTerminal(Path path) {

        String os = System.getProperty("os.name").toLowerCase();
        String absolutePath = path.toAbsolutePath().toString();

        try {

            if (os.contains("win")) {

                new ProcessBuilder(
                        "cmd.exe",
                        "/K",
                        "cd",
                        "/d",
                        absolutePath
                ).start();

            } else if (os.contains("mac")) {

                new ProcessBuilder(
                        "open",
                        "-a",
                        "Terminal",
                        absolutePath
                ).start();

            } else {

                try {

                    new ProcessBuilder(
                            "x-terminal-emulator",
                            "--working-directory=" + absolutePath
                    ).start();

                } catch (IOException e) {

                    new ProcessBuilder(
                            "gnome-terminal",
                            "--working-directory=" + absolutePath
                    ).start();
                }
            }

            System.out.println("Opening terminal...");

        } catch (IOException e) {

            System.out.println("Could not open terminal.");
        }
    }
}