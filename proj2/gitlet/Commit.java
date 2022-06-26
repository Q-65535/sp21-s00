package gitlet;


import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Repository.*;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Di Wu
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;

    /**
     * The creation time of this commit
     */
    private Date date;

    /**
     * Mappings from file names to their corresponding blobs (SHA-1 value)
     */
    private TreeMap<String, String> fileRefs;

    /**
     * The main parent commit of this commit
     */
    private String parent;

    /**
     * The second parent commit of this commit (for merging)
     */
    private String sParent;

    /**
     * the SHA-1 hash value of this commit
     */
    private String commitHash;

    /**
     * This function is for initial commit
     */
    private Commit(String message) {
        this.date = new Date(0);
        fileRefs = new TreeMap<>();
        this.message = message;
    }

    /**
     * Only for initial commit
     *
     * @return the initial commit
     */
    public static Commit initialCommit() {
        return new Commit("initial commit");
    }

    /**
     * This function is for commit with one parent
     */
    public Commit(String message, String parent) {
        this.date = new Date();
        this.message = message;
        this.parent = parent;

        this.fileRefs = new TreeMap<>();
        inheritRefs();
        fetchStaging();

    }

    /**
     * This function is for commit with two parents
     */
    public Commit(String message, String parent, String sParent) {
        this.date = new Date();
        this.message = message;
        this.parent = parent;
        this.sParent = sParent;

        this.fileRefs = new TreeMap<>();
        inheritRefs();
        fetchStaging();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        return getHash().equals(commit.getHash());
    }

    @Override
    public int hashCode() {
        return getHash().hashCode();
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    /**
     * @return the string representation of this commit's date
     */
    public String getDateStr() {
        return new SimpleDateFormat("E MMM dd HH:mm:ss yyyy").format(date) + " +0630";
    }

    public TreeMap<String, String> getFileRefs() {
        return fileRefs;
    }

    public String getParent() {
        return parent;
    }

    public String getsParent() {
        return sParent;
    }

    /**
     * inherit file references from parent commit
     */
    private void inheritRefs() {
        if (this.fileRefs == null) {
            throw error("file reference is null");
        }
        File parentFile = Utils.join(Repository.COMMITS_DIR, parent);
        Commit parentCommit = Utils.readObject(parentFile, Commit.class);
        // if the parent commit is the initial commit (which means that the file reference map is null)
        if (parentCommit.fileRefs == null) {
            return;
        }
        this.fileRefs.putAll(parentCommit.fileRefs);
    }

    private void fetchStaging() {
        TreeMap<String, String> staging = getStaging();
        if (staging.isEmpty()) {
            throw error("The staging area is empty, commit failed");
        }
        for (Map.Entry<String, String> stagingEntry : staging.entrySet()) {
            String fileName = stagingEntry.getKey();
            String fileHash = stagingEntry.getValue();
            // if the file is to be deleted, remove it from file references
            if (fileHash == null) {
                fileRefs.remove(fileName);
            } else {
                fileRefs.put(fileName, fileHash);
            }
        }
    }

    public void makePersistent() {
        File commitFile = Utils.join(Repository.COMMITS_DIR, getHash());
        Utils.writeObject(commitFile, this);
    }

    /**
     * get the string representation of SHA-1 hash value of this commit
     */
    public String getHash() {
        if (commitHash == null) {
            commitHash = sha1(serialize(this));
        }
        return commitHash;
    }

    /**
     * Estimate whether the given file (represented as file name) is tracked in this commit
     *
     * @param fileName the given file name
     * @return true if the file is tracked in this commit, false otherwise
     */
    boolean hasFile(String fileName) {
        return fileRefs.containsKey(fileName);
    }

    public String commitInfoStr() {
        StringBuilder sb = new StringBuilder();
        sb.append("===").append("\n");
        // hash info
        sb.append("commit ").append(getHash()).append("\n");
        //  date info
        sb.append("Date: ").append(getDateStr()).append("\n");
        // message info
        sb.append(message).append("\n").append("\n");
        return sb.toString();
    }

    public String getFileHash(String fileName) {
        if (!fileRefs.containsKey(fileName)) {
            // throw error("File does not exist in that commit.");
            throw error("The file " + "\" " + fileName + "\"" + " doesn't exist in the commit: " + getHash());
        }
        return fileRefs.get(fileName);
    }

    public byte[] getFileContent(String fileName) {
        if (!fileRefs.containsKey(fileName)) {
//            throw error("File does not exist in that commit.");
            throw error("The file " + "\" " + fileName + "\"" + " doesn't exist in the commit: " + getHash());
        }
        String blobHash = fileRefs.get(fileName);
        File blobFile = join(BLOBS_DIR, blobHash);
        return readContents(blobFile);
    }

    public boolean hasParent() {
        return parent != null;
    }

    public Commit getParentCommit() {
        return getCommitFromHash(parent);
    }

    public boolean hasSecondParent() {
        return sParent != null;
    }

    public Commit getSecondParentCommit() {
        return getCommitFromHash(sParent);
    }

    public List<String> getTrackedFileNames() {
        ArrayList<String> fileNames = new ArrayList<>();
        for (Map.Entry<String, String> entry : fileRefs.entrySet()) {
            fileNames.add(entry.getKey());
        }
        return fileNames;
    }
}
