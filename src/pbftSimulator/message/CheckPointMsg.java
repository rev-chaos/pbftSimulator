package pbftSimulator.message;

import java.util.Map;

public class CheckPointMsg extends Message{
	
	public int v;				
	
	public int n;			
	
	public Map<Integer, LastReply> s;	
	
	public int i;
	
	//消息结构
	//<CHECKPOINT, v, n, s, i>:v表示视图编号;n表示序列号;s表示lastReply集合;i表示节点id
	public CheckPointMsg(int v, int n, Map<Integer, LastReply> s, int i, int sndId, int rcvId, long rcvtime) {
		super(sndId, rcvId, rcvtime);
		long appendLen = s == null ? 0L : s.size() * LASTREPLEN;
		this.type = CHECKPOINT;
		this.len = CKPMSGBASELEN + appendLen;
		this.v = v;
		this.n = n;
		this.s = s;
		this.i = i;
	}
	
	public Message copy(int rcvId, long rcvtime) {
		//s是浅复制，不过没有关系，不会修改s的值
		return new CheckPointMsg(v, n, s, i, sndId, rcvId, rcvtime);
	}
	
	public boolean equals(Object obj) {
        if (obj instanceof CheckPointMsg) {
        	CheckPointMsg msg = (CheckPointMsg) obj;
            return (v == msg.v && n == msg.n && i == msg.i);
        }
        return super.equals(obj);
    }
        
    public int hashCode() {
        String str = "" + v + n + i;
        return str.hashCode();
    }
    
    public String toString() {
    	return super.toString() + "视图编号:"+v+";序列号:"+n;
    }
}
