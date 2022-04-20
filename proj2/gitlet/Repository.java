package gitlet;

import java.io.File;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
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

    public static void commit(String message) {
        if (!GITLET_DIR.exists()) {
            System.out.println("Gitlet has not been initialized!");
            System.exit(0);
        }
        Commit newCommit = new Commit(message, parseHeadToHash());
        newCommit.makePersistent();
        String hashText = newCommit.hash();
        // in detached head state, commit means checkout the new commit
        if (isDetachedHead()) {
            checkoutCommit(hashText);
            // otherwise, update the current branch
        } else {
            String curBranchName = getCurBranchName();
            File curBranchFile = join(HEADS_DIR, curBranchName);
            writeContents(curBranchFile, hashText);
        }

        // finally, clear the staging area
        clearStaging();
    }

    private static void clearStaging() {
        writeObject(STAGING, new TreeMap<String, String>());
    }

    private static String getCurBranchName() {
        if (isDetachedHead()) {
            throw error("DETACHED HEAD state, not in any branch!");
        }
        String refForm = readContentsAsString(CUR_HEAD);
        String[] split = refForm.split(" ");
        String refTag = split[0];
        String branchName = split[1];
        return branchName;
    }

    private static boolean isDetachedHead() {
        return !isRef(CUR_HEAD);
    }

    /**
     * Check out a commit based on its hash value
     *
     * @param hashText the hash value of the commit we want to check out
     */
    public static void checkoutCommit(String hashText) {
        if (!join(COMMITS_DIR, hashText).exists()) {
            throw error("The commit hash doesn't exist: " + hashText);
        }
        Utils.writeContents(CUR_HEAD, hashText);
    }

    /**
     * Check out a branch reference based on given branch name
     *
     * @param branchName the given branch name
     */
    public static void checkoutBranch(String branchName) {
        if (!join(HEADS_DIR, branchName).isFile()) {
            throw error("no such branch exists: " + branchName);
        }
        // write the branch name to current head
        Utils.writeContents(CUR_HEAD, "ref: ", branchName);
    }

    /**
     * Create a branch from current HEAD based on given branch name
     *
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
     *
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
     *
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
        TreeMap<String, String> staging = readStaging();

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
        writeStaging(staging);
    }

    /**
     * Read the staging area
     *
     * @return The TreeMap representing the staging area
     */
    public static TreeMap<String, String> readStaging() {
        return readObject(STAGING, TreeMap.class);
    }

    /**
     * Write a TreeMap object representing the staging area to STAGING persistently
     *
     * @param staging The TreeMap object representing the staging area
     */
    public static void writeStaging(TreeMap<String, String> staging) {
        writeObject(STAGING, staging);
    }

    /**
     * Get current head commit object from head
     *
     * @return The current head commit object
     */
    public static Commit getHeadCommit() {
        String commitFileName = parseHeadToHash();
        File commitFile = join(COMMITS_DIR, commitFileName);
        return readObject(commitFile, Commit.class);
    }
}
