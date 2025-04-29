package gitlet;

import java.io.Serializable;

public class Blob implements Serializable {
    private final byte[] content;   // 仅存储内容
    private final String ID;        // 仅基于内容的哈希

    public Blob(byte[] content) {
        this.content = content;
        this.ID = Utils.sha1(content);
    }

    public String getID() {
        return ID;
    }

    public Object getContent() {
        return content;
    }
}
