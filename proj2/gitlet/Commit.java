package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.*;
import static gitlet.Repository.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
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

    /* TODO: fill in the rest of this class. */

    /**
     * This function is for initial commit
     */
    public Commit(String message) {
        this.date = new Date();
        fileRefs = new TreeMap<>();
        this.message = message;
    }

    /**
     * This function is for commit with one parent
     */
    public Commit(String message, String parent) {
        this.date = new Date();
        this.message = message;
        this.fileRefs = new TreeMap<>();
        this.parent = parent;

        inheritRefs();
        handleStage();

    }

    /**
     * This function is for commit with two parents
     */
    public Commit(String message, String parent, String sParent) {
        this.date = new Date();
        this.message = message;
        //TODO add inherit reference function for two parents
        this.parent = parent;
        this.sParent = sParent;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
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

    private void handleStage() {
        TreeMap<String, String> staging = readStaging();
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
        File commitFile = Utils.join(Repository.COMMITS_DIR, hash());
        Utils.writeObject(commitFile, this);
    }

    /**
     * get the string representation of SHA-1 hash value of this commit
     */
    public String hash() {
        return Utils.sha1(Utils.serialize(this));
    }
}
