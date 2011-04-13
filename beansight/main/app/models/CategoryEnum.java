package models;

public enum CategoryEnum {
	CELEBRITIES(1),
	ECONOMICS(2),
	FUN(3),
	POLITICS(4),
	SPORT(5),
	TECHNOLOGY(6),
	ENTERTAINEMENT(7),
	SOCIETY(8);
	
	private long id;
	
	private CategoryEnum(long id) {
		this.id = id;
	}
	
	public long getId() {
		return this.id;
	}
}
