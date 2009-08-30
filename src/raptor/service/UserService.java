package raptor.service;

public class UserService {

	private static final UserService instance = new UserService();
	private boolean isGuest;
	private String name;
	private String partner;

	public static UserService getInstance() {
		return instance;
	}

	public boolean isGuest() {
		return isGuest;
	}

	public void setGuest(boolean isGuest) {
		this.isGuest = isGuest;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}
}
