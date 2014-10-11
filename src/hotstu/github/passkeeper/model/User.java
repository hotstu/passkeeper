package hotstu.github.passkeeper.model;

public class User {
	private int _id;
	private String username;
	private int pwdLength;
	private int hostId;
	
	public User(int _id, String username, int pwdLength, int hostId) {
		super();
		this._id = _id;
		this.username = username;
		this.pwdLength = pwdLength;
		this.hostId = hostId;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getPwdLength() {
		return pwdLength;
	}

	public void setPwdLength(int pwdLength) {
		this.pwdLength = pwdLength;
	}

	public int getHostId() {
		return hostId;
	}

	public void setHostId(int hostId) {
		this.hostId = hostId;
	}
	
	

}
