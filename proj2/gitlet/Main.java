package gitlet;

import java.util.TreeMap;

import static gitlet.Repository.*;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author wudi
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Empty argument!");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                init();
                break;
            case "add":
                if (!argsLenEqualCheck(args, 2)) {
                    exitWithMessage("Please specify one file th be added");
                }
                String addFileName = args[1];
                addFile(addFileName);
                break;
            case "rm":
                if (!argsLenEqualCheck(args, 2)) {
                    exitWithMessage("Please specify one file to be removed");
                }
                removeFile(args[1]);
                break;
            case "commit":
                if (!argsLenEqualCheck(args, 2)) {
                    exitWithMessage("Please enter a commit message.");
                }
                TreeMap<String, String> staging = getStaging();
                if (staging.isEmpty()) {
                    exitWithMessage("No changes added to the commit.");
                }
                String message = args[1];
                commit(message);
                break;
            case "log":
                if (!argsLenEqualCheck(args, 1)) {
                    exitWithMessage("no argument needed");
                }
                System.out.print(log());
                break;
            case "global-log":
                if (!argsLenEqualCheck(args, 1)) {
                    exitWithMessage("no argument needed");
                }
                System.out.println(globalLog());
                break;
            case "find":
                if (!argsLenEqualCheck(args, 2)) {
                    exitWithMessage("Please specify a message");
                }
                System.out.println(find(args[1]));
                break;
            case "status":
                if (!argsLenEqualCheck(args, 1)) {
                    exitWithMessage("no argument needed");
                }
                System.out.print(status());
                break;
            case "checkout":
                if (!argsLenRangeCheck(args, 2, 4)) {
                    exitWithMessage("Please specify a file name, or file name with a commit, or a branch");
                }
                // if checkout one file in current commit
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        exitWithMessage("Please use \"--\" before the file name");
                    }
                    String checkFileName = args[2];
                    checkoutCommitFile(getHeadHash(), checkFileName);
                }
                // if checkout a branch
                if (args.length == 2) {
                    checkoutBranch(args[1]);
                }
                // if checkout one file in a specific commit
                if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        exitWithMessage("Please use \"--\" before the file name");
                    }
                    String commitId = args[1];
                    String checkFileName = args[3];
                    checkoutCommitFile(commitId, checkFileName);
                }
                break;
        }
    }

    /**
     * Exit the program with a message
     *
     * @param exitMessage the message to be printed in the console
     */
    private static void exitWithMessage(String exitMessage) {
        System.out.println(exitMessage);
        System.exit(0);
    }

    private static boolean argsLenEqualCheck(String[] args, int expectLen) {
        return args.length == expectLen;
    }

    private static boolean argsLenRangeCheck(String[] args, int minLen, int maxLen) {
        boolean res;
        res = args.length <= maxLen && args.length >= minLen;
        return res;
    }


}
