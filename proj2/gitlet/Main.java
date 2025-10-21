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
            System.out.println("请输入命令");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (args.length != 1) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.init();
                break;
            case "add":
                if (args.length != 2) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.add(args[1]);
                break;
            case "ignore":
                if(args.length == 1) {
                    Repository.ignore(null);
                } else {
                    Repository.ignore(args[1]);
                }
                break;
            case "commit":
                if (args.length != 2) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                if (args.length != 2) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.rm(args[1]);
                break;
            case"log":
                if (args.length != 1) {
                    System.out.println("参数错误");
                }
                Repository.log();
                break;
            case "global-log":
                if (args.length != 1) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.globalLog();
                break;
            case  "find":
                if (args.length != 2) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.find(args[1]);
                break;
            case "status":
                if (args.length != 1) {
                    System.out.println("参数错误");
                    System.exit(1);
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
                    System.out.println("参数错误");
                    System.exit(1);
                }
                break;
            case  "branch":
                if (args.length != 2) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                if (args.length != 2) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.reset(args[1], false);
                break;
            case "merge":
                if (args.length != 2) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.merge(args[1]);
                break;
            case "undo":
                if (args.length != 1) {
                    System.out.println("参数错误");
                    System.exit(1);
                }
                Repository.undo();
                break;
            case "--help":
                Repository.help();
                break;
            default:
                System.out.println("命令错误，请使用--help获取帮助");
                System.exit(1);
        }
    }
}
