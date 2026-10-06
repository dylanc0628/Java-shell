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

            while (running) {
                System.out.print("$ ");

                String userInput = scanner.nextLine();

                List<String> arguments = new ArrayList<>(Arrays.asList(userInput.split(" ")));
                
                switch (arguments.get(0).toLowerCase().strip()) {
                    case "exit" -> running = false;

                    case "echo" -> {
                        for (int i = 1; i < arguments.size(); i++) {
                            if (i == arguments.size() - 1) {
                                System.out.println(arguments.get(i));
                            } else {
                                System.out.print(arguments.get(i) + " ");
                            }
                        }
                    }

                    case "type" -> {
                        if (arguments.get(1).equals("echo") ||
                            arguments.get(1).equals("exit") ||
                            arguments.get(1).equals("type")) {
                                System.out.println(arguments.get(1) + " is a shell builtin");
                        } else {
                            String path = findPath(arguments.get(1));
                            if (path != null) {
                                System.out.println(arguments.get(1) + " is " + path);
                            } else {
                                System.out.println(arguments.get(1) + ": not found");
                            }
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