package fastilybot;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.fastily.jwiki.core.Wiki;
import org.fastily.jwiki.util.FL;
import org.fastily.wptoolbox.Dates;
import org.fastily.wptoolbox.HTTP;

import com.google.gson.reflect.TypeToken;

/**
 * Shared static methods used by FastilyBot
 * 
 * @author Fastily
 *
 */
class BUtils
{
	/**
	 * The Type representation for a HashMap of [ String : String ].
	 */
	protected static final Type strStrHM = new TypeToken<HashMap<String, String>>() {
	}.getType();

	/**
	 * Constructors disallowed
	 */
	private BUtils()
	{

	}

	/**
	 * Fetch a simple, raw report from fastilybot's toollabs dumps.
	 * 
	 * @param wiki The Wiki object to use
	 * @param report The name of the report, without the {@code .txt} extension.
	 * @param prefix The prefix to add to each entry (usually a namespace prefix).
	 * @return A String Array with each item in the report, or the empty Array if something went wrong.
	 */
	public static HashSet<String> fetchLabsReportSet(Wiki wiki, String report, String prefix)
	{
		String body = HTTP.get(String.format("https://tools.wmflabs.org/fastilybot-reports/r/%s.txt", report));
		return body != null ? FL.toSet(body.lines().map(s -> prefix + s.replace('_', ' '))) : new HashSet<>();
	}

	/**
	 * Fetch a simple report from fastilybot's toollabs dumps where each entry is prefixed with {@code File:} and where underscores are replaced by spaces.
	 * 
	 * @param wiki The Wiki object to use
	 * @param rNum The report number to fetch.
	 * @return A HashSet with each item in the report, or the empty HashSet if something went wrong.
	 */
	public static HashSet<String> fetchLabsReportAsFiles(Wiki wiki, int rNum)
	{
		return fetchLabsReportSet(wiki, "report" + rNum, "File:");
	}

	/**
	 * Generates a Wiki-text ready, wiki-linked, unordered list from a list of titles.
	 * 
	 * @param header A header/lead string to apply at the beginning of the returned String.
	 * @param titles The titles to use
	 * @param doEscape Set as true to escape titles. i.e. adds a {@code :} before each link so that files and categories are properly escaped and appear as links.
	 * @return A String with the titles as a linked, unordered list, in Wiki-text.
	 */
	public static String listify(String header, Collection<String> titles, boolean doEscape)
	{
		String fmtStr = String.format("* [[%s%%s]]\n", doEscape ? ":" : "");

		StringBuilder x = new StringBuilder(header);
		for (String s : titles)
			x.append(String.format(fmtStr, s));

		return x.toString();
	}

	/**
	 * Generates a ZonedDateTime of now in UTC with h/m/s set to 0.
	 * 
	 * @return A ZonedDateTime of the currrent time in UTC with h/m/s set to 0.
	 */
	public static ZonedDateTime utcWithTodaysDate()
	{
		return Dates.getUTCofNow().truncatedTo(ChronoUnit.DAYS);
	}
}