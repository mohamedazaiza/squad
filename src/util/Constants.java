package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains constant values used throughout the application,
 * including dynamically determined paths for resource files.
 * It attempts to locate files as embedded resources within the JAR first,
 * extracting them to a temporary location if found.
 * If not found as a resource, it falls back to checking common filesystem locations
 * (useful for IDE development).
 */
public class Constants {

    public static final String DB_FILE_NAME = "database.accdb";
    public static final String SUPIR_XML_FILE_NAME = "supir.xml";

    public static final String DB_URL;
    public static final String SUPIR_XML_FILE_PATH;

    static {
        String dbPath = extractResourceOrGetFilesystemPath(DB_FILE_NAME);
        String xmlPath = extractResourceOrGetFilesystemPath(SUPIR_XML_FILE_NAME);

        if (dbPath != null) {
            // UCanAccess requires a file path; memory=false ensures it uses the file, openExclusive=false can help with some environments.
            DB_URL = "jdbc:ucanaccess://" + dbPath + ";memory=false;openExclusive=false;ignoreCase=true";
            System.out.println("INFO: Database will be accessed at: " + dbPath);
        } else {
            // Fallback, though application functionality will likely be impaired.
            DB_URL = "jdbc:ucanaccess://" + DB_FILE_NAME;
            System.err.println("CRITICAL ERROR: Database file '" + DB_FILE_NAME + "' could not be located or extracted. Application might not function correctly.");
        }

        if (xmlPath != null) {
            SUPIR_XML_FILE_PATH = xmlPath;
            System.out.println("INFO: Supir XML will be accessed at: " + xmlPath);
        } else {
            // Fallback, XML import will likely fail.
            SUPIR_XML_FILE_PATH = SUPIR_XML_FILE_NAME;
            System.err.println("CRITICAL ERROR: Supir XML file '" + SUPIR_XML_FILE_NAME + "' could not be located or extracted. XML Import will fail.");
        }
    }

    /**
     * Attempts to load a resource from the JAR (or classpath). If found, it's extracted
     * to a temporary file, and the path to this temp file is returned.
     * If not found as a resource, it searches common filesystem locations.
     *
     * @param resourceName The name of the resource file (e.g., "database.accdb").
     * @return The absolute path to the (potentially temporary) resource file, or null if not found.
     */
    private static String extractResourceOrGetFilesystemPath(String resourceName) {
        // Try to load the resource from the root of the classpath (common for JARs)
        try (InputStream inputStream = Constants.class.getResourceAsStream("/" + resourceName)) {
            if (inputStream != null) {
                File tempFile = File.createTempFile(resourceName.substring(0, resourceName.lastIndexOf('.')), resourceName.substring(resourceName.lastIndexOf('.')));
                tempFile.deleteOnExit(); // Ensure the temporary file is cleaned up
                try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                System.out.println("INFO: Extracted resource '" + resourceName + "' to temporary file: " + tempFile.getAbsolutePath());
                return tempFile.getAbsolutePath();
            } else {
                 System.out.println("INFO: Resource '" + resourceName + "' not found as stream from JAR/classpath root. Attempting filesystem lookup.");
            }
        } catch (IOException e) {
            System.err.println("WARNING: Error extracting resource '" + resourceName + "' from JAR: " + e.getMessage() + ". Attempting filesystem lookup.");
            // Fall through to filesystem check
        } catch (NullPointerException e) {
            System.err.println("WARNING: Could not get resource '" + resourceName + "' as stream (NullPointerException - likely not in JAR). Attempting filesystem lookup.");
            // Fall through to filesystem check
        }


        // Filesystem fallback (useful for IDE development or if files are external to JAR)
        try {
            File codeSourceFile = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String executionDir;

            if (codeSourceFile.isFile()) { // Likely running from a JAR file
                executionDir = codeSourceFile.getParent(); // Directory containing the JAR
            } else { // Likely running from an IDE (e.g., /bin or /target/classes)
                executionDir = codeSourceFile.getPath();
            }

            // 1. Check in the same directory as the JAR or classes
            Path pathInExecutionDir = Paths.get(executionDir, resourceName);
            if (pathInExecutionDir.toFile().exists()) {
                System.out.println("INFO: Found resource '" + resourceName + "' in execution directory: " + pathInExecutionDir.toAbsolutePath().toString());
                return pathInExecutionDir.toAbsolutePath().toString();
            }

            // 2. Check in the parent directory of the JAR/classes (common for project root in IDE)
            Path parentOfExecutionDir = Paths.get(executionDir).getParent();
            if (parentOfExecutionDir != null) {
                Path pathInParentDir = Paths.get(parentOfExecutionDir.toString(), resourceName);
                if (pathInParentDir.toFile().exists()) {
                    System.out.println("INFO: Found resource '" + resourceName + "' in parent of execution directory (project root?): " + pathInParentDir.toAbsolutePath().toString());
                    return pathInParentDir.toAbsolutePath().toString();
                }
            }
            
            // 3. Check in current working directory as a last resort for filesystem
            Path pathInWorkingDir = Paths.get(".", resourceName).toAbsolutePath();
             if (pathInWorkingDir.toFile().exists()) {
                System.out.println("INFO: Found resource '" + resourceName + "' in current working directory: " + pathInWorkingDir.toString());
                return pathInWorkingDir.toString();
            }

        } catch (URISyntaxException e) {
            System.err.println("WARNING: URISyntaxException during filesystem check for '" + resourceName + "': " + e.getMessage());
        } catch (Exception e) {
             System.err.println("WARNING: Unexpected error during filesystem check for '" + resourceName + "': " + e.getMessage());
        }
        
        System.err.println("ERROR: Resource '" + resourceName + "' could NOT be found either in JAR or common filesystem locations.");
        return null;
    }
}