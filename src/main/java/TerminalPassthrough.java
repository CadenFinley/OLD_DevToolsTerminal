
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

    public void executeCommand(String command, boolean feedback) {
        boolean executionPass;
        boolean outputted = false;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.directory(new java.io.File(currentDirectory));
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                outputted = true;
            }
            if (!outputted) {
                System.out.println();
            }
            process.waitFor();
            executionPass = true;
            // Update current directory if the command is 'cd'
            if (command.startsWith("cd ")) {
                String[] commandParts = command.split(" ");
                if (commandParts.length > 1) {
                    String newPath = commandParts[1];
                    java.io.File newDir = new java.io.File(currentDirectory, newPath);
                    if (newDir.exists() && newDir.isDirectory()) {
                        currentDirectory = newDir.getCanonicalPath();
                    } else {
                        System.out.println("Directory not found: " + newPath);
                    }
                }
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
    }
}
