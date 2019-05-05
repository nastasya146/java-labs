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
        if (args.length != 2 || args[0]==null || args[1]==null || args[0].equals("")  || args[1].equals("")) return;
        try {
            outputPath = new File(args[1]).toPath();
            boolean created = outputPath.toFile().exists();
            if (!created) {
                try {
                    created = outputPath.toFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            created &= outputPath.toFile().canWrite();
            created &= outputPath.toFile().isFile();
            File input = new File(args[0]);
            if (input.exists() && input.canRead() && input.isFile() && created) {
                try {
                    Files.readAllLines(input.toPath()).forEach(RecursiveWalk::walkFileTree);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (InvalidPathException e) {
            e.getMessage();
        }
    }

    public static void walkFileTree(String path) {
        File mainDir = new File(path);
        if (mainDir.isDirectory() && mainDir.listFiles()!=null) {
            for (File file: mainDir.listFiles()) {
                if (file.isDirectory()) {
                    RecursiveWalk.walkFileTree(file.toPath().toString());
                } else {
                    try {
                        if (!outputPath.toFile().exists()) outputPath.toFile().createNewFile();
                        Files.write(outputPath, Collections.singleton(String.format("%08x", RecursiveWalk.getHash(file.toPath())) + " " + file.toPath()), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else if (mainDir.isFile()) {
            try {
                if (!outputPath.toFile().exists()) outputPath.toFile().createNewFile();
                Files.write(outputPath, Collections.singleton(String.format("%08x", getHash(mainDir.toPath())) + " " + mainDir.toPath()), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                if (!outputPath.toFile().exists()) outputPath.toFile().createNewFile();
                Files.write(outputPath, Collections.singleton(String.format("%08x", 0) + " " + path), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Integer getHash(Path path) {
        try {
            if (!path.toFile().exists() || !path.toFile().canRead()) return 0;
            return hash(Files.readAllBytes(path));
        } catch (IOException e) {
            return 0;
        }
    }


    private static int hash(final byte[] bytes) {
        int h = 0x811c9dc5;
        for (final byte b : bytes) {
            h = (h * 0x01000193) ^ (b & 0xff);
        }
        return h;
    }
}