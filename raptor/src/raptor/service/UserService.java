package raptor.service;

public class UserService {

	private static final UserService instance = new UserService();

	public static UserService getInstance() {
		return instance;
	}

	private boolean isGuest;
	private String name;

	private String partner;

	public String getName() {
		return name;
	}

	public String getPartner() {
		return partner;
	}

	public boolean isGuest() {
		return isGuest;
	}

	public void setGuest(boolean isGuest) {
		this.isGuest = isGuest;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}
}
