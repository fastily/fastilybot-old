package util;

import java.time.format.DateTimeFormatter;

/**
 * Commonly used Strings in my bots/tools.
 * 
 * @author Fastily
 *
 */
public class BStrings
{
	/**
	 * Used as part of report headers.
	 */
	public static final String updatedAt = "This report updated at <onlyinclude>~~~~~</onlyinclude>\n";
	
	/**
	 * Wiki-text message stating that a bot did not nominate any files for deletion.
	 */
	public static final String botNote = "\n{{subst:User:FastilyBot/BotNote}}";
	
	/**
	 * Summary for speedy deletion criterion G8 (talk page)
	 */
	public static final String g8Talk = "[[WP:CSD#G8|G8]]: [[Help:Talk page|Talk page]] of a deleted or non-existent page";
	
	/**
	 * Summary for speedy deletion criterion G6
	 */
	public static final String g6 = "[[WP:CSD#G6|G6]]: Housekeeping and routine (non-controversial) cleanup";

	/**
	 * Constructors disallowed
	 */
	private BStrings()
	{

	}

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
	}
}