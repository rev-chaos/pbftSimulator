package pbftSimulator;

public class Message {
	
	public static final int Request = 0;
	
	public static final int Preprepare = 1;
	
	public static final int Prepare = 2;
	
	public static final int Commit = 3;
	
	public static final int TimeOut = 4;
	
	public static final int ViewChg = 5;
	
	public static final int NewView = 6;
	
	//消息类型 Request, Preprepare, Prepare, Commit, ViewChange, NewView, Timeout
	private int type;				
	
	private String ctx;				//消息内容（唯一，可作为key）
	
	private int sndId;				//消息发送端id

	private int rcvId;  			//消息接收端id
	
	private int seqId;  			//消息处理序列
	
	private int priId;  			//leader id
	
	private long timestamp;  		//消息处理时间戳
	
	public Message(int type, String ctx, int sndId,
			int rcvId, int seqId, int priId, long timestamp) {
		this.type = type;
		this.ctx = ctx;
		this.sndId = sndId;
		this.rcvId = rcvId;
		this.seqId = seqId;
		this.priId = priId;
		this.timestamp = timestamp;
	}
	
	public void print(String tag) {
		if(!Settings.showDetailInfo) return;
		String prefix = "【"+tag+"】";
		String[] typeName = {"Request","Preprepare","Prepare","Commit","TimeOut","ViewChange","NewView"};
		System.out.println(prefix+"消息类型:"+typeName[type]+";消息内容:"
				+ctx+";发送者id:"+sndId+";接收者id:"+rcvId+";消息处理序列:"+seqId
				+";主节点id:"+priId+";消息接收时间戳:"+timestamp
				+";remainConfirms:"+Settings.remainConfirms);
	}
	
	public boolean equals(Message msg) {
		if(msg == null)
			return false;
		if(msg.ctx.equals(ctx) && msg.type == type 
				&& msg.priId == priId && msg.seqId == seqId
				&& msg.rcvId == rcvId && msg.sndId == sndId) {
			return true;
		}
		return false;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCtx() {
		return ctx;
	}

	public void setCtx(String ctx) {
		this.ctx = ctx;
	}

	public int getSndId() {
		return sndId;
	}

	public void setSndId(int sndId) {
		this.sndId = sndId;
	}

	public int getRcvId() {
		return rcvId;
	}

	public void setRcvId(int rcvId) {
		this.rcvId = rcvId;
	}

	public int getSeqId() {
		return seqId;
	}

	public void setSeqId(int seqId) {
		this.seqId = seqId;
	}

	public int getPriId() {
		return priId;
	}

	public void setPriId(int priId) {
		this.priId = priId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
