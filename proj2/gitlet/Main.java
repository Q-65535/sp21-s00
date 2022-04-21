package gitlet;
import java.util.TreeMap;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author wudi
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length==0) {
            System.out.println("Empty argument!");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                init();
                break;
            case "add":
                if (!argsLenEqualCheck(args, 2)) {
                    exitWithMessage("Please specify one file th be added");
                    System.out.println();
                    System.exit(0);
                }
                String fileName = args[1];
                addFile(fileName);
                break;
            case "commit":
                if (!argsLenEqualCheck(args, 2)) {
                    exitWithMessage("Please enter a commit message.");
                }
                TreeMap<String, String> staging = readStaging();
                if (staging.isEmpty()) {
                    exitWithMessage("No changes added to the commit.");
                }
                String message = args[1];
                commit(message);
                System.out.println("commit successfully!");
        }
    }

    /**
     * Exit the program with a message
     * @param exitMessage the message to be printed in the console
     */
    private static void exitWithMessage(String exitMessage) {
        System.out.println(exitMessage);
        System.exit(0);
    }

    private static boolean argsLenEqualCheck(String[] args, int expectLen) {
        return args.length == expectLen;
    }




}
