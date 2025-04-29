package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.Map;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author 逐辰
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    private String parent1ID;       // 第一个父提交的哈希
    private String parent2ID;       // 第二个父提交的哈希（用于合并提交）
    private Date timestamp;        // 提交时间戳
    private String message;        // 提交信息
    private Map<String, String> fileToBlobID; // 文件名 → Blob哈希的映射
    private String id;             // 基于内容生成的SHA-1哈希

    // 普通提交的构造函数
    public Commit(String parent1ID, String message, Map<String, String> fileToBlobID) {
        this(parent1ID, null, new Date(), message, fileToBlobID);
    }
    // 合并提交的构造函数
    public Commit(String parent1ID, String parent2ID, Date timestamp,String message, Map<String, String> fileToBlobID) {
        this.parent1ID = parent1ID;
        this.parent2ID = parent2ID;
        this.timestamp = timestamp;
        this.message = message;
        this.fileToBlobID = new HashMap<>(fileToBlobID);
        this.id = generateID(); // 生成唯一ID
    }

    private String generateID() {
        // 序列化元数据、父提交、文件映射等内容，计算SHA-1哈希
        return Utils.sha1(parent1ID + parent2ID + timestamp.toString() + message + fileToBlobID.toString());
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getFileToBlobID() {
        return fileToBlobID;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getParent2ID() {
        return parent2ID;
    }

    public String getParent1ID() {
        return parent1ID;
    }
}
