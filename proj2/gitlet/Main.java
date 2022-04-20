package gitlet;
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
                    throw error("Please specify one file");
                }
                String fileName = args[1];
                addFile(fileName);
                break;
            case "commit":
                if (!argsLenEqualCheck(args, 2)) {
                    throw error("please specify one commit message!");
                }
                String message = args[1];
                commit(message);
                System.out.println("commit successfully!");
        }
    }

    private static boolean argsLenEqualCheck(String[] args, int expectLen) {
        return args.length == expectLen;
    }


}
