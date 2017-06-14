package org.shipkit.internal.comparison;

import org.shipkit.internal.comparison.diff.Diff;
import org.shipkit.internal.comparison.diff.DirectoryDiffGenerator;
import org.shipkit.internal.util.ExposedForTesting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.lang.String.format;
import static org.shipkit.internal.util.ArgumentValidation.notNull;

class ZipComparator{

    private final DirectoryDiffGenerator directoryDiffGenerator;


    ZipComparator() {
        directoryDiffGenerator = new DirectoryDiffGenerator();
    }

    @ExposedForTesting
    ZipComparator(DirectoryDiffGenerator directoryDiffGenerator){
        this.directoryDiffGenerator = directoryDiffGenerator;
    }

    public Diff areEqual(File previousFile, File currentFile) {
        notNull(previousFile, "previous version file to compare", currentFile, "current version file to compare");
        return compareZips(previousFile, currentFile);
    }

    Diff compareZips(File previousFile, File currentFile) {
        ZipFile file1 = openZipFile(previousFile.getAbsolutePath());
        ZipFile file2 = openZipFile(currentFile.getAbsolutePath());

        Set<String> previous = extractEntries(file1);
        Set<String> current = extractEntries(file2);

        int differences = 0;

        List<String> addedFiles = new ArrayList<String>();
        List<String> removedFiles = new ArrayList<String>();
        List<String> changedFiles = new ArrayList<String>();

        for (String name : previous) {
            if (!current.contains(name)) {
                removedFiles.add(name);
                differences += 1;
                continue;
            }
            try {
                current.remove(name);
                if (!streamsEqual(file1.getInputStream(file1.getEntry(name)), file2.getInputStream(file2
                        .getEntry(name)))) {
                    changedFiles.add(name);
                    differences += 1;
                    continue;
                }
            } catch (Exception e) {
                throw new ZipCompareException(format("Unable to compare zip entry '%s' found in '%s' with '%s'",
                      name, previousFile.getAbsolutePath(), currentFile.getAbsolutePath()), e);
            }
        }
        for (String name : current) {
            addedFiles.add(name);
            differences += 1;
        }

        String diffOutput = directoryDiffGenerator.generateDiffOutput(addedFiles, removedFiles, changedFiles);

        if (differences > 0) {
            return Diff.ofDifferentFiles(previousFile, currentFile, diffOutput);
        }
        return Diff.ofEqualFiles(previousFile, currentFile);
    }

    private Set<String> extractEntries(ZipFile file1) {
        Set<String> set1 = new LinkedHashSet<String>();
        for (Enumeration e = file1.entries(); e.hasMoreElements();) {
            set1.add(((ZipEntry) e.nextElement()).getName());
        }
        return set1;
    }

    private ZipFile openZipFile(String filePath) {
        try {
            return new ZipFile(filePath);
        } catch (IOException e) {
            throw new ZipCompareException("Could not open zip file " + filePath, e);
        }
    }

    static boolean streamsEqual(InputStream stream1, InputStream stream2) throws IOException {
        byte[] buf1 = new byte[4096];
        byte[] buf2 = new byte[4096];
        boolean done1 = false;
        boolean done2 = false;

        try {
            while (!done1) {
                int off1 = 0;
                int off2 = 0;

                while (off1 < buf1.length) {
                    int count = stream1.read(buf1, off1, buf1.length - off1);
                    if (count < 0) {
                        done1 = true;
                        break;
                    }
                    off1 += count;
                }
                while (off2 < buf2.length) {
                    int count = stream2.read(buf2, off2, buf2.length - off2);
                    if (count < 0) {
                        done2 = true;
                        break;
                    }
                    off2 += count;
                }
                if (off1 != off2 || done1 != done2) {
                    return false;
                }
                for (int i = 0; i < off1; i++) {
                    if (buf1[i] != buf2[i]) {
                        return false;
                    }
                }
            }
            return true;
        } finally {
            stream1.close();
            stream2.close();
        }
    }

    static class ZipCompareException extends RuntimeException {
        public ZipCompareException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
