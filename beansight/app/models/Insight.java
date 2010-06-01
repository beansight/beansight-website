package models;

import java.util.List;

import siena.Id;
import siena.Model;

public class Insight extends Model {

	@Id
	public Long id;
	public String content;
	
	
	public static List<Insight> getAll() {
		return Insight.all(Insight.class).fetch();
	}
	
	
}
