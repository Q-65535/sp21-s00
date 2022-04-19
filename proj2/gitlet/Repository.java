package gitlet;

import java.io.File;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * The default name of the master branch
     */
    public static final String masterBranchName = "master";

    /**
     * The blobs directory
     */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");

    /**
     * The commits directory
     */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    /**
     * The heads directory
     */
    public static final File HEADS_DIR = join(GITLET_DIR, "heads");

    /**
     * The current head file
     */
    public static final File CUR_HEAD = join(GITLET_DIR, "HEAD");

    /**
     * The staging area file
     */
    public static final File STAGING = join(GITLET_DIR, "staging");


    /* TODO: fill in the rest of this class. */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            initDirs();
            initStaging();
            // create initial commit
            Commit initial_commit = new Commit("initial commit");
            initial_commit.makePersistent();
            String hashText = initial_commit.hash();

            checkoutCommit(hashText);
            // initialize the master branch
            createBranch(masterBranchName);

            // set current head to current branch reference
            checkoutBranch(masterBranchName);

            System.out.println("gitlet initialization success!");
        }
    }

    /**
     * Initialize the staging area. The staging area is a map that maps file name
     * to a blob hash
     */
    private static void initStaging() {
        TreeMap<String, String> staging_map = new TreeMap<>();
        writeObject(STAGING, staging_map);
    }

    /**
     * Initialize all the directories in .gitlet
     */
    private static void initDirs() {
        GITLET_DIR.mkdir();
        BLOBS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        HEADS_DIR.mkdir();
    }

    /**
     * Check out a commit based on its hash value
     * @param hashText the hash value of the commit we want to check out
     */
    public static void checkoutCommit(String hashText) {
        Utils.writeContents(CUR_HEAD, hashText);
    }

    /**
     * Check out a branch reference based on given branch name
     * @param branchName the given branch name
     */
    public static void checkoutBranch(String branchName) {
        // write the branch name to current head
        Utils.writeContents(CUR_HEAD, "ref: ", branchName);
    }

    /**
     * Create a branch from current HEAD based on given branch name
     * @param BranchName The name of the branch to be created
     */
    public static void createBranch(String BranchName) {
        File branchFile = join(HEADS_DIR, BranchName);
        String headCommitHash = parseHeadToHash();
        Utils.writeContents(branchFile, headCommitHash);
    }

    private static String parseHeadToHash() {
        if (isRef(CUR_HEAD)) {
            return readRefToHash(CUR_HEAD);
        } else {
            return readContentsAsString(CUR_HEAD);
        }
    }

    /**
     * Read the file to commit hash value
     * @param head The file to be read
     * @return The hash value of the commit this head points to
     */
    private static String readRefToHash(File head) {
        String refForm = readContentsAsString(head);
        String[] split = refForm.split(" ");
        String refTag = split[0];
        String branchName = split[1];
        File branchFile = join(HEADS_DIR, branchName);
        return readContentsAsString(branchFile);
    }

    /**
     * check if a file contains branch reference information
     * @param head the file to be checked
     * @return true if it is a branch reference
     */
    private static boolean isRef(File head) {
        String refForm = readContentsAsString(head);
        if (refForm.contains("ref")) {
            return true;
        }
        return false;
    }


    public static void addFile(String fileName) {
        // get file reference of current commit
        File curCommitFile = join(COMMITS_DIR, parseHeadToHash());
        Commit curCommit = readObject(curCommitFile, Commit.class);
        TreeMap<String, String> fileRefs = curCommit.getFileRefs();

        // get staging area
        TreeMap<String, String> staging = readObject(STAGING, TreeMap.class);

        // deal with the case where the file does not exist
        if (!join(CWD, fileName).isFile()) {
            if (fileRefs.containsKey(fileName)) {
                staging.put(fileName, null);
            } else {
                if (staging.containsKey(fileName)) {
                    staging.remove(fileName);
                } else {
                    throw new GitletException("file not valid: " + fileName);
                }
            }
            writeObject(STAGING, staging);
            return;
        }

        byte[] bytes = readContents(join(CWD, fileName));
        String fileHash = sha1(bytes);
        // if the file ref of current commit doesn't contain that file, create a new blob and stage it
        if (!fileRefs.containsKey(fileName)) {
            // write the file blob
            writeContents(join(BLOBS_DIR, fileHash), bytes);
            staging.put(fileName, fileHash);
        } else {
            String fileHashInCommit = fileRefs.get(fileName);
            // if the file is identical to the file in current commit, remove it from staging area
            if (fileHashInCommit.equals(fileHash)) {
                staging.remove(fileName);
            } else {
                // write the file blob
                writeContents(join(BLOBS_DIR, fileHash), bytes);
                staging.put(fileName, fileHash);
            }
        }
        // save the staging area persistently
        writeObject(STAGING, staging);
    }

    /**
     * Delete the file blob in BLOB directory and remove it from the staging area
     * @param fileName the file name
     * @param staging the staging area reference
     */
    private static void delFileInStaging(String fileName, TreeMap<String, String> staging) {
        if (staging.containsKey(fileName)) {
            // delete the old file blob
            String preFileHash = staging.get(fileName);
            restrictedDelete(join(BLOBS_DIR, preFileHash));
            staging.remove(fileName);
        }
    }
}
