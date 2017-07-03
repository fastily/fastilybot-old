package tools;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.Triple;
import fastily.wpkit.util.Toolbox;
import fastily.wpkit.util.WikiX;

/**
 * Dumb utility which carries out actual deletions of uncontested FfD nominations. Be sure to close any discussions
 * which are requesting copyright review or revision deletions first.
 * 
 * @author Fastily
 *
 */
public class SimpleFFD
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki = Toolbox.getFastily();

	/**
	 * The root page of Files for Discussion
	 */
	private static final String ffdPrefix = "Wikipedia:Files for discussion/";

	/**
	 * String format to use for the deletion reason
	 */
	private static final String ffdLinkTP = "[[%s#%s]]";

	/**
	 * Matches the tail end of a user's time-stamped signature
	 */
	private static final Pattern tsRegex = Pattern.compile("\\d{4} \\(UTC\\)");
	
	/**
	 * Main driver.
	 * 
	 * @param args One argument, the date of the listing to process. e.g. {@code 2017 January 15}.
	 */
	public static void main(String[] args)
	{
		String targetPage;
		if(args.length > 0)
			 targetPage = ffdPrefix + args[0];
		else
			targetPage = ffdPrefix + Toolbox.dateAsYMD(Toolbox.getUTCofNow().minusDays(8));
		
		ArrayList<Triple<Integer, String, Integer>> l = wiki.getSectionHeaders(targetPage);
		l.removeIf(t -> t.x != 4);

		MQuery.exists(wiki, true,
				FL.toAL(WikiX.listPageSections(l, wiki.getPageText(targetPage)).stream()
						.filter(t -> checkText(t.z) && wiki.whichNS(t.y).equals(NS.FILE)).map(t -> t.y)))
				.stream().forEach(s -> wiki.delete(s, String.format(ffdLinkTP, targetPage, s)));
	}

	/**
	 * Determines if multiple users have edited a section or if the section was created by the user closing the
	 * discussion.
	 * 
	 * @param text The text of the section to inspect.
	 * @return True if there is nothing obvious in the section text to indicate the closure should not be executed.
	 */
	private static boolean checkText(String text)
	{
		Matcher m = tsRegex.matcher(text);
		int i = 0;
		while (m.find())
			if (++i > 1)
				return false;

		return !text.contains(wiki.whoami());
	}
}