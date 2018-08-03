package pbftSimulator.message;

public class ReplyMsg extends Message {
	
	public int v;	
	
	public long t;
	
	public int c;			
	
	public int i;
	
	public String r;	
	
	//消息结构
	//<REPLY, v, t, c, i, r>:v表示视图编号;t表示客户端请求时间戳;c表示客户端id;i表示节点id;r表示处理返回结果
	public ReplyMsg(int v, long t, int c, int i, String r, int sndId, int rcvId, long rcvtime) {
		super(sndId, rcvId, rcvtime);
		this.type = REPLY;
		this.len = REPMSGLEN;
		this.v = v;
		this.t = t;
		this.c = c;
		this.i = i;
		this.r = r;
	}
	
	public boolean equals(Object obj) {
        if (obj instanceof ReplyMsg) {
        	ReplyMsg msg = (ReplyMsg) obj;
            return (v == msg.v && t == msg.t && c == msg.c && i == msg.i && r.equals(msg.r));
        }
        return super.equals(obj);
    }
        
    public int hashCode() {
        String str = "" + v + t + c + i + r;
        return str.hashCode();
    }
    
    public String toString() {
    	return super.toString() + "视图编号:"+v+";时间戳:"+t+";客户端编号:"+c;
    }
}