package com.tools.localstackui.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CommandRunnerService {

  private static final Set<String> ALLOWED_COMMANDS = Set.of(
      "pwd", "ls", "mvn", "./mvnw", "gradle", "./gradlew", "docker", "kubectl", "curl",
      "java", "git");

  @Value("${command.runner.allowed.directories:/Users/s2151621/Desktop/GitTerm/local-stack-ui,/Users/s2151621/Desktop/GitTerm/spring6}")
  private String allowedDirectories;

  @Value("${command.runner.timeout.seconds:60}")
  private int timeoutSeconds;

  public List<String> getAllowedDirectories() {
    List<String> directories = new ArrayList<>();
    for (String value : allowedDirectories.split(",")) {
      String directory = value.trim();
      if (!directory.isEmpty()) {
        directories.add(directory);
      }
    }
    return directories;
  }

  public Map<String, String> runCommand(String cwd, String command)
      throws IOException, InterruptedException {
    Path workingDirectory = requireAllowedDirectory(cwd);
    List<String> tokens = tokenize(command);
    validateCommand(tokens, workingDirectory);
    ProcessBuilder processBuilder = new ProcessBuilder(tokens);
    processBuilder.directory(workingDirectory.toFile());
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    StringBuilder output = new StringBuilder();
    Thread outputReader = new Thread(() -> readOutput(process, output));
    outputReader.start();
    boolean completed = process.waitFor(Math.max(1, timeoutSeconds), TimeUnit.SECONDS);
    if (!completed) {
      process.destroyForcibly();
      outputReader.join(Duration.ofSeconds(1).toMillis());
      throw new IOException(command + " timed out after " + timeoutSeconds + " seconds");
    }
    outputReader.join(Duration.ofSeconds(1).toMillis());
    Map<String, String> response = new LinkedHashMap<>();
    response.put("exitCode", String.valueOf(process.exitValue()));
    response.put("output", output.toString().trim());
    response.put("command", String.join(" ", tokens));
    response.put("cwd", workingDirectory.toString());
    return response;
  }

  private Path requireAllowedDirectory(String cwd) throws IOException {
    if (cwd == null || cwd.isBlank()) {
      throw new IllegalArgumentException("Working directory is required.");
    }
    Path requestedDirectory = Path.of(cwd).toRealPath();
    for (String allowedDirectory : getAllowedDirectories()) {
      Path allowedPath = Path.of(allowedDirectory).toRealPath();
      if (requestedDirectory.startsWith(allowedPath)) {
        return requestedDirectory;
      }
    }
    throw new IllegalArgumentException("Working directory is not allowed.");
  }

  private void validateCommand(List<String> tokens, Path workingDirectory) {
    if (tokens.isEmpty()) {
      throw new IllegalArgumentException("Command is required.");
    }
    String executable = tokens.get(0);
    if (!ALLOWED_COMMANDS.contains(executable)) {
      throw new IllegalArgumentException("Command is not allowed: " + executable);
    }
    for (String token : tokens) {
      if (token.contains("&&") || token.contains("||") || token.contains(";")
          || token.contains("|") || token.contains(">") || token.contains("<")
          || token.contains("`") || token.contains("$(")) {
        throw new IllegalArgumentException("Shell operators are not allowed.");
      }
    }
    if ("./mvnw".equals(executable) || "./gradlew".equals(executable)) {
      Path wrapper = workingDirectory.resolve(executable.substring(2));
      if (!Files.exists(wrapper)) {
        throw new IllegalArgumentException(executable + " does not exist in the working directory.");
      }
    }
    validateSubcommand(tokens);
  }

  private void validateSubcommand(List<String> tokens) {
    if (tokens.size() < 2) {
      return;
    }
    String executable = tokens.get(0);
    String subcommand = tokens.get(1);
    if ("git".equals(executable)
        && !Set.of("status", "log", "branch", "diff").contains(subcommand)) {
      throw new IllegalArgumentException("Only git status/log/branch/diff are allowed.");
    }
    if ("docker".equals(executable)
        && Set.of("rm", "rmi", "prune", "system", "volume", "network").contains(subcommand)) {
      throw new IllegalArgumentException("Destructive docker subcommands are not allowed.");
    }
    if ("kubectl".equals(executable)
        && Set.of("delete", "drain", "cordon", "uncordon").contains(subcommand)) {
      throw new IllegalArgumentException("Destructive kubectl subcommands are not allowed.");
    }
  }

  private List<String> tokenize(String command) {
    if (command == null || command.trim().isEmpty()) {
      return List.of();
    }
    List<String> tokens = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    for (int i = 0; i < command.length(); i++) {
      char c = command.charAt(i);
      if (c == '\'' && !inDoubleQuote) {
        inSingleQuote = !inSingleQuote;
      } else if (c == '"' && !inSingleQuote) {
        inDoubleQuote = !inDoubleQuote;
      } else if (Character.isWhitespace(c) && !inSingleQuote && !inDoubleQuote) {
        addToken(tokens, current);
      } else {
        current.append(c);
      }
    }
    addToken(tokens, current);
    if (inSingleQuote || inDoubleQuote) {
      throw new IllegalArgumentException("Unclosed quote in command.");
    }
    return tokens;
  }

  private void addToken(List<String> tokens, StringBuilder current) {
    if (!current.isEmpty()) {
      tokens.add(current.toString());
      current.setLength(0);
    }
  }

  private void readOutput(Process process, StringBuilder output) {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    } catch (IOException e) {
      output.append(e.getMessage());
    }
  }
}
