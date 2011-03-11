package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import models.User;

import play.Play;

public class UserCount implements Comparable {
	public User user;
	public Long count;

	public UserCount(User user, Long count) {
		this.user = user;
		this.count = count;
	}
	
	@Override
	public int compareTo(Object o) {
		return this.count.compareTo(((UserCount) o).count);
	}
}
