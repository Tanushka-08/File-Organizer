import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileOrganizer {

    private String folderPath;
    private File folder;

    private Map<String, List<String>> categories = new LinkedHashMap<>();
    private Map<String, Integer> categoryStats = new LinkedHashMap<>();

    private StringBuilder outputLog = new StringBuilder();
    private int totalFilesProcessed = 0;

    private int movedCount = 0;
    private int skippedCount = 0;
    private int failedCount = 0;

    private final String UNDO_FILE_NAME = "undo_log.txt";

    public FileOrganizer(String folderPath) {
        this.folderPath = folderPath;
        this.folder = new File(folderPath);

        initializeCategories();
        initializeStats();
    }

    // ---------------------------
    // INITIAL SETUP
    // ---------------------------

    private void initializeCategories() {
        categories.put("Images", Arrays.asList(
                ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".webp", ".ico"
        ));

        categories.put("Documents", Arrays.asList(
                ".pdf", ".doc", ".docx", ".txt", ".ppt", ".pptx", ".xls", ".xlsx",
                ".odt", ".rtf", ".csv"
        ));

        categories.put("Videos", Arrays.asList(
                ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v"
        ));

        categories.put("Music", Arrays.asList(
                ".mp3", ".wav", ".flac", ".aac", ".ogg", ".m4a"
        ));

        categories.put("Archives", Arrays.asList(
                ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2"
        ));

        categories.put("Programs", Arrays.asList(
                ".exe", ".msi", ".sh", ".bat", ".app", ".apk", ".jar"
        ));

        categories.put("Code", Arrays.asList(
                ".java", ".py", ".cpp", ".c", ".css", ".js", ".html",
                ".php", ".json", ".xml", ".sql", ".ts"
        ));
    }

    private void initializeStats() {
        categoryStats.clear();
        categoryStats.put("Images", 0);
        categoryStats.put("Documents", 0);
        categoryStats.put("Videos", 0);
        categoryStats.put("Music", 0);
        categoryStats.put("Archives", 0);
        categoryStats.put("Programs", 0);
        categoryStats.put("Code", 0);
        categoryStats.put("Others", 0);
    }

    private void resetState() {
        outputLog.setLength(0);
        totalFilesProcessed = 0;
        movedCount = 0;
        skippedCount = 0;
        failedCount = 0;
        initializeStats();
    }

    // ---------------------------
    // VALIDATION
    // ---------------------------

    private boolean isValidFolder() {
        if (folderPath == null || folderPath.trim().isEmpty()) {
            log("ERROR: Invalid folder path.");
            return false;
        }

        if (!folder.exists()) {
            log("ERROR: Folder does not exist.");
            return false;
        }

        if (!folder.isDirectory()) {
            log("ERROR: Selected path is not a folder.");
            return false;
        }

        if (!folder.canRead()) {
            log("ERROR: Folder cannot be accessed.");
            return false;
        }

        return true;
    }

    public File[] scanFiles() {
        if (!isValidFolder()) {
            return new File[0];
        }

        File[] files = folder.listFiles();

        if (files == null) {
            log("WARNING: Unable to read files from folder.");
            return new File[0];
        }

        return files;
    }

    // ---------------------------
    // FILE HELPERS
    // ---------------------------

    public String getExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index > 0 && index < fileName.length() - 1) {
            return fileName.substring(index).toLowerCase();
        }
        return "";
    }

    private boolean isHiddenOrSystemFile(File file) {
        String name = file.getName().toLowerCase();

        return file.isHidden()
                || name.equals("desktop.ini")
                || name.equals("thumbs.db")
                || name.equals(".ds_store");
    }

    private String getCategory(String extension) {
        for (String category : categories.keySet()) {
            if (categories.get(category).contains(extension)) {
                return category;
            }
        }
        return "Others";
    }

    private void createFolderIfNotExists(String category) {
        File categoryFolder = new File(folder, category);
        if (!categoryFolder.exists()) {
            categoryFolder.mkdir();
        }
    }

    public Path getUniquePath(Path destination) {
        int count = 1;

        while (Files.exists(destination)) {
            String fileName = destination.getFileName().toString();
            int dotIndex = fileName.lastIndexOf(".");

            String name = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
            String ext = (dotIndex > 0) ? fileName.substring(dotIndex) : "";

            String newName = name + "(" + count + ")" + ext;
            destination = destination.getParent().resolve(newName);
            count++;
        }

        return destination;
    }

    private boolean isOrganizerGeneratedFile(String fileName) {
        return fileName.equalsIgnoreCase(UNDO_FILE_NAME)
                || fileName.equalsIgnoreCase("organizer_log.txt");
    }

    // ---------------------------
    // PREVIEW
    // ---------------------------

    public void preview() {
        resetState();

        File[] files = scanFiles();

        if (files.length == 0) {
            log("WARNING: No files found to preview.");
            LoggerUtil.writeLog(folderPath, "Preview attempted - no files found.");
            return;
        }

        log("========== FILE PREVIEW ==========");

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }

            if (isOrganizerGeneratedFile(file.getName())) {
                log("SKIPPED Organizer file: " + file.getName());
                skippedCount++;
                continue;
            }

            if (isHiddenOrSystemFile(file)) {
                log("SKIPPED Hidden/System file: " + file.getName());
                skippedCount++;
                continue;
            }

            String ext = getExtension(file.getName());
            String category = getCategory(ext);

            log("FILE: " + file.getName() + " -> " + category);

            categoryStats.put(category, categoryStats.getOrDefault(category, 0) + 1);
            totalFilesProcessed++;
        }

        addSummary("Preview Completed");
        LoggerUtil.writeLog(folderPath, "Preview completed successfully.");
    }

    // ---------------------------
    // ORGANIZE
    // ---------------------------

    public void organize() {
        resetState();

        File[] files = scanFiles();

        if (files.length == 0) {
            log("WARNING: No files found to organize.");
            LoggerUtil.writeLog(folderPath, "Organize attempted - no files found.");
            return;
        }

        clearUndoLog();

        log("========== ORGANIZING FILES ==========");

        for (File file : files) {

            if (!file.isFile()) {
                continue;
            }

            if (isOrganizerGeneratedFile(file.getName())) {
                log("SKIPPED Organizer file: " + file.getName());
                skippedCount++;
                continue;
            }

            if (isHiddenOrSystemFile(file)) {
                log("SKIPPED Hidden/System file: " + file.getName());
                skippedCount++;
                continue;
            }

            String ext = getExtension(file.getName());
            String category = getCategory(ext);

            try {
                createFolderIfNotExists(category);

                Path source = file.toPath();
                Path destination = Paths.get(folderPath, category, file.getName());

                destination = getUniquePath(destination);

                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);

                saveUndoEntry(destination.toString(), source.toString());

                log("MOVED: " + file.getName() + " -> " + category);
                LoggerUtil.writeLog(folderPath, "Moved file: " + file.getName() + " to " + category);

                movedCount++;
                totalFilesProcessed++;
                categoryStats.put(category, categoryStats.getOrDefault(category, 0) + 1);

            } catch (IOException e) {
                log("ERROR moving file: " + file.getName() + " | Reason: " + e.getMessage());
                LoggerUtil.writeLog(folderPath, "Error moving file: " + file.getName() + " | " + e.getMessage());
                failedCount++;
            }
        }

        addSummary("Organization Completed");
        LoggerUtil.writeLog(folderPath, "Organization completed.");
    }

    // ---------------------------
    // UNDO LAST ORGANIZATION
    // ---------------------------

    public void undoLastOrganization() {
        resetState();

        File undoFile = new File(folderPath, UNDO_FILE_NAME);

        if (!undoFile.exists()) {
            log("WARNING: No undo history found.");
            LoggerUtil.writeLog(folderPath, "Undo failed - no undo file found.");
            return;
        }

        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(undoFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            log("ERROR: Failed to read undo log.");
            LoggerUtil.writeLog(folderPath, "Undo failed - could not read undo log.");
            return;
        }

        if (lines.isEmpty()) {
            log("WARNING: Undo log is empty.");
            LoggerUtil.writeLog(folderPath, "Undo failed - undo log empty.");
            return;
        }

        Collections.reverse(lines);

        log("========== UNDO LAST ORGANIZATION ==========");

        for (String line : lines) {
            String[] parts = line.split("\\|\\|");

            if (parts.length != 2) {
                failedCount++;
                continue;
            }

            Path movedPath = Paths.get(parts[0]);
            Path originalPath = Paths.get(parts[1]);

            try {
                if (Files.exists(movedPath)) {
                    Path safeOriginalPath = getUniquePath(originalPath);
                    Files.move(movedPath, safeOriginalPath, StandardCopyOption.REPLACE_EXISTING);

                    log("RESTORED: " + movedPath.getFileName() + " -> Original Folder");
                    LoggerUtil.writeLog(folderPath, "Restored file: " + movedPath.getFileName());
                    movedCount++;
                } else {
                    log("SKIPPED missing file during undo: " + movedPath.getFileName());
                    LoggerUtil.writeLog(folderPath, "Skipped undo file not found: " + movedPath.getFileName());
                    skippedCount++;
                }

            } catch (IOException e) {
                log("ERROR restoring file: " + movedPath.getFileName() + " | Reason: " + e.getMessage());
                LoggerUtil.writeLog(folderPath, "Error restoring file: " + movedPath.getFileName() + " | " + e.getMessage());
                failedCount++;
            }
        }

        clearUndoLog();
        addSummary("Undo Completed");
        LoggerUtil.writeLog(folderPath, "Undo completed.");
    }

    // ---------------------------
    // CUSTOM CATEGORY
    // ---------------------------

    public void addCustomCategory(String categoryName, String extensionsInput) {
        resetState();

        if (categoryName == null || categoryName.trim().isEmpty()) {
            log("ERROR: Invalid category name.");
            return;
        }

        if (extensionsInput == null || extensionsInput.trim().isEmpty()) {
            log("ERROR: No extensions provided.");
            return;
        }

        String cleanCategory = categoryName.trim();

        if (categories.containsKey(cleanCategory)) {
            log("WARNING: Category already exists. It will be updated.");
        }

        String[] extensionsArray = extensionsInput.split(",");
        List<String> extensionList = new ArrayList<>();

        for (String ext : extensionsArray) {
            ext = ext.trim().toLowerCase();

            if (ext.isEmpty()) {
                continue;
            }

            if (!ext.startsWith(".")) {
                ext = "." + ext;
            }

            extensionList.add(ext);
        }

        if (extensionList.isEmpty()) {
            log("ERROR: No valid extensions found.");
            return;
        }

        categories.put(cleanCategory, extensionList);

        if (!categoryStats.containsKey(cleanCategory)) {
            categoryStats.put(cleanCategory, 0);
        }

        log("SUCCESS: Custom category added.");
        log("Category: " + cleanCategory);
        log("Extensions: " + extensionList);

        LoggerUtil.writeLog(folderPath, "Custom category added: " + cleanCategory + " -> " + extensionList);
    }

    // ---------------------------
    // OUTPUT / STATS
    // ---------------------------

    private void log(String message) {
        outputLog.append(message).append("\n");
    }

    private void addSummary(String title) {
        log("");
        log("========== " + title.toUpperCase() + " SUMMARY ==========");
        log("Total Files Processed: " + totalFilesProcessed);
        log("Moved Successfully: " + movedCount);
        log("Skipped: " + skippedCount);
        log("Failed: " + failedCount);
    }

    public String getOutputLog() {
        return outputLog.toString();
    }

    public Map<String, Integer> getCategoryStats() {
        return categoryStats;
    }

    public int getTotalFilesProcessed() {
        return totalFilesProcessed;
    }

    // ---------------------------
    // UNDO LOG FILE HELPERS
    // ---------------------------

    private void saveUndoEntry(String movedPath, String originalPath) {
        File undoFile = new File(folderPath, UNDO_FILE_NAME);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(undoFile, true))) {
            bw.write(movedPath + "||" + originalPath);
            bw.newLine();
        } catch (IOException e) {
            log("WARNING: Could not save undo history for a file.");
        }
    }

    private void clearUndoLog() {
        File undoFile = new File(folderPath, UNDO_FILE_NAME);

        try {
            if (undoFile.exists()) {
                new PrintWriter(undoFile).close();
            }
        } catch (FileNotFoundException e) {
            log("WARNING: Could not clear old undo log.");
        }
    }
}