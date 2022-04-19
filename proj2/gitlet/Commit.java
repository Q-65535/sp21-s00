package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.TreeMap;

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
    public Commit(String message, TreeMap<String, String> newFileRefs, String parent) {
        this.date = new Date();
        this.message = message;

        // deal with file references
        this.fileRefs = new TreeMap<>();
        inheritRefs(parent);
        this.fileRefs.putAll(newFileRefs);

        this.parent = parent;
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
     * @param parent the hash value of parent commit
     */
    private void inheritRefs(String parent) {
        File parentFile = Utils.join(Repository.COMMITS_DIR, parent);
        Commit parentCommit = Utils.readObject(parentFile, Commit.class);
        // if the parent commit is the initial commit (which means that the file reference map is null)
        if (parentCommit.fileRefs == null) {
            return;
        }
        this.fileRefs.putAll(parentCommit.fileRefs);
    }

    /**
     * This function is for commit with two parents
     */
    public Commit(String message, TreeMap<String, String> fileRefs, String parent, String sParent) {
        this.date = new Date();
        this.message = message;
        //TODO add inherit reference function for two parents
        this.fileRefs = fileRefs;
        this.parent = parent;
        this.sParent = sParent;
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
