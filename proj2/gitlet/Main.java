package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author 逐辰
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.init();
                break;
            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;
            case "commit":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.rm(args[1]);
                break;
            case"log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                }
                Repository.log();
                break;
            case "global-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.globalLog();
                break;
            case  "find":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.find(args[1]);
                break;
            case "status":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.status();
                break;
            case  "checkout":
                if (args.length == 2) {
                    Repository.checkout(args[1]);
                }else if (args.length == 4) {
                    Repository.checkout(args[1], args[3]);
                }else if (args.length == 3) {
                    Repository.checkout(args[1], args[2]);
                }else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case  "branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.rmBranch(args[1]);
                break;
            case "nb-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.nbLog();
            case "reset":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.reset(args[1]);
                break;
            case "merge":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.merge(args[1]);
                break;
            // TODO: FILL THE REST IN
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}
