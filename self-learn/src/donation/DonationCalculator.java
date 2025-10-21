package donation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 捐赠金额计算器 - 解析文本中的捐赠金额并计算总和
 */
public class DonationCalculator {
    
    /**
     * 从文本中提取并计算捐赠总金额
     * @param text 包含捐赠信息的文本
     * @return 捐赠总金额（万元）
     */
    public static double calculateTotalDonation(String text) {
        double total = 0.0;
        
        // 使用正则表达式匹配金额模式：数字+单位（万元/亿元）
        // 匹配模式如：800万元、1.5亿元、3500万元等
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([亿万])\\s*元");
        Matcher matcher = pattern.matcher(text);
        
        System.out.println("=== 捐赠金额解析过程 ===");
        
        // 遍历所有匹配项
        while (matcher.find()) {
            String amountStr = matcher.group(1);  // 数字部分
            String unit = matcher.group(2);       // 单位部分
            
            double amount = Double.parseDouble(amountStr);
            
            // 单位转换：亿元转换为万元
            if ("亿".equals(unit)) {
                amount *= 10000;  // 1亿元 = 10000万元
            }
            
            System.out.println("提取金额: " + amountStr + unit + " = " + amount + "万元");
            total += amount;
        }
        
        return total;
    }
    
    /**
     * 主方法 - 测试捐赠金额计算功能
     */
    public static void main(String[] args) {
        // 捐赠信息文本
        String donationText = "2025年蓝天大学数据科学学院学科建设捐赠仪式上，多家企业及校友提供支持。" +
                "其中，迅腾科技有限公司捐赠800万元用于实验室建设；" +
                "多多拼数据集团捐赠1.5亿元设立专项奖学金；" +
                "校友陈先生捐赠600万元助力人才引进；" +
                "科创投资公司捐赠3500万元支持科研项目；" +
                "此前，阿里啦啦科技曾捐赠500万元用于设备更新，" +
                "云帆数据集团曾捐赠9000万元建设实训基地；" +
                "此外，星河集团捐赠4200万元、校友李女士捐赠300万元，" +
                "相关捐赠已完成签约。";
        
        System.out.println("原始文本:");
        System.out.println(donationText);
        System.out.println();
        
        // 计算总捐赠金额
        double totalDonation = calculateTotalDonation(donationText);
        
        System.out.println();
        System.out.println("=== 计算结果 ===");
        System.out.println("捐赠总金额: " + totalDonation + "万元");
        System.out.println("捐赠总金额: " + (totalDonation/10000) + "亿元");
    }
}