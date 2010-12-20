package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Promocode extends Model {

	public String code;
	public long nbUsageLeft;
	public Date crDate;
	public Date endDate;

	public Promocode(String code, long nbUsage, Date deadline) {
		this.code = code;
		this.nbUsageLeft = nbUsage;
		this.crDate = new Date();
		this.endDate = deadline;
	}
	
	public String toString() {
		return code;
	}
}
