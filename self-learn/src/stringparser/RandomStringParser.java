package src.stringparser;

import java.util.Random;

/**
 * 随机字符串解析器 - 生成随机字符串并按类别解析
 */
public class RandomStringParser {
    
    // 定义字符集
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!#$&*";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
    
    private static final Random random = new Random();
    
    /**
     * 生成指定长度的随机字符串
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALL_CHARS.length());
            sb.append(ALL_CHARS.charAt(randomIndex));
        }
        return sb.toString();
    }
    
    /**
     * 解析字符串，按类别分离字符
     * @param str 待解析的字符串
     */
    public static void parseString(String str) {
        StringBuilder uppercase = new StringBuilder();
        StringBuilder lowercase = new StringBuilder();
        StringBuilder digits = new StringBuilder();
        StringBuilder others = new StringBuilder();
        
        // 遍历字符串中的每个字符并分类
        for (char c : str.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                uppercase.append(c);
            } else if (c >= 'a' && c <= 'z') {
                lowercase.append(c);
            } else if (c >= '0' && c <= '9') {
                digits.append(c);
            } else {
                others.append(c);
            }
        }
        
        // 输出分类结果
        System.out.println("原始字符串: " + str);
        System.out.println("大写英文字母: " + uppercase.toString());
        System.out.println("小写英文字母: " + lowercase.toString());
        System.out.println("数字: " + digits.toString());
        System.out.println("其他字符: " + others.toString());
        System.out.println(); // 空行分隔
    }
    
    /**
     * 主方法 - 运行测试
     */
    public static void main(String[] args) {
        System.out.println("=== 随机字符串生成与解析测试 ===\n");
        
        // 循环测试6次
        for (int i = 1; i <= 6; i++) {
            System.out.println("第" + i + "次测试:");
            
            // 生成25-35之间的随机长度
            int length = 25 + random.nextInt(11); // 25 + [0,10] = [25,35]
            
            // 生成随机字符串
            String randomString = generateRandomString(length);
            
            // 解析字符串
            parseString(randomString);
        }
        
        System.out.println("=== 测试完成 ===");
    }
}