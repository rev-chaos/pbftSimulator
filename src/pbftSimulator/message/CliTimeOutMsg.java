package pbftSimulator.message;

public class CliTimeOutMsg extends Message {
	
	//消息结构
	public long t;
	//<CLITIMEOUT, t>: t表示request请求时间戳
	public CliTimeOutMsg(long t, int sndId, int rcvId, long rcvtime) {
		super(sndId, rcvId, rcvtime);
		this.type = CLITIMEOUT;
		this.len = CLTMSGLEN;
		this.t = t;
	}
	
	public boolean equals(Object obj) {
        if (obj instanceof CliTimeOutMsg) {
        	CliTimeOutMsg msg = (CliTimeOutMsg) obj;
            return (t == msg.t);
        }
        return super.equals(obj);
    }
        
    public int hashCode() {
        String str = "" + t;
        return str.hashCode();
    }
	
    public String toString() {
    	return super.toString() + "请求时间戳:"+t;
    }

}
