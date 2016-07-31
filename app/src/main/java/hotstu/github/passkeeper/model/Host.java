package hotstu.github.passkeeper.model;

public class Host {
	private int _id;
	private String hostname;
	
	public Host(int _id, String hostname) {
		super();
		this._id = _id;
		this.hostname = hostname;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	

}
