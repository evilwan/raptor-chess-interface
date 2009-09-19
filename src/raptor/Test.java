package raptor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Test {
	public static final long TIMEZONE_OFFSET = -(Calendar.getInstance().get(Calendar.ZONE_OFFSET));
	
	public static void main(String args[]) {

		SimpleDateFormat format = new SimpleDateFormat(
				"HH:mm:ss.S");
		
		System.out.println(TIMEZONE_OFFSET);
		System.out.println(format.format(new Date(60000 * 3 + TIMEZONE_OFFSET)));

	}
}
