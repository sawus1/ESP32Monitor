package SystemLoad;

public class SysProc {
	private int pid;
	private String name;
	private String state;
	private int ppid;
	private int threads;
	private long vmsize;
	private long vmrss;
	private long vmswap;
	
	public int getPid() { return pid; }
	public String getName() { return name; }
	public String getState() { return state; }
	public int getPpid() { return ppid; }
	public int getThreads() { return threads; }
	public long getVmsize() { return vmsize; }
	public long getVmrss() { return vmrss; }
	public long getVmswap() { return vmswap; }
}
