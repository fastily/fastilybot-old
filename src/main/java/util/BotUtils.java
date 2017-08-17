package util;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.wpkit.util.WikiGen;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Static functions specific to fastilybot and tools.
 * 
 * @author Fastily
 *
 */
public class BotUtils
{
	/**
	 * Used as part of report headers.
	 */
	public static final String updatedAt = "This report updated at ~~~~~\n";

	/**
	 * Wiki-text message stating that a bot did not nominate any files for deletion.
	 */
	public static final String botNote = "\n{{subst:User:FastilyBot/BotNote}}";

	/**
	 * Summary for speedy deletion criterion g8 - talk page
	 */
	public static final String csdG8talk = "[[WP:CSD#G8|G8]]: [[Help:Talk page|Talk page]] of a deleted or non-existent page";

	/**
	 * Constructors disallowed
	 */
	private BotUtils()
	{

	}

	/**
	 * Gets the specified WikiGen user at en.wikipedia.org.
	 * 
	 * @param user The user to get a Wiki object for
	 * @return A Wiki object, or null on error
	 */
	private static Wiki getUserWP(String user)
	{
		return WikiGen.wg.get(user, "en.wikipedia.org");
	}

	/**
	 * Gets the specified user on Commons.
	 * 
	 * @param user The username to get
	 * @return A Wiki object, or null on error.
	 */
	public static Wiki getUserCOM(String user)
	{
		return WikiGen.wg.get(user, "commons.wikimedia.org");
	}

	/**
	 * Gets a Wiki (from WikiGen) for Fastily at en.wikipedia.org.
	 * 
	 * @return A Wiki object, or null on error
	 */
	public static Wiki getFastily()
	{
		return getUserWP("Fastily");
	}

	/**
	 * Gets a Wiki (from WikiGen) for FSock at en.wikipedia.org.
	 * 
	 * @return A Wiki object, or null on error
	 */
	public static Wiki getFSock()
	{
		return getUserWP("FSock");
	}

	/**
	 * Gets a Wiki (from WikiGen) for FastilyBot at en.wikipedia.org.
	 * 
	 * @return A Wiki object, or null on error
	 */
	public static Wiki getFastilyBot()
	{
		return getUserWP("FastilyBot");
	}

	/**
	 * Derives a Wiki from {@code wiki} with the domain set to {@code commons.wikimedia.org}.
	 * 
	 * @param wiki The Wiki object to derive a new Commons Wiki from.
	 * @return A Wiki pointing to Commons, or null on error.
	 */
	public static Wiki getCommons(Wiki wiki)
	{
		return wiki.getWiki("commons.wikimedia.org");
	}

	/**
	 * Fetch a simple, raw report from fastilybot's toollabs dumps.
	 * 
	 * @param wiki The Wiki object to use
	 * @param report The name of the report, without the {@code .txt} extension.
	 * @return A String Array with each item in the report, or the empty Array if something went wrong.
	 */
	public static String[] fetchLabsReportList(Wiki wiki, String report)
	{
		try
		{
			Response r = wiki.apiclient.client
					.newCall(
							new Request.Builder().url(String.format("https://tools.wmflabs.org/fastilybot/r/%s.txt", report)).get().build())
					.execute();

			if (r.isSuccessful())
				return r.body().string().split("\n");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return new String[0];
	}

	/**
	 * Fetch a simple report from fastilybot's toollabs dumps where each entry is prefixed with {@code prefix} and where underscores are replaced by spaces.
	 * 
	 * @param wiki The Wiki object to use
	 * @param report The name of the report, without the {@code .txt} extension.
	 * @param prefix The prefix to add to each entry (usually a namespace prefix).
	 * @return A HashSet where each item is an entry in the report.
	 */
	public static HashSet<String> fetchLabsReportList(Wiki wiki, String report, String prefix)
	{
		return FL.toSet(Arrays.stream(fetchLabsReportList(wiki, report)).map(s -> prefix + s.replace('_', ' ')));
	}

	/**
	 * Fetch a simple report from fastilybot's toollabs dumps where each entry is prefixed with {@code File:} and where underscores are replaced by spaces.
	 * 
	 * @param wiki The Wiki object to use
	 * @param report The name of the report, without the {@code .txt} extension.
	 * @return A HashSet with each item in the report, or the empty HashSet if something went wrong.
	 */
	public static HashSet<String> fetchLabsReportListAsFiles(Wiki wiki, String report)
	{
		return fetchLabsReportList(wiki, report, "File:");
	}
	
	/**
	 * Generates an {@code Template:Ncd} template for a bot user.
	 * 
	 * @param user The bot username to use
	 * @return The template.
	 */
	public static String makeNCDBotTemplate(String user) // TODO: Out of place?
	{
		return String.format("{{Now Commons|%%s|date=%s|bot=%s}}%n", DateTimeFormatter.ISO_LOCAL_DATE.format(DateUtils.getUTCofNow()),
				user);
	}

	/**
	 * Removes a List from a HashSet. Copies a List into a HashSet and then removes it from {@code l}
	 * 
	 * @param l The HashSet to remove elements contained in {@code toRemove} from
	 * @param toRemove The List of items to remove from {@code l}
	 */
	public static void removeListFromHS(HashSet<String> l, List<String> toRemove)
	{
		l.removeAll(new HashSet<>(toRemove));
	}
}