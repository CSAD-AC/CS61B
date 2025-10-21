package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/**
 * 操作历史记录类，用于支持undo功能
 * 记录每一步操作的详细信息，以便能够撤销操作
 */
public class OperationHistory implements Serializable {
    /**
     * 操作类型枚举
     */
    public enum OperationType {
        ADD, COMMIT, RESET, BRANCH, CHECKOUT, RM, RM_BRANCH, MERGE, NONE
    }

    /**
     * 单个操作记录
     */
    public static class OperationRecord implements Serializable {
        private OperationType type;
        private Map<String, Object> parameters;
        private Object data; // 用于存储操作相关的数据
        private Date timestamp;

        public OperationRecord(OperationType type, Map<String, Object> parameters, Object data) {
            this.type = type;
            this.parameters = parameters;
            this.data = data;
            this.timestamp = new Date();
        }

        public OperationType getType() {
            return type;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public Object getData() {
            return data;
        }

        public Date getTimestamp() {
            return timestamp;
        }
    }

    private static final File OPERATION_HISTORY_FILE = join(Repository.GITLET_DIR, "operation_history");
    
    private Deque<OperationRecord> history;
    
    public OperationHistory() {
        this.history = new ArrayDeque<>();
    }
    
    /**
     * 记录操作
     */
    public void recordOperation(OperationType type, Map<String, Object> parameters, Object data) {
        OperationRecord record = new OperationRecord(type, parameters, data);
        history.push(record);
        save();
    }

    public void invalidOperation() {
        OperationRecord record = new OperationRecord(OperationType.NONE, null, null);
        history.push(record);
        save();
    }
    

    /**
     * 获取最后一次操作记录
     */
    public OperationRecord getLastOperation() {
        if (history.isEmpty()) {
            return null;
        }
        return history.peek();
    }
    
    /**
     * 移除最后一次操作记录（用于撤销后）
     */
    public OperationRecord removeLastOperation() {
        if (history.isEmpty()) {
            return null;
        }
        OperationRecord record = history.pop();
        save();
        return record;
    }
    
    /**
     * 保存操作历史到文件
     */
    public void save() {
        if (Repository.GITLET_DIR.exists()) {
            writeObject(OPERATION_HISTORY_FILE, this);
        }
    }
    
    /**
     * 从文件加载操作历史
     */
    public static OperationHistory load() {
        if (OPERATION_HISTORY_FILE.exists()) {
            return readObject(OPERATION_HISTORY_FILE, OperationHistory.class);
        }
        return new OperationHistory();
    }
}