package pbftSimulator;

import java.util.HashMap;
import java.util.Map;

public class Status {
	
	public static final int Request = 1;
	
	public static final int Preprepare = 2;
	
	public static final int Prepare = 3;
	
	public static final int Commit = 4;
	
	public static final int Byzantine = 9;
	
	private int stage; 	//Request; Preprepare; Prepare; Commit; 

	private Map<Integer, Message> prepareVotes; 	//收集的prepare消息
	
	private Map<Integer, Message> commitVotes; 	//收集的commit消息
	
	private Map<Integer, Message> viewChgVotes; 	//收集的viewChange消息
	
	private Map<Integer, Message> newViewVotes; 	//收集的newView消息
	
	private int priId;	//主节点id
	
	private int seqId;	//消息处理序列id
	
	private long startTime;	//收到request的时间戳
	
	private long endTime;	//进入commit状态的时间戳

	public Status(int stage, int seqId, int priId, long startTime) {
		this.stage = stage;
		this.seqId = seqId;
		this.priId = priId;
		this.startTime = startTime;
		prepareVotes = new HashMap<>();
		commitVotes = new HashMap<>(); 
		viewChgVotes = new HashMap<>();
		newViewVotes = new HashMap<>();
	}
	
	public int addPrepareVote(int voteId) {
		prepareVotes.put(voteId, null);
		return prepareVotes.size();
	}
	
	public int addCommitVote(int voteId) {
		commitVotes.put(voteId, null);
		return commitVotes.size();
	}
	
	public int addViewChgVote(Message msg) {
		viewChgVotes.put(msg.getSndId(), msg);
		int seqId = msg.getSeqId();
		int priId = msg.getPriId();
		int cnt = 0;
		for(int i : viewChgVotes.keySet()) {
			Message m = viewChgVotes.get(i);
			if(m.getSeqId() == seqId && m.getPriId() == priId) {
				++cnt;
			}
		}
		return cnt;
	}
	
	public int addNewViewVote(int voteId) {
		newViewVotes.put(voteId, null);
		return newViewVotes.size();
	}
	
	public void reset(int seqId, int priId) {
		stage = Status.Request;
		this.seqId = seqId;
		this.priId = priId;
		prepareVotes.clear();
		commitVotes.clear();
		viewChgVotes.clear();
		newViewVotes.clear();
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
		if(stage == Commit) {
			prepareVotes.clear();
			commitVotes.clear();
			viewChgVotes.clear();
			newViewVotes.clear();
		}
	}

	public Map<Integer, Message> getPrepareVotes() {
		return prepareVotes;
	}

	public void setPrepareVotes(Map<Integer, Message> prepareVotes) {
		this.prepareVotes = prepareVotes;
	}

	public Map<Integer, Message> getCommitVotes() {
		return commitVotes;
	}

	public void setCommitVotes(Map<Integer, Message> commitVotes) {
		this.commitVotes = commitVotes;
	}

	public Map<Integer, Message> getViewChgVotes() {
		return viewChgVotes;
	}

	public void setViewChgVotes(Map<Integer, Message> viewChgVotes) {
		this.viewChgVotes = viewChgVotes;
	}

	public Map<Integer, Message> getNewViewVotes() {
		return newViewVotes;
	}

	public void setNewViewVotes(Map<Integer, Message> newViewVotes) {
		this.newViewVotes = newViewVotes;
	}

	public int getPriId() {
		return priId;
	}

	public void setPriId(int priId) {
		this.priId = priId;
	}

	public int getSeqId() {
		return seqId;
	}

	public void setSeqId(int seqId) {
		this.seqId = seqId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}
