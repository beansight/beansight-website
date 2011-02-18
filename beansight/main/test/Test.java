import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Test {

	
	public static void main(String[] args) {
		Pattern p = Pattern.compile("(@[\\w]+)");
		Matcher m = p.matcher("@jerome je ne suis pas d'accord tu devrais écouter @john quand il dit que blabla bla");
		if( m.find()) {
			System.out.println("trouvé");
		   for(int i= 0; i<= m.groupCount(); ++i)
		      System.out.println("\n"+"groupe "+i+" :"+m.group(i));
		}
	}
}
