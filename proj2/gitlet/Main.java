package gitlet;
import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                init();
                break;
            case "add":
                if (args.length != 2) {
                    throw new GitletException("specify the file to be added");
                }
                String fileName = args[1];
                addFile(fileName);
                break;
            // TODO: FILL THE REST IN
        }
    }
}
