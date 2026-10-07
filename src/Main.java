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
                boolean check = false;
                System.out.print("$ ");

                String userInput = scanner.nextLine();

                List<String> arguments = new ArrayList<>(Arrays.asList(userInput.split(" ")));
                
                switch (arguments.get(0).toLowerCase().strip()) {
                    case "exit" -> running = false;

                    case "echo" -> {
                        String line = "";
                        for (int i = 1; i < arguments.size(); i++) {
                            line += arguments.get(i);
                        }

                        if (line.contains("\'") && !line.contains("\"")) {
                            String printLine = "";
                            for (int i = 1; i < arguments.size(); i++) {
                                if (arguments.get(i).startsWith("\'")) {
                                    printLine += arguments.get(i).replace("\'", "");
                                }
                                else if (arguments.get(i).endsWith("\'")) {
                                    printLine += " " + arguments.get(i).replace("\'", "");
                                } 
                                else {
                                    if (arguments.get(i).equals("")) {
                                        arguments.set(i, " ");
                                    }
                                    printLine += arguments.get(i);
                                }
                            }
                            System.out.println(printLine);
                        } 
                        else if (line.contains("\"")) {
                            String printLine = "";
                            for (int i = 1; i < arguments.size(); i++) {
                                if (arguments.get(i).startsWith("\"") && arguments.get(i).endsWith("\"") && i < arguments.size() - 1) {
                                    printLine += arguments.get(i).replace("\"", "") + " ";
                                    check = true;
                                }
                                else if (arguments.get(i).startsWith("\"")) {
                                    printLine += arguments.get(i).replace("\"", "");
                                }
                                else if (arguments.get(i).endsWith("\"")) {
                                    printLine += " " + arguments.get(i).replace("\"", "");
                                }
                                else if (arguments.get(i).equals("") && !check) {
                                    arguments.set(i, " ");
                                    printLine += arguments.get(i);
                                }
                                else {
                                    printLine += arguments.get(i);
                                }
                            }
                            System.out.println(printLine);
                        }
                        else {
                            for (int i = 1; i < arguments.size(); i++) {
                                if (arguments.get(i).equals("")) {
                                    continue;
                                }
                                if (i == arguments.size() - 1) {
                                    System.out.println(arguments.get(i).trim());
                                } 
                                else {
                                    System.out.print(arguments.get(i).trim() + " ");
                                }    
                            }
                        }
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