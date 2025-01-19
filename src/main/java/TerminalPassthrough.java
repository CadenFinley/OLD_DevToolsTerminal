
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TerminalPassthrough class provides methods to interact with the terminal.
 *
 * @version 1.0
 */
public class TerminalPassthrough {

    private final String BLUE_COLOR_BOLD = "\033[1;34m";
    private final String RED_COLOR_BOLD = "\033[1;31m";
    private final String YELLOW_COLOR_BOLD = "\033[1;33m";
    private final String RESET_COLOR = "\033[0m";
    private String currentDirectory;
    private final Map<String, String> terminalCache;
    private boolean displayWholePath = false;

    /**
     * Constructs a TerminalPassthrough with the current directory set to the
     * user's working directory.
     */
    public TerminalPassthrough() {
        currentDirectory = System.getProperty("user.dir");
        terminalCache = new HashMap<>();
    }

    /**
     * Gets the name of the terminal based on the operating system.
     *
     * @return the terminal name
     */
    public String getTerminalName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "cmd";
        } else if (os.contains("nix") || os.contains("nux")) {
            return "bash";
        } else {
            return "sh";
        }
    }

    /**
     * Sets whether to display the whole path in the terminal prompt.
     *
     * @param displayWholePath true to display the whole path, false otherwise
     */
    public void setDisplayWholePath(boolean displayWholePath) {
        this.displayWholePath = displayWholePath;
    }

    /**
     * Toggles the display of the whole path in the terminal prompt.
     */
    public void toggleDisplayWholePath() {
        setDisplayWholePath(!displayWholePath);
    }

    /**
     * Checks whether the whole path is displayed in the terminal prompt.
     *
     * @return true if the whole path is displayed, false otherwise
     */
    public boolean isDisplayWholePath() {
        return displayWholePath;
    }

    private String getCurrentFilePath() {
        return currentDirectory;
    }

    private String getCurrentFileName() {
        Path fileNamePath = Paths.get(getCurrentFilePath()).getFileName();
        return (fileNamePath != null && !fileNamePath.toString().equals(" ") && !fileNamePath.toString().strip().equals("")) ? fileNamePath.toString() : "/";
    }

    /**
     * Prints the current terminal position.
     */
    public void printCurrentTerminalPosition() {
        System.out.println(returnCurrentTerminalPosition());
    }

    /**
     * Gets the terminal cache.
     *
     * @return the terminal cache
     */
    public Map<String, String> getTerminalCache() {
        return terminalCache;
    }

    /**
     * Clears the terminal cache.
     */
    public void clearTerminalCache() {
        terminalCache.clear();
    }

    /**
     * Returns the current terminal position as a string.
     *
     * @return the current terminal position
     */
    public String returnCurrentTerminalPosition() {
        String gitInfo = "";
        Path currentPath = Paths.get(getCurrentFilePath());
        Path gitHeadPath = null;
        while (currentPath != null) {
            gitHeadPath = currentPath.resolve(".git").resolve("HEAD");
            if (Files.exists(gitHeadPath)) {
                break;
            }
            currentPath = currentPath.getParent();
        }
        boolean gitRepo = gitHeadPath != null && Files.exists(gitHeadPath);
        if (gitRepo) {
            try {
                List<String> headLines = Files.readAllLines(gitHeadPath);
                String branchName = "";
                Pattern headPattern = Pattern.compile("ref: refs/heads/(.*)");
                for (String line : headLines) {
                    Matcher headMatcher = headPattern.matcher(line);
                    if (headMatcher.find()) {
                        branchName = BLUE_COLOR_BOLD + "git:(" + RESET_COLOR + YELLOW_COLOR_BOLD + headMatcher.group(1) + RESET_COLOR + BLUE_COLOR_BOLD + ")" + RESET_COLOR;
                    }
                }
                String repoName;
                if (currentPath != null) {
                    if (displayWholePath) {
                        repoName = RED_COLOR_BOLD + getCurrentFilePath() + RESET_COLOR;
                    } else {
                        repoName = RED_COLOR_BOLD + getCurrentFileName() + RESET_COLOR;
                    }
                } else {
                    repoName = RED_COLOR_BOLD + "unknown" + RESET_COLOR;
                }
                if (!repoName.isEmpty() && !branchName.isEmpty()) {
                    gitInfo = String.format("%s %s", repoName, branchName);
                }
            } catch (IOException e) {
                System.out.println("Error reading git HEAD file: " + e.getMessage());
            }
            return (RED_COLOR_BOLD + getTerminalName() + ": " + RESET_COLOR + gitInfo + ": ");
        }
        if (displayWholePath) {
            return (RED_COLOR_BOLD + getTerminalName() + ": " + RESET_COLOR + YELLOW_COLOR_BOLD + getCurrentFilePath() + " " + RESET_COLOR);
        }
        return (RED_COLOR_BOLD + getTerminalName() + ": " + RESET_COLOR + YELLOW_COLOR_BOLD + getCurrentFileName() + " " + RESET_COLOR);
    }

    /**
     * Executes a command in the terminal.
     *
     * @param command the command to execute
     * @param feedback whether to provide feedback on the command execution
     * @return the thread executing the command
     */
    public Thread executeCommand(String command, boolean feedback) {
        Thread commandThread = new Thread(() -> {
            boolean executionPass = true;
            try {
                if (command.startsWith("cd ")) {
                    String newDir = command.substring(3).trim();
                    if (newDir.equals("/")) {
                        currentDirectory = new java.io.File("/").getCanonicalPath();
                    } else {
                        java.io.File dir = new java.io.File(currentDirectory, newDir);
                        if (dir.exists() && dir.isDirectory()) {
                            currentDirectory = dir.getCanonicalPath();
                        } else {
                            throw new IOException("No such file or directory");
                        }
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
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    process.waitFor();
                    terminalCache.put("User input: ", command);
                    terminalCache.put(getTerminalName() + " output: ", output.toString());
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
