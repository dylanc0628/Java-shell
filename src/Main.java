package src;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            String currentDir = System.getProperty("user.dir");

            while (running) {
                System.out.print("$ ");
                boolean isBackSlash = false; //only true if iterating over an initial backslash
                boolean isDoubleQuotes = false;
                boolean isSingleQuotes = false;

                String userInput = scanner.nextLine();

                List<String> arguments = new ArrayList<>(Arrays.asList(userInput.split("(?<=\\s)|(?=\\s)")));
                
                switch (arguments.get(0).toLowerCase().strip()) {
                    case "exit" -> running = false;

                    case "echo" -> {
                        String line = "";
                        for (int i = 2; i < arguments.size(); i++) {
                            line += arguments.get(i);
                        }
                        
                        char[] charArray = line.toCharArray();
                        String printLine = "";
                        for (int i = 0; i < line.length(); i++) {
                            switch (charArray[i]) {
                                case '\\' -> {
                                    isBackSlash = !isBackSlash;
                                    if (i == 0) {}
                                    else if (i == line.length() - 1) {}

                                    if (isBackSlash && charArray[i + 1] != '\\') isBackSlash = !isBackSlash;
                                    else if (isBackSlash && charArray[i + 1] == '\\') {}
                                    else printLine += charArray[i];
                                }
                                case '\'' -> {
                                    isSingleQuotes = !isSingleQuotes;
                                    if (i == 0) {}
                                    else if (i == line.length() - 1 && charArray[i - 1] != '\\') {}
                                    else if (charArray[i - 1] == '\\') printLine += charArray[i];
                                    else if (charArray[0] == '\"') printLine += charArray[i];
                                }
                                case '\"' -> {
                                    isDoubleQuotes = !isDoubleQuotes;
                                    if (i == 0) {}
                                    else if (i == line.length() - 1) {}
                                    else if (charArray[i - 1] == '\\') printLine += charArray[i];
                                }
                                case ' ' -> {
                                    if (isDoubleQuotes) printLine += charArray[i];
                                    else if (isSingleQuotes) printLine += charArray[i];
                                    else if (Character.isLetterOrDigit(charArray[i - 1]) || charArray[i - 1] == '\"') printLine += charArray[i];
                                    else if (charArray[i - 1] == '\\') printLine += charArray[i];
                                    else {}
                                }
                                default -> printLine += charArray[i];
                            }
                        }
                        System.out.println(printLine);
                    }

                    case "type" -> {
                        if (arguments.get(1).equals("echo") ||
                            arguments.get(1).equals("exit") ||
                            arguments.get(1).equals("type") ||
                            arguments.get(1).equals("pwd")  ||
                            arguments.get(1).equals("cd")) {
                                System.out.println(arguments.get(1) + " is a shell builtin");
                        } 
                        else {
                            String path = findPath(arguments.get(1));
                            if (path != null) {
                                System.out.println(arguments.get(1) + " is " + path);
                            } else {
                                System.out.println(arguments.get(1) + ": not found");
                            }
                        }
                    }
                    case "pwd" -> System.out.println(currentDir);

                    case "cd" -> {
                        if (arguments.get(1).equals("~")) {
                            arguments.set(1, System.getenv("HOME"));
                        } else if (arguments.get(1).startsWith("~/")) {
                            arguments.set(1, System.getenv("HOME") + arguments.get(1).substring(1));
                        }
                        File file = new File(arguments.get(1));
                        if (!file.isAbsolute()) {
                            file = new File(currentDir, arguments.get(1));
                        }
                        try { 
                            if (file.isDirectory()) {
                                currentDir = file.getCanonicalPath();
                            } else {
                                System.out.println("cd: " + arguments.get(1) + ": No such file or directory");
                            }
                        } catch (IOException e) {
                            System.out.println(arguments.get(1) + " failed to execute");
                        }
                    }
                    default -> {
                        String path = findPath(arguments.get(0));
                        if (path != null) {
                            try {
                                ProcessBuilder pb = new ProcessBuilder(arguments);
                                pb.inheritIO();
                                pb.start().waitFor();
                            } catch (IOException e) {
                                System.out.println(arguments.get(0) + ": failed to execute");
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        } else {
                            System.out.println(arguments.get(0) + ": command not found");
                        }
                    }
                }
            }
        }
    }

    public static String findPath(String command) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return null;
        }

        for (String dir : pathEnv.split(File.pathSeparator)) {
            File file = new File(dir, command);
            if (file.isFile() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }
}