
import java.io.IOException;

public class TerminalPassthrough {

    private final String RED_COLOR_BOLD = "\033[1;31m";
    private final String RESET_COLOR = "\033[0m";
    private String currentDirectory = System.getProperty("user.dir");

    public TerminalPassthrough() {
        //nothing rn
    }

    private String getTerminalName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "cmd.exe";
        } else if (os.contains("nix") || os.contains("nux")) {
            return "bash";
        } else {
            return "sh";
        }
    }

    private String getCurrentFilePath() {
        return currentDirectory;
    }

    public void printCurrentTerminalPosition() {
        System.out.println(RED_COLOR_BOLD + getCurrentFilePath() + " " + getTerminalName() + ": " + RESET_COLOR);
    }

    public String returnCurrentTerminalPosition() {
        return (RED_COLOR_BOLD + getCurrentFilePath() + " " + getTerminalName() + ": " + RESET_COLOR);
    }

    public Thread executeCommand(String command, boolean feedback) {
        Thread commandThread = new Thread(() -> {
            boolean executionPass = true;
            try {
                if (command.startsWith("cd ")) {
                    String newDir = command.substring(3).trim();
                    java.io.File dir = new java.io.File(currentDirectory, newDir);
                    if (dir.exists() && dir.isDirectory()) {
                        currentDirectory = dir.getCanonicalPath();
                    } else {
                        throw new IOException("No such file or directory");
                    }
                } else {
                    String os = System.getProperty("os.name").toLowerCase();
                    ProcessBuilder processBuilder;
                    if (os.contains("win")) {
                        processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
                    } else {
                        processBuilder = new ProcessBuilder(getTerminalName(), "-c", command);
                    }
                    processBuilder.directory(new java.io.File(currentDirectory));
                    processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
                    processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                    Process process = processBuilder.start();
                    process.waitFor();
                }
            } catch (IOException | InterruptedException e) {
                if (feedback) {
                    System.out.println("Error executing command: '" + command + "' " + e.getMessage());
                }
                executionPass = false;
            }
            if (feedback) {
                if (!executionPass) {
                    System.out.println("Command failed to execute: '" + command + "'");
                }
            }
        });
        commandThread.start();
        return commandThread;
    }
}
