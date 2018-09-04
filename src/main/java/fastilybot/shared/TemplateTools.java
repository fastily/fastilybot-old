package fastilybot.shared;

import java.time.format.DateTimeFormatter;

import fastily.wptoolbox.DateUtils;

/**
 * Template-related functions for use with fastilybot
 * 
 * @author Fastily
 *
 */
public class TemplateTools
{
	/**
	 * Generates an {@code Template:Ncd} template for a bot user.
	 * 
	 * @param user The bot username to use
	 * @return The template.
	 */
	public static String ncdTemplateFor(String user)
	{
		return String.format("{{Now Commons|%%s|date=%s|bot=%s}}%n", DateTimeFormatter.ISO_LOCAL_DATE.format(DateUtils.getUTCofNow()),
				user);
	} // TODO: can date be stored globally?
}
