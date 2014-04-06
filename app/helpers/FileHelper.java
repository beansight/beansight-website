package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import play.Play;

public class FileHelper {

	public static File getTmpFile(InputStream is) throws IOException {
		File f = new File(Play.getFile("tmp") + "/" + System.currentTimeMillis());
		OutputStream out = null;
		try {
			out = new FileOutputStream(f);
			byte buf[] = new byte[1024];
			int len;
			while ((len = is.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			if (out != null) {
				out.close();
			}
			is.close();
		}
		
		return f;
	}
}
