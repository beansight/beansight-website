package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import org.apache.commons.lang.RandomStringUtils;

import java.util.*;

@Entity
public class Promocode extends Model {

	public String code;
	public long nbUsageLeft;
	public Date crDate;
	public Date endDate;

	public Promocode(long nbUsage, Date deadline) {
		// Create a promocode
		this.code = Promocode.generateUniqueCode();
		this.nbUsageLeft = nbUsage;
		this.crDate = new Date();
		this.endDate = deadline;
	}
	
	public static Promocode findbyCode(String promocode) {
		return Promocode.find("byCode", promocode).first();
	}
	
	/** 
	 * generate a code and insure that the code has never been used in database, well you know ... unique ...
	 */
	public static String generateUniqueCode() {
		for (int i = 0; i<10; i++) {
			String randomCode = RandomStringUtils.randomAlphanumeric(6);
			if (Promocode.count("code = ?", randomCode) == 0) {
				return randomCode;
			}
		}
		throw new RuntimeException("Cannot generate a unique promocode : all generated codes already existed in database !!!");
	}
	
	public String toString() {
		return code;
	}
}
