package pbftSimulator.message;

public class LastReply {
	
	public int c;
	
	public long t;
	
	public String r;
	
	public LastReply(int c, long t, String r) {
		this.c = c;
		this.t = t;
		this.r = r;
	}
	
	public boolean equals(Object obj) {
        if (obj instanceof LastReply) {
        	LastReply lr = (LastReply) obj;
            return (c == lr.c && t == lr.t && r.equals(lr.r));
        }
        return super.equals(obj);
    }
}
