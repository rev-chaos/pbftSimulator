package pbftSimulator.message;

import java.util.Set;

public class NewViewMsg extends Message {
	
	public int v;				
	
	public Set<Message> V;
	
	public Set<Message> O;
	
	public Set<Message> N;
	
	public int i;
	
	//消息结构
	//<NEWVIEW, v, V, O, N, i>:v表示视图编号;V表示viewChange消息集合;
	//O表示prePrepare消息集合;N表示prePrepare消息集合;i表示节点id
	public NewViewMsg(int v, Set<Message> V, Set<Message> O, Set<Message> N, 
			int i, int sndId, int rcvId, long rcvtime) {
		super(sndId, rcvId, rcvtime);
		this.type = NEWVIEW;
		this.len = NEVMSGBASELEN + accumulateLen(V) + accumulateLen(O) + accumulateLen(N);
		this.v = v;
		this.V = V;
		this.O = O;
		this.N = N;
		this.i = i;
	}
	
	public Message copy(int rcvId, long rcvtime) {
		//V O N是浅复制，不过没有关系，不会修改它们的值
		return new NewViewMsg(v, V, O, N, i, sndId, rcvId, rcvtime);
	}
	
	public boolean equals(Object obj) {
        if (obj instanceof NewViewMsg) {
        	NewViewMsg msg = (NewViewMsg) obj;
            return (v == msg.v && i == msg.i);
        }
        return super.equals(obj);
    }
        
    public int hashCode() {
        String str = "" + v + i;
        return str.hashCode();
    }
    
    public String toString() {
    	return super.toString() + "视图编号:"+v;
    }
}
