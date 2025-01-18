
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

public class TerminalPassthrough {

    private final String RED_COLOR_BOLD = "\033[1;31m";
    private final String RESET_COLOR = "\033[0m";
    private String currentDirectory;
    private final Map<String, String> terminalCache;

    public TerminalPassthrough() {
        currentDirectory = System.getProperty("user.dir");
        terminalCache = new HashMap<>();
    }

    public String getTerminalName() {
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
        System.out.println(returnCurrentTerminalPosition());
    }

    public Map<String, String> getTerminalCache() {
        return terminalCache;
    }

    public void clearCache() {
        terminalCache.clear();
    }

    public String returnCurrentTerminalPosition() {
        String gitInfo = "";
        Path gitConfigPath = Paths.get(currentDirectory, ".git", "config");
        boolean gitRepo = Files.exists(gitConfigPath);
        if (gitRepo) {
            try {
                List<String> configLines = Files.readAllLines(gitConfigPath);
                String repoName = "";
                String branchName = "";
                Pattern repoPattern = Pattern.compile("url = .*/(.*)\\.git");
                Pattern branchPattern = Pattern.compile("\\[branch \"(.*)\"\\]");
                for (String line : configLines) {
                    Matcher repoMatcher = repoPattern.matcher(line);
                    Matcher branchMatcher = branchPattern.matcher(line);
                    if (repoMatcher.find()) {
                        repoName = repoMatcher.group(1);
                    }
                    if (branchMatcher.find()) {
                        branchName = branchMatcher.group(1);
                    }
                }
                if (!repoName.isEmpty() && !branchName.isEmpty()) {
                    gitInfo = String.format("%s (%s)", repoName, branchName);
                }
            } catch (IOException e) {
                // Handle exception if needed
            }
        }
        if (gitRepo) {
            return (RED_COLOR_BOLD + getTerminalName() + ": " + gitInfo + ": " + RESET_COLOR);
        }
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
