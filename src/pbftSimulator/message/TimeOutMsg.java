package pbftSimulator.message;

public class TimeOutMsg extends Message {
	
	public int v;				
	
	public int n;		
	
	//消息结构
	//<TIMEOUT, v, n>:v表示视图编号;n表示序号;
	public TimeOutMsg(int v, int n, int sndId, int rcvId, long rcvtime) {
		super(sndId, rcvId, rcvtime);
		this.type = TIMEOUT;
		this.len = TIMMSGLEN;
		this.v = v;
		this.n = n;
	}
	
	public boolean equals(Object obj) {
        if (obj instanceof TimeOutMsg) {
        	TimeOutMsg msg = (TimeOutMsg) obj;
            return (v == msg.v && n == msg.n);
        }
        return super.equals(obj);
    }
        
    public int hashCode() {
        String str = "" + v + n;
        return str.hashCode();
    }
    
    public String toString() {
    	return super.toString() + "视图编号:"+v+";序号:"+n;
    }
}
