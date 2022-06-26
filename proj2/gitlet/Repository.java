package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.MergeFileStatus.*;
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

    private static String getHeadBranchName() {
        if (isDetachedHead()) {
            exit("DETACHED HEAD state, not on any branch!");
        }
        String refForm = readContentsAsString(CUR_HEAD);
        String[] split = refForm.split(" ");
        String refTag = split[0];
        String branchName = split[1];
        return branchName;
    }

    /**
     * Get current head commit object from head
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
            String hashText = initial_commit.getHash();

            headCheckoutCommit(hashText);
            // initialize the master branch
            createBranch(masterBranchName);
            // set current head to current branch reference
            headCheckoutBranch(masterBranchName);
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
        // the commit message shouldn't be empty
        if (message.length() == 0) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = new Commit(message, getHeadHash());
        newCommit.makePersistent();
        String hashText = newCommit.getHash();
        // in detached head state, commit means checkout the new commit
        if (isDetachedHead()) {
            headCheckoutCommit(hashText);
            // otherwise, update the current branch
        } else {
            changeHeadBranchPtr(hashText);
        }

        // finally, clear the staging area
        clearStaging();
    }

    // @Smell: the two commits methods are similar, there may be a cleaner way to write the code.
    /** special commit method for merging. The given merge branch name indicates the other branch to be
     *  merged into current branch */
    private static void commit(String message, String mergeBranchName) {
        if (!GITLET_DIR.exists()) {
            System.out.println("Gitlet has not been initialized!");
            System.exit(0);
        }
        // the commit message shouldn't be empty
        if (message.length() == 0) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit otherBranchCommit = getCommitFromBranchName(mergeBranchName);
        Commit newCommit = new Commit(message, getHeadHash(), otherBranchCommit.getHash());
        newCommit.makePersistent();
        String hashText = newCommit.getHash();
        // in detached head state, commit means checkout the new commit
        if (isDetachedHead()) {
            headCheckoutCommit(hashText);
            // otherwise, update the current branch
        } else {
            changeHeadBranchPtr(hashText);
        }

        // finally, clear the staging area
        clearStaging();
    }

    /**
     * Change the branch to point to other commit
     *
     * @param branchName The branch to be changed
     * @param commitHash The commit hash the branch will point to
     */
    private static void changeBranchPtr(String branchName, String commitHash) {
        if (!branchExists(branchName)) {
            exit("branch name " + branchName + " doesn't exist!");
        }
        if (!commitExists(commitHash)) {
            exit("commit" + branchName + " doesn't exist!");
        }
        File branchFile = join(HEADS_DIR, branchName);
        writeContents(branchFile, commitHash);
    }

    /**
     * Change current branch to point to the given commit hash
     */
    private static void changeHeadBranchPtr(String commitHash) {
        changeBranchPtr(getHeadBranchName(), commitHash);
    }

    /**
     * Clear the staging area
     */
    private static void clearStaging() {
        writeObject(STAGING, new TreeMap<String, String>());
    }


    /**
     * Check if we are now in the detached state
     */
    private static boolean isDetachedHead() {
        return !isRef(CUR_HEAD);
    }

    /**
     * Get the commit according to the given hashText
     *
     * @param hashText
     * @return
     */
    public static Commit getCommitFromHash(String hashText) {
        File commitFile = join(COMMITS_DIR, hashText);
        // if the given hash text is in the short version, find the corresponding commit file
        // @Smell maybe we have a cleaner way to find the commit
        if (hashText.length() == 8) {
            for (File file : COMMITS_DIR.listFiles()) {
                String FileHash = file.getName();
                if (FileHash.substring(0, 8).equals(hashText)) {
                    commitFile = join(COMMITS_DIR, FileHash);
                    break;
                }
            }
        }
        if (!commitFile.exists()) {
            exit("the commit hash does not exist: " + hashText);
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
            exit("The commit hash doesn't exist: " + hashText);
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
            exit("no such branch exists: " + branchName);
        }
        // write the branch name to current head
        Utils.writeContents(CUR_HEAD, "ref: ", branchName);
    }

    /**
     * Create a branch from current HEAD based on given branch name
     *
     * @param branchName The name of the branch to be created
     */
    public static void createBranch(String branchName) {
        if (branchExists(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File branchFile = join(HEADS_DIR, branchName);
        String headCommitHash = getHeadHash();
        Utils.writeContents(branchFile, headCommitHash);
    }

    public static void removeBranch(String branchName) {
        if (!branchExists(branchName)) {
            exit("A branch with that name does not exist.");
        }
        if (getHeadBranchName().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        restrictedDelete(getBranchFile(branchName));
    }

    /**
     * Get the branch file according to the branch name
     */
    private static File getBranchFile(String branchName) {
        return join(HEADS_DIR, branchName);
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


    /**
     * add a file to the current staging area
     */
    public static void addFile(String fileName) {
        // get file reference of current commit
        File curCommitFile = join(COMMITS_DIR, getHeadHash());
        Commit curCommit = readObject(curCommitFile, Commit.class);
        TreeMap<String, String> fileRefs = curCommit.getFileRefs();

        // get staging area
        TreeMap<String, String> staging = getStaging();

        // deal with the case where the file does not exist
        if (!join(CWD, fileName).isFile()) {
            // if the file is tracked in previous commit, stage the deleted file
            if (fileRefs.containsKey(fileName)) {
                staging.put(fileName, null);
            } else {
                // this case means that the file is added and then deleted
                if (staging.containsKey(fileName)) {
                    staging.remove(fileName);
                    //
                } else {
                    System.out.println("File does not exist.");
                    System.exit(0);
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


    /**
     * Using Gitlet command to remove a file in CWD. (this method not only delete the file, but also record the deletion in staging area)
     */
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
                System.out.println("No reason to remove the file.");
                System.exit(0);
            }
            // otherwise, remove it from staging area
            // notice here that we don't delete the file in CWD, just remove it from staging area
            staging.remove(fileName);
        }

        // finally, persist staging area
        writeStaging(staging);
    }

    /**
     * the gitlet log function
     */
    public static String log() {
        StringBuilder res = new StringBuilder();
        Commit headCommit = getHeadCommit();
        Commit curCommit = headCommit;
        res.append(curCommit.commitInfoStr());
        while (curCommit.hasParent()) {
            curCommit = curCommit.getParentCommit();
            res.append(curCommit.commitInfoStr());
        }
        String finalResult = res.toString();
        return finalResult;
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
                res.append(commit.getHash()).append("\n");
            }
        }
        // if no message matches, error
        if (res.length() == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        return res.toString();
    }

    /**
     * Get all commits object in the commit directory
     */
    private static List<Commit> getAllCommits() {
        ArrayList<Commit> res = new ArrayList<>();
        List<String> commitHashList = plainFilenamesIn(COMMITS_DIR);
        for (String commitHash : commitHashList) {
            res.add(getCommitFromHash(commitHash));
        }
        return res;
    }

    /**
     * Get the string representation of current satus
     */
    public static String status() {
        StringBuilder res = new StringBuilder();
        res.append(getBranchStatus()).append("\n");
        res.append(getNonRemoveStagingStatus()).append("\n");
        res.append(getRemoveStagingStatus()).append("\n");
        res.append(getModifiedStatus()).append("\n");
        res.append(getUncheckedFilesStr()).append("\n");
        return res.toString();
    }

    private static String getUncheckedFilesStr() {
        StringBuilder res = new StringBuilder();
        res.append("=== Untracked Files ===").append("\n");
        for (String fileName : getUntrackedFileNames()) {
            res.append(fileName).append("\n");
        }
        return res.toString();
    }

    private static List<String> getUntrackedFileNames() {
        List<String> res = new ArrayList<>();
        List<String> CWDFileNames = plainFilenamesIn(CWD);
        // loop through all EXISTING files in CWD (only existing file can be in untracked state)
        for (String fileName : CWDFileNames) {
            TreeMap<String, String> fileRefs = getHeadCommit().getFileRefs();
            if (!getStaging().containsKey(fileName) && !fileRefs.containsKey(fileName)) {
                res.add(fileName);
                // if the file is tracked in current commit, but it's staged for removal. This means the file is removed and then added back manually. But Gitlet doesn't aware it's backed to the CWD
            } else if (fileRefs.containsKey(fileName) && getStaging().containsKey(fileName) && getStaging().get(fileName) == null) {
                res.add(fileName);
            }
        }
        return res;
    }

    private static String getRemoveStagingStatus() {
        StringBuilder res = new StringBuilder();
        res.append("=== Removed Files ===").append("\n");
        for (Map.Entry<String, String> entry : getStaging().entrySet()) {
            String fileName = entry.getKey();
            if (entry.getValue() == null && !isFileInCWD(fileName)) {
                res.append(fileName).append("\n");
            }
        }
        return res.toString();
    }

    /**
     * Evaluate whether a file is in CWD directory
     */
    private static boolean isFileInCWD(String fileName) {
        return join(CWD, fileName).exists();
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
        String curBranchName = getHeadBranchName();
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
            exit("No commit with that id exists.");
        }
        Commit commit = getCommitFromHash(commitHash);
        if (!fileExistsInCommit(commit, fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        byte[] fileContent = commit.getFileContent(fileName);
        writeContents(join(CWD, fileName), fileContent);
    }

    public static void checkoutCommitFile(Commit commit, String fileName) {
        checkoutCommitFile(commit.getHash(), fileName);
    }

    private static boolean fileExistsInCommit(Commit commit, String fileName) {
        return commit.getFileRefs().containsKey(fileName);
    }

    private static boolean commitExists(String commitHash) {
        // if the given hash text is in the short version, find the corresponding commit file
        // @Smell maybe we have a cleaner way to find the commit
        if (commitHash.length() == 8) {
            for (File file : COMMITS_DIR.listFiles()) {
                String FileHash = file.getName();
                // If we find a fileHash that has the same first few sub string, return true
                if (FileHash.substring(0, 8).equals(commitHash)) {
                    return true;
                }
            }
            return false;
        }
        return join(COMMITS_DIR, commitHash).exists();
    }

    public static void checkoutBranch(String branchName) {
        if (!branchExists(branchName)) {
            exit("No such branch exists.");
        }
        if (!isDetachedHead() && branchName.equals(getHeadBranchName())) {
            exit("No need to checkout the current branch.");
        }
        //TODO carefully deal with this case according to the project specification
        if (!getUntrackedFileNames().isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        // the commit that the check-out branch points to
        Commit commit = getCommitFromBranchName(branchName);
        // delete all plain files that are tracked in current commit
        delFilesInHeadCommit();
        // check out and clear staging area
        checkoutAllCommitFiles(commit);
        headCheckoutBranch(branchName);
        clearStaging();
    }

    /**
     * Delete all files that are tracked in head commit
     */
    private static void delFilesInHeadCommit() {
        Commit headCommit = getHeadCommit();
        // delete all plain files that are tracked in current commit
        List<String> trackedFileNames = headCommit.getTrackedFileNames();
        for (String trackedFileName : trackedFileNames) {
            restrictedDelete(join(CWD, trackedFileName));
        }
    }

    /**
     * Checkout all files in the given commit in CWD
     */
    private static void checkoutAllCommitFiles(Commit commit) {
        for (Map.Entry<String, String> entry : commit.getFileRefs().entrySet()) {
            checkoutCommitFile(commit.getHash(), entry.getKey());
        }
    }

    private static void checkoutAllCommitFiles(String commitHash) {
        checkoutAllCommitFiles(getCommitFromHash(commitHash));
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

    /**
     * For gitlet function reset
     */
    public static void reset(String commitHash) {
        // the case when the given commit doesn't exist
        if (!commitExists(commitHash)) {
            exit("No commit with that id exists.");
        }
        // if there is untracked files, error
        if (!getUntrackedFileNames().isEmpty()) {
            exit("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        delFilesInHeadCommit();
        checkoutAllCommitFiles(commitHash);
        changeHeadBranchPtr(commitHash);
        clearStaging();
    }

    public static void merge(String branchName) {
        if (!getStaging().isEmpty()) {
            exit("You have uncommitted changes");
        }
        if (!branchExists(branchName)) {
            exit("A branch with that name does not exist.");
        }
        if (getCommitFromBranchName(branchName).equals(getHeadCommit())) {
            exit("Cannot merge a branch with itself.");
        }
        // @Logic: do something better to deal with untracked files. Instead of just exit once we find an untracked file.
        if (!getUntrackedFileNames().isEmpty()) {
            exit("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        Commit otherCommit = getCommitFromBranchName(branchName);
        Commit thisCommit = getHeadCommit();
        Commit splitPointCommit = getSplitPointCommit(thisCommit, otherCommit);
        if (splitPointCommit.equals(otherCommit)) {
            exit("Given branch is an ancestor of the current branch.");
        }
        if (splitPointCommit.equals(thisCommit)) {
            checkoutBranch(branchName);
            exit("Current branch fast-forwarded.");
        }
        // get all the file names in this and other commits, put them in a set
        Set<String> fileNames = new HashSet<>();
        List<String> thisFileNames = thisCommit.getTrackedFileNames();
        List<String> otherFileNames = otherCommit.getTrackedFileNames();
        fileNames.addAll(thisFileNames);
        fileNames.addAll(otherFileNames);

        boolean hasMergeConflict = false;
        for (String fileName : fileNames) {
            MergeFileStatus thisStatus = fileStatusCheck(splitPointCommit, thisCommit, fileName);
            MergeFileStatus otherStatus = fileStatusCheck(splitPointCommit, otherCommit, fileName);
            if (thisStatus.equals(UNCHANGED)) {
                if (otherStatus.equals(MODIFIED) || otherStatus.equals(ADDED)) {
                    checkoutCommitFile(otherCommit, fileName);
                    // stage it
                    addFile(fileName);
                }
                if (otherStatus.equals(DELETED)) {
                    removeFile(fileName);
                }
            }
            // the case of the file being deleted in both commits are not possible, because the file is selected from those two commits
            if (thisStatus.equals(MODIFIED) || thisStatus.equals(DELETED)) {
                if (otherStatus.equals(MODIFIED) || otherStatus.equals(DELETED)) {
                    // @Logic: in mergeFile, we check whether the file is in both of the two commits
                    // @Smell: in mergeFile, we do a lot of functions that are not related to merge
                    hasMergeConflict = mergeFile(thisCommit, otherCommit, fileName);
                }
            }
            if (thisStatus.equals(ADDED) && otherStatus.equals(ADDED)) {
                hasMergeConflict = mergeFile(thisCommit, otherCommit, fileName);
            }
        }

        String message = "Merged " + branchName + " into " + getHeadBranchName() + ".";
        commit(message, branchName);
        if (hasMergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static MergeFileStatus fileStatusCheck(Commit splitPointCommit, Commit curCommit, String fileName) {
        // the file is not in both of the two commits
        if (!splitPointCommit.hasFile(fileName) && !curCommit.hasFile(fileName)) {
            return UNCHANGED;
        }
        if (!splitPointCommit.hasFile(fileName) && curCommit.hasFile(fileName)) {
            return ADDED;
        }
        if (splitPointCommit.hasFile(fileName) && !curCommit.hasFile(fileName)) {
            return DELETED;
        }
        if (splitPointCommit.hasFile(fileName) && curCommit.hasFile(fileName)) {
            if (splitPointCommit.getFileHash(fileName).equals(curCommit.getFileHash(fileName))) {
                return UNCHANGED;
            }
            else {
                return MODIFIED;
            }
        }
        // @Smell do something better to not allow this return statement, because the code will not fall into this area
        return UNCHANGED;
    }

    /** Get the splitting point commit of the two given commits */
    private static Commit getSplitPointCommit(Commit ca, Commit cb) {
        Set<Commit> ancestorsA = getAllAncestors(ca);
        Set<Commit> ancestorsB = getAllAncestors(cb);
        ancestorsA.retainAll(ancestorsB);
        Set<Commit> intersected = ancestorsA;
        // record all the parents of the commits in the intersected set (their role are parents in the set)
        Set<Commit> parents = new HashSet<>();
        for (Commit c : intersected) {
            if (c.hasParent()) {
                parents.add(c.getParentCommit());
            }
            if (c.hasSecondParent()) {
                parents.add(c.getSecondParentCommit());
            }
        }
        // Then, we find the commit that doesn't play the role of parent in the intersected set
        Commit splitPointCommit = null;
        for (Commit c : intersected) {
            if (!parents.contains(c)) {
                splitPointCommit = c;
            }
        }
        return splitPointCommit;
    }

    /** Get all ancestors of the given commit, including second parents */
    private static Set<Commit> getAllAncestors(Commit c) {
        Set<Commit> ancestors = new HashSet<>();
        if (c.hasParent()) {
            Commit firstParentCommit = c.getParentCommit();
            ancestors.add(firstParentCommit);
            ancestors.addAll(getAllAncestors(firstParentCommit));
        }
        if (c.hasSecondParent()) {
            Commit secondparentCommit = c.getSecondParentCommit();
            ancestors.add(secondparentCommit);
            ancestors.addAll(getAllAncestors(secondparentCommit));
        }
        return ancestors;
    }

    /** merge two files in different commits, return true if the files are merged */
    private static boolean mergeFile(Commit mainCommit, Commit otherCommit, String fileName) {
        // if the file doesn't exist in both of the given commits, just return
        if (!mainCommit.hasFile(fileName) && !otherCommit.hasFile(fileName)) {
            return false;
        }
        // if the files in two commits are just identical, do nothing
        if (mainCommit.hasFile(fileName) && otherCommit.hasFile(fileName)) {
            if (mainCommit.getFileHash(fileName).equals(otherCommit.getFileHash(fileName))) {
                return false;
            }
        }
        String mainFileContent;
        if (mainCommit.hasFile(fileName)) {
            mainFileContent = new String(mainCommit.getFileContent(fileName));
        } else {
            mainFileContent = "";
        }
        String otherFileContent;
        if (otherCommit.hasFile(fileName)) {
            otherFileContent = new String(otherCommit.getFileContent(fileName));
        } else {
            otherFileContent = "";
        }
        String headLine = "<<<<<<< HEAD\n";
        String middleLine = "=======\n";
        String tailLine = ">>>>>>>\n";
        writeContents(join(CWD, fileName), headLine, mainFileContent, middleLine, otherFileContent, tailLine);
        // remember stage it
        addFile(fileName);
        return true;
    }

    // universal exit way
    private static void exit(String exitMessage) {
        System.out.println(exitMessage);
        System.exit(0);
    }
}
