package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.annotations.Index;

import java.util.*;

@Entity
public class ForgotPassword extends Model {

	@Index (name = "FORGOT_PWD_CODE_IDX")
	public String code;
	public String email;
	public Date crDate;
	
	public ForgotPassword(String email) {
		super();
		this.code = RandomStringUtils.randomAlphanumeric(6);
		this.email = email;
		this.crDate = new Date();
	}
	
}
