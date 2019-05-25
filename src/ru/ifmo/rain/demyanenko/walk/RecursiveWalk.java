package ru.ifmo.rain.demyanenko.walk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class RecursiveWalk {

    private static Path outputPath;

    public static void main(String[] args) {
        if (args.length != 2 || args[0] == null || args[1] == null || args[0].equals("") || args[1].equals("")) {
            System.out.println("Invalid arguments. Must be 2 not null or empty arguments.");
            return;
        }
        try {
            outputPath = new File(args[1]).toPath();
            boolean createdOutput = checkOutputFile(outputPath);

            File input = new File(args[0]);
            if (input.exists() && input.canRead() && input.isFile() && createdOutput) {
                try {
                    Files.readAllLines(input.toPath()).forEach(RecursiveWalk::walkFileTree);
                } catch (IOException e) {
                    System.out.println("Failed to read input file " + args[0]);
                }
            }
        } catch (InvalidPathException e) {
            System.out.println("Invalid path: " + e.getMessage());
        }
    }

    public static boolean checkOutputFile(Path path) throws InvalidPathException {
        boolean created = outputPath.toFile().exists();
        if (!created) {
            try {
                created = outputPath.toFile().createNewFile();
            } catch (IOException e) { }
        }
        created &= outputPath.toFile().canWrite();
        created &= outputPath.toFile().isFile();

        if (!created) {
            System.out.println("Failed to create output file " + path);
        }
        return created;
    }

    public static void walkFileTree(String path) {
        File mainDir = new File(path);
        if (mainDir.isDirectory() && mainDir.listFiles() != null) {
            for (File file : mainDir.listFiles()) {
                if (file.isDirectory()) {
                    RecursiveWalk.walkFileTree(file.toPath().toString());
                } else {
                    int hashValue = RecursiveWalk.getHash(file.toPath());
                    writeHashToOutput(hashValue, file.toPath().toString());
                }
            }
        } else if (mainDir.isFile()) {
            int hashValue = RecursiveWalk.getHash(mainDir.toPath());
            writeHashToOutput(hashValue, mainDir.toPath().toString());          
        } else {
            writeHashToOutput(0, path);
        }
    }

    public static void writeHashToOutput(int hash, String path) {
        try {
            Files.write(outputPath, 
                Collections.singleton(String.format("%08x", hash) + " " + path),
                StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Failed to write data to output file " + outputPath);
        }
    }

    public static Integer getHash(Path path) {
        int hashValue = 0;
        try {
            if (path.toFile().exists() && path.toFile().canRead()) {
                hashValue = hash(Files.readAllBytes(path));
            }
        } catch (IOException e) {
            System.out.println("Error on reading file: " + path);
        }
        return hashValue;
    }

    private static int hash(final byte[] bytes) {
        int h = 0x811c9dc5;
        for (final byte b : bytes) {
            h = (h * 0x01000193) ^ (b & 0xff);
        }
        return h;
    }
}