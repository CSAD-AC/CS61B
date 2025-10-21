package src.wordcount;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 单词计数器 - 统计文本中英文单词出现次数
 */
public class WordCounter {
    
    /**
     * 统计文本中每个英文单词出现的次数
     * @param text 输入文本
     * @return 单词及其出现次数的映射
     */
    public static Map<String, Integer> countWords(String text) {
        Map<String, Integer> wordCount = new HashMap<>();
        
        // 使用正则表达式匹配英文单词（只包含字母的序列）
        Pattern pattern = Pattern.compile("\\b[a-zA-Z]+\\b");
        Matcher matcher = pattern.matcher(text);
        
        // 遍历所有匹配的单词
        while (matcher.find()) {
            String word = matcher.group().toLowerCase(); // 转为小写
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }
        
        return wordCount;
    }
    
    /**
     * 获取出现次数最多的前N个单词
     * @param wordCount 单词计数映射
     * @param topN 需要返回的单词数量
     * @return 按出现次数排序的前N个单词列表
     */
    public static List<Map.Entry<String, Integer>> getTopWords(Map<String, Integer> wordCount, int topN) {
        // 将映射转换为列表以便排序
        List<Map.Entry<String, Integer>> wordList = new ArrayList<>(wordCount.entrySet());
        
        // 按出现次数从大到小排序，次数相同时按字母顺序排序
        wordList.sort((entry1, entry2) -> {
            int countCompare = entry2.getValue().compareTo(entry1.getValue()); // 降序
            if (countCompare != 0) {
                return countCompare;
            }
            return entry1.getKey().compareTo(entry2.getKey()); // 升序
        });
        
        // 返回前N个单词
        return wordList.subList(0, Math.min(topN, wordList.size()));
    }
    
    /**
     * 主方法 - 测试单词统计功能
     */
    public static void main(String[] args) {
        String text =
                "College of Computer Science and Software Engineering\n" +
                " SZU  CN\n" +
                "Home\n" +
                "About\n" +
                "Organization\n" +
                "People\n" +
                "Research\n" +
                "Courses\n" +
                "News & Events\n" +
                "Admission\n" +
                "Position\n" +
                "Global\n" +
                " CN\n" +
                "Home\n" +
                "About\n" +
                "Organization\n" +
                "People\n" +
                "Research\n" +
                "Courses\n" +
                "News & Events\n" +
                "Admission\n" +
                "Position\n" +
                "Global\n" +
                "Homepage\n" +
                "General Information\n" +
                "Dean Notes\n" +
                "Current Leaders\n" +
                "Dean Hui Huang\n" +
                "We are in the information age. Computer Science and Technology are closely connected with all areas of human life in unprecedented depth and breadth, constantly breaking the barriers of time and space between people, promoting the rapid iteration of knowledge, achieving the sensory digitalization and unlimitedly extending the cognitive boundary. \n" +
                "In 1983, the Computer Science of Shenzhen University was initialized with the aid of Tsinghua University. At the end of 2008, the College of Computer Science and Software Engineering (CSSE) was established. For more than ten years, CSSE has been forging ahead steadily and fruitfully. The faculty, staff and students have never forgotten the original intention, keeping in mind the mission of making our country stronger with science and technology, striving toward the world-class disciplines, and reaping prosperous results in the multilateral progress. \n" +
                "Success is not final, failure is not fatal;\n" +
                "it is the courage to continue that counts.\n" +
                "- Winston Churchill\n" +
                "Though good is good, yet better is better! There is no smooth road in science. Only those who do not fear difficulties and dangers to climb along the steep mountain road can reach the glorious summit!\n" +
                "May you all\n" +
                "work hard, live well, love Lots, laugh often!\n" +
                "BIO\n" +
                "Hui Huang, Ph.D in Math, Chair Professor, Founding Director of the Visual Computing Research Center, Dean of the College of Computer Science and Software Engineering, Shenzhen University. Her research interests lie in Computer Graphics, focusing on Point Cloud Processing, Geometric Modeling, Shape Analysis, 3D Acquisition and Creation. She was on the editorial board of ACM TOG and IEEE TVCG, held positions with the SIGGRAPH Technical Papers Advisory Board, EG Executive Committee, EG SGP Steering Committee and GRSI International Evaluation Committee, and served as CADCG 2025 Program Chair, SIGGRAPH Asia 2024 Courses Chair, SMI 2024 Conference Chair, CVM 2023 Conference Chair, CASA 2022 Program Chair, SGP 2019 Program Chair, SIGGRAPH Asia 2017 Technical Briefs and Posters Chair, SIGGRAPH Asia 2016 Workshops Chair and SIGGRAPH Asia 2014 Community Liaison Chair. She has authored over 60 papers in SIGGRAPH/TOG venues, and presided over more than 10 major national projects or key initiatives. She has also been honored with awards including the ACM SIGGRAPH Test-of-Time Award, the Natural Science First Prize from the China Computer Federation, and the Shenzhen Technology Invention First Prize. Additionally, she has been consistently recognized as a Highly Cited Chinese Researcher, named among the World's Top 2% Scientists, selected for the AMiner AI 2000 Most Influential Scholars Award, and ranked as a globally top-cited female scientist. For more detail, please check her homepage: https://vcc.tech/~huihuang\n" +
                "LINKS\n" +
                "CONTACT US\n" +
                "ADDRESS\n" +
                "Zhiteng Building, Canghai Campus of Shenzhen University, Nanshan District, Shenzhen, Guangdong Province,\n" +
                "China 518060\n" +
                "Tel & FAX\n" +
                "+86 0755 26534078\n" +
                "E-mail\n" +
                "csse@szu.edu.cn\n" +
                "SUBSCRIPTION TO WECHAT OFFICIAL ACCOUNTS\n" +
                "CSSE\n" +
                "Party-masses\n" +
                "SZU\n" +
                "粤ICP备11018045号 粤公网安备44030502007936号Copyright © 2021 College of Computer Science and Software Engineering, SZUTOP\n";
        System.out.println("=== 英文单词统计分析 ===\n");
        // 统计单词出现次数
        Map<String, Integer> wordCount = countWords(text);
        
        System.out.println("总共找到 " + wordCount.size() + " 个不同的单词");
        
        // 获取出现次数最多的前15个单词
        List<Map.Entry<String, Integer>> topWords = getTopWords(wordCount, 15);
        
        System.out.println("\n出现次数最多的15个单词:");
        System.out.println("排名\t单词\t\t出现次数");
        System.out.println("------------------------");
        for (int i = 0; i < topWords.size(); i++) {
            Map.Entry<String, Integer> entry = topWords.get(i);
            System.out.printf("%d\t%-15s\t%d\n", i+1, entry.getKey(), entry.getValue());
        }
    }
}