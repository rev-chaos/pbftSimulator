package pbftSimulator.message;

import java.util.Map;
import java.util.Set;

public class ViewChangeMsg extends Message {
	
	public int v;				
	
	public int sn;	
	
	public Map<Integer, LastReply> ss;	
	
	public Set<Message> C;
	
	public Map<Integer, Set<Message>> P; 
	
	public int i;
	
	//消息结构
	//<VIEWCHANGE, v, sn, ss, C, P, i>:v表示视图编号;sn表示稳定状态的序列号;
	//ss表示稳定状态的lastReply集合 ;C表示checkpoint消息集合;
	//P表示n>sn的prePrepare消息集合;i表示节点id
	public ViewChangeMsg(int v, int sn, Map<Integer, LastReply> ss, 
			Set<Message> C, Map<Integer, Set<Message>> P, int i, int sndId, int rcvId, long rcvtime) {
		super(sndId, rcvId, rcvtime);
		long sLen = ss == null ? 0 : ss.size() * LASTREPLEN;
		this.type = VIEWCHANGE;
		this.len = VCHMSGBASELEN + sLen + accumulateLen(C) + accumulateLen(P);
		this.v = v;
		this.sn = sn;
		this.ss = ss;
		this.C = C;
		this.P = P;
		this.i = i;
	}
	
	public Message copy(int rcvId, long rcvtime) {
		//ss, C, P是浅复制，不过没有关系，不会修改它们的值
		return new ViewChangeMsg(v, sn, ss, C, P, i, sndId, rcvId, rcvtime);
	}
	
	public boolean equals(Object obj) {
        if (obj instanceof ViewChangeMsg) {
        	ViewChangeMsg msg = (ViewChangeMsg) obj;
            return (v == msg.v && sn == msg.sn && i == msg.i);
        }
        return super.equals(obj);
    }
        
    public int hashCode() {
        String str = "" + v + sn + i;
        return str.hashCode();
    }
    
    public String toString() {
    	return super.toString() + "视图编号:"+v+";稳定点序列号:"+sn;
    }
}
