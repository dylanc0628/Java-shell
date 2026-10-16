package src;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//TODO: Redirection next
//TODO: Add testing
//TODO: Split Main into separate classes and a command interface

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            String currentDir = System.getProperty("user.dir");

            while (running) {
                System.out.print("$ ");

                List<String> arguments = tokenize(scanner.nextLine());
                //System.err.println(arguments);
                
                switch (arguments.get(0)) {
                    case "exit" -> running = false;

                    case "echo" -> System.out.println(String.join(" ", arguments.subList(1, arguments.size())));

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
                                pb.directory(new File(currentDir));
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

    public static List<String> tokenize(String input) {
        List<String> args = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inToken = false, inSingle = false, inDouble = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inSingle) {
                if (c == '\'') inSingle = false;
                else cur.append(c);
            }
            else if (inDouble) {
                if (c == '\"') inDouble = false;
                else if (c == '\\' && i + 1 < input.length() && "\"\\$`".indexOf(input.charAt(i + 1)) >= 0) {
                    cur.append(input.charAt(++i));
                }
                else cur.append(c);
            }
            else if (c == '\\') {
                if (i + 1 < input.length()) cur.append(input.charAt(++i));
                inToken = true;
            }
            else if (c == '\'') {
                inSingle = true;
                inToken = true;
            }
            else if (c == '\"') {
                inDouble = true;
                inToken = true;
            }
            else if (Character.isWhitespace(c)) {
                if (inToken) {
                    args.add(cur.toString());
                    cur.setLength(0);
                    inToken = false;
                }
            } 
            else {
                cur.append(c);
                inToken = true;
            }
        }
        if (inToken) args.add(cur.toString());
        return args;
    }
}
