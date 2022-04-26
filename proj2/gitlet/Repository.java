package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Di Wu
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

    private static String getCurBranchName() {
        if (isDetachedHead()) {
            throw error("DETACHED HEAD state, not on any branch!");
        }
        String refForm = readContentsAsString(CUR_HEAD);
        String[] split = refForm.split(" ");
        String refTag = split[0];
        String branchName = split[1];
        return branchName;
    }

    /**
     * Get current head commit object from head
     *
     * @return The current head commit object
     */
    public static Commit getHeadCommit() {
        String commitFileName = getHeadHash();
        File commitFile = join(COMMITS_DIR, commitFileName);
        return readObject(commitFile, Commit.class);
    }


    /**
     * Read the staging area
     *
     * @return The TreeMap representing the staging area
     */
    static TreeMap<String, String> getStaging() {
        return readObject(STAGING, TreeMap.class);
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            initDirs();
            initStaging();
            // create initial commit
            Commit initial_commit = Commit.initialCommit();
            initial_commit.makePersistent();
            String hashText = initial_commit.hash();

            headCheckoutCommit(hashText);
            // initialize the master branch
            createBranch(masterBranchName);
            // set current head to current branch reference
            headCheckoutBranch(masterBranchName);
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
        Commit newCommit = new Commit(message, getHeadHash());
        newCommit.makePersistent();
        String hashText = newCommit.hash();
        // in detached head state, commit means checkout the new commit
        if (isDetachedHead()) {
            headCheckoutCommit(hashText);
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



    private static boolean isDetachedHead() {
        return !isRef(CUR_HEAD);
    }

    public static Commit getCommitFromHash(String hashText) {
        File commitFile = join(COMMITS_DIR, hashText);
        if (!commitFile.exists()) {
            throw error("the commit hash does not exist: " + hashText);
        }
        return readObject(commitFile, Commit.class);
    }

    /**
     * Check out a commit based on its hash value (only move head, files don't change)
     *
     * @param hashText the hash value of the commit we want to check out
     */
    public static void headCheckoutCommit(String hashText) {
        if (!join(COMMITS_DIR, hashText).exists()) {
            throw error("The commit hash doesn't exist: " + hashText);
        }
        Utils.writeContents(CUR_HEAD, hashText);
    }

    /**
     * Check out a branch reference based on given branch name (only move head, files don't change)
     *
     * @param branchName the given branch name
     */
    public static void headCheckoutBranch(String branchName) {
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
        String headCommitHash = getHeadHash();
        Utils.writeContents(branchFile, headCommitHash);
    }

    public static String getHeadHash() {
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
        File curCommitFile = join(COMMITS_DIR, getHeadHash());
        Commit curCommit = readObject(curCommitFile, Commit.class);
        TreeMap<String, String> fileRefs = curCommit.getFileRefs();

        // get staging area
        TreeMap<String, String> staging = getStaging();

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
     * Write a TreeMap object representing the staging area to STAGING persistently
     *
     * @param staging The TreeMap object representing the staging area
     */
    public static void writeStaging(TreeMap<String, String> staging) {
        writeObject(STAGING, staging);
    }


    public static void removeFile(String fileName) {
        Commit headCommit = getHeadCommit();
        TreeMap<String, String> staging = getStaging();
        // if the file is tracked in current commit
        if (headCommit.getFileRefs().containsKey(fileName)) {
            // map the file name to null to indicate deletion
            staging.put(fileName, null);
            // delete the file in current directory
            restrictedDelete(join(CWD, fileName));
        } else {
            // if the file is neither in commit nor in staging area, error
            if (!staging.containsKey(fileName)) {
                throw error("No reason to remove the file.");
            }
            // otherwise, remove it from staging area
            staging.remove(fileName);
        }

        // finally, persist staging area
        writeStaging(staging);
    }

    public static String log() {
        StringBuilder res = new StringBuilder();
        Commit headCommit = getHeadCommit();
        Commit curCommit = headCommit;
        res.append(curCommit.commitInfoStr());
        while (curCommit.hasParent()) {
            curCommit = curCommit.getParentCommit();
            res.append(curCommit.commitInfoStr());
        }
        return res.toString();
    }

    public static String globalLog() {
        StringBuilder sb = new StringBuilder();
        List<Commit> commitList = getAllCommits();
        for (Commit commit : commitList) {
            sb.append(commit.commitInfoStr());
        }
        return sb.toString();
    }

    /**
     * Given a commit message, find all its corresponding commit hash
     */
    public static String find(String message) {
        StringBuilder res = new StringBuilder();
        List<Commit> commitList = getAllCommits();
        for (Commit commit : commitList) {
            if (message.equals(commit.getMessage())) {
                res.append(commit.hash()).append("\n");
            }
        }
        // if no message matches, error
        if (res.length() == 0) {
            throw error("Found no commit with that message.");
        }
        return res.toString();
    }

    /**
     * Get all commits object in the commit directory
     *
     * @return
     */
    private static List<Commit> getAllCommits() {
        ArrayList<Commit> res = new ArrayList<>();
        List<String> commitHashList = plainFilenamesIn(COMMITS_DIR);
        for (String commitHash : commitHashList) {
            res.add(getCommitFromHash(commitHash));
        }
        return res;
    }

    public static String status() {
        StringBuilder res = new StringBuilder();
        res.append(getBranchStatus()).append("\n");
        res.append(getNonRemoveStagingStatus()).append("\n");
        res.append(getREmoveStagingStatus()).append("\n");
        res.append(getModifiedStatus()).append("\n");
        res.append(getUncheckedFilesStr()).append("\n");
        return res.toString();
    }

    private static String getUncheckedFilesStr() {
        StringBuilder res = new StringBuilder();
        res.append("=== Untracked Files ===").append("\n");
        for (String fileName : getUntrackedFileNamesList()) {
            res.append(fileName).append("\n");
        }
        return res.toString();
    }

    private static List<String> getUntrackedFileNamesList() {
        List<String> res = new ArrayList<>();
        List<String> CWDFileNames = plainFilenamesIn(CWD);
        for (String fileName : CWDFileNames) {
            TreeMap<String, String> fileRefs = getHeadCommit().getFileRefs();
            if (!getStaging().containsKey(fileName) && !fileRefs.containsKey(fileName)) {
                res.add(fileName);
            } else if (fileRefs.containsKey(fileName) && fileRefs.get(fileName) == null) {
                res.add(fileName);
            }
        }
        return res;
    }

    private static String getREmoveStagingStatus() {
        StringBuilder res = new StringBuilder();
        res.append("=== Removed Files ===").append("\n");
        for (Map.Entry<String, String> entry : getStaging().entrySet()) {
            if (entry.getValue() == null) {
                res.append(entry.getKey()).append("\n");
            }
        }
        return res.toString();
    }

    private static String getModifiedStatus() {
        List<String> modifiedFileNames = new ArrayList<>();
        // case1: tracked in current commit but not staged
        // get all the files in current commit but not in staging area
        // put commit file names into a set
        Set<String> commitFileNames = new HashSet<>();
        for (Map.Entry<String, String> entry : getHeadCommit().getFileRefs().entrySet()) {
            commitFileNames.add(entry.getKey());
        }
        // filter all file names in the staging area
        for (Map.Entry<String, String> entry : getStaging().entrySet()) {
            commitFileNames.remove(entry.getKey());
        }
        // get all file names in current working directory
        List<String> CWDFileNameList = plainFilenamesIn(CWD);
        Set<String> CWDFileNames = new HashSet<>();
        for (String s : CWDFileNameList) {
            CWDFileNames.add(s);
        }

        for (String fileName : commitFileNames) {
            String blobHash = getHeadCommit().getFileRefs().get(fileName);
            // if the file is not in current directory and is not staged
            if (!CWDFileNames.contains(fileName)) {
                modifiedFileNames.add(fileName + " (deleted)");
                // if the file is modified in current directory and is not staged
            } else if (!blobHash.equals(getFileHash(join(CWD, fileName)))) {
                modifiedFileNames.add(fileName + " (modified)");
            }
        }


        // case2: files are sataged
        for (Map.Entry<String, String> entry : getStaging().entrySet()) {
            String fileName = entry.getKey();
            // if the file is staged for adding
            if (entry.getValue() != null) {
                String blobHash = entry.getValue();
                // if current directory doesn't contain the stage-for-adding file
                if (!CWDFileNames.contains(fileName)) {
                    modifiedFileNames.add(fileName + " (deleted)");
                    // if current directory contains the file but the file is modfied
                } else if (!blobHash.equals(getFileHash(join(CWD, fileName)))) {
                    modifiedFileNames.add(fileName + " (modified)");
                }
            }
        }
        // build the result string
        StringBuilder res = new StringBuilder();
        res.append("=== Modifications Not Staged For Commit ===").append("\n");
        Collections.sort(modifiedFileNames);
        for (String modifiedFileName : modifiedFileNames) {
            res.append(modifiedFileName).append("\n");
        }
        return res.toString();
    }

    private static String getFileHash(File file) {
        return sha1(readContents(file));
    }

    public static String getNonRemoveStagingStatus() {
        StringBuilder res = new StringBuilder();
        res.append("=== Staged Files ===").append("\n");
        TreeMap<String, String> staging = getStaging();
        // iterate through all mappings in the staging area
        for (Map.Entry<String, String> entry : staging.entrySet()) {
            // if the file name doesn't point to null, it's staged
            if (entry.getValue() != null) {
                res.append(entry.getKey()).append("\n");
            }
        }
        return res.toString();
    }

    public static String getBranchStatus() {
        StringBuilder res = new StringBuilder();
        String curBranchName = getCurBranchName();
        // get all branch names in sorted order
        List<String> branchNames = getAllBranchNames();
        Collections.sort(branchNames);
        for (int i = 0; i < branchNames.size(); i++) {
            // find the branch name that matches current branch, then mark it
            if (branchNames.get(i).equals(curBranchName)) {
                branchNames.set(i, "*" + curBranchName);
                break;
            }
        }
        res.append("=== Branches ===").append("\n");
        for (String branchName : branchNames) {
            res.append(branchName).append("\n");
        }
        return res.toString();
    }

    private static List<String> getAllBranchNames() {
        List<String> heads = plainFilenamesIn(HEADS_DIR);
        return heads;
    }

    public static void checkoutCommitFile(String commitHash, String fileName) {
        if (!commitExists(commitHash)) {
            throw error("No commit with that id exists.");
        }
        Commit commit = getCommitFromHash(commitHash);
        if (!fileExistsInCommit(commit, fileName)) {
            throw error("File does not exist in that commit.");
        }
        byte[] fileContent = commit.readFileContent(fileName);
        writeContents(join(CWD, fileName), fileContent);
    }

    public static void checkoutCommitFile(Commit commit, String fileName) {
        checkoutCommitFile(commit.hash(), fileName);
    }

    private static boolean fileExistsInCommit(Commit commit, String fileName) {
        return commit.getFileRefs().containsKey(fileName);
    }

    private static boolean commitExists(String commitHash) {
        return join(COMMITS_DIR, commitHash).exists();
    }

    public static void checkoutBranch(String branchName) {
        if (!branchExists(branchName)) {
            throw error("No such branch exists.");
        }
        if (!isDetachedHead() && branchName.equals(getCurBranchName())) {
            throw error("No need to checkout the current branch.");
        }
        if (!getUntrackedFileNamesList().isEmpty()) {
            throw error("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        // the commit that the check-out branch points to
        Commit commit = getCommitFromBranchName(branchName);
        // delete all plain files in the working directory
        List<String> fileNames = plainFilenamesIn(CWD);
        for (String fileName : fileNames) {
            restrictedDelete(join(CWD, fileName));
        }
        // check out and clear staging area
        checkoutAllCommitFiles(commit);
        headCheckoutBranch(branchName);
        clearStaging();
    }

    private static void checkoutAllCommitFiles(Commit commit) {
        for (Map.Entry<String, String> entry : commit.getFileRefs().entrySet()) {
            checkoutCommitFile(commit.hash(), entry.getKey());
        }
    }

    private static Commit getCommitFromBranchName(String branchName) {
        File branchFile = join(HEADS_DIR, branchName);
        String commitHash = readContentsAsString(branchFile);
        Commit commit = getCommitFromHash(commitHash);
        return commit;
    }

    private static boolean branchExists(String branchName) {
        return join(HEADS_DIR, branchName).exists();
    }
}

