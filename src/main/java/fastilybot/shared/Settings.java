package fastilybot.shared;

/**
 * Static constants for use in fastilybot
 * 
 * @author Fastily
 *
 */
public class Settings
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
	 * Constructors disallowed
	 */
	private Settings()
	{

	}
}
