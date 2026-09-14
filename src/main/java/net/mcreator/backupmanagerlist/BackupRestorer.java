package net.mcreator.backupmanagerlist;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BackupRestorer {

    public static boolean restoreBackup(String worldFolderId, Path zipFilePath) {
        File gameDir = Minecraft.getInstance().gameDirectory;
        Path savesDir = Paths.get(gameDir.getAbsolutePath(), "saves");
        Path worldTargetFolder = savesDir.resolve(worldFolderId);

        try {
            // 1. Purge existing world folder if it exists
            if (Files.exists(worldTargetFolder)) {
                deleteDirectory(worldTargetFolder);
            }

            Files.createDirectories(worldTargetFolder);

            // 2. Extract ZIP entries into the world folder
            unzip(zipFilePath, worldTargetFolder);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void deleteDirectory(Path path) throws IOException {
        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private static void unzip(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
            ZipEntry zipEntry = zis.getNextEntry();

            // Detect root folder inside ZIP if backup mod wrapped everything in a parent directory
            String prefixToRemove = detectRootPrefix(zipFile);

            while (zipEntry != null) {
                String entryName = zipEntry.getName();

                if (!prefixToRemove.isEmpty() && entryName.startsWith(prefixToRemove)) {
                    entryName = entryName.substring(prefixToRemove.length());
                }

                if (entryName.isEmpty()) {
                    zipEntry = zis.getNextEntry();
                    continue;
                }

                Path newPath = zipPathTraversalCheck(targetDir, entryName);

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null) {
                        Files.createDirectories(newPath.getParent());
                    }
                    
                    try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    // Zip Path Traversal Guard
    private static Path zipPathTraversalCheck(Path targetDir, String entryName) throws IOException {
        Path targetPath = targetDir.resolve(entryName).normalize();
        if (!targetPath.startsWith(targetDir.normalize())) {
            throw new IOException("Bad zip entry path traversal: " + entryName);
        }
        return targetPath;
    }

    // Normalizes paths in case the zip contains a root folder like "my_world/level.dat"
    private static String detectRootPrefix(Path zipFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
            ZipEntry entry;
            String firstFolder = null;
            boolean levelDatInRoot = false;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals("level.dat")) {
                    levelDatInRoot = true;
                    break;
                }
                if (firstFolder == null && name.contains("/")) {
                    firstFolder = name.substring(0, name.indexOf("/") + 1);
                }
            }

            if (!levelDatInRoot && firstFolder != null) {
                return firstFolder;
            }
        }
        return "";
    }
}