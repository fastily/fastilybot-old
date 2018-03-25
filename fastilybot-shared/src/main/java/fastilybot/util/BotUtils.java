package fastilybot.util;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.FSystem;
import fastily.jwiki.util.Tuple;
import fastily.jwiki.util.WGen;
import okhttp3.OkHttpClient;
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
	 * Generic http client for miscellaneous use.
	 */
	public static OkHttpClient httpClient = new OkHttpClient.Builder().readTimeout(2, TimeUnit.MINUTES).build();

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
	public static Wiki getUserWP(String user)
	{
		return WGen.get(user, "en.wikipedia.org");
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
	 * @param prefix The prefix to add to each entry (usually a namespace prefix).
	 * @return A String Array with each item in the report, or the empty Array if something went wrong.
	 */
	public static HashSet<String> fetchLabsReportSet(Wiki wiki, String report, String prefix)
	{
		try
		{
			Response r = httpClient
					.newCall(
							new Request.Builder().url(String.format("https://tools.wmflabs.org/fastilybot/r/%s.txt", report)).get().build())
					.execute();

			if (r.isSuccessful())
				return FL.toSet(Arrays.stream(r.body().string().split("\n")).map(s -> prefix + s.replace('_', ' ')));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return new HashSet<>();
	}

	/**
	 * Fetch a simple report from fastilybot's toollabs dumps where each entry is prefixed with {@code File:} and where
	 * underscores are replaced by spaces.
	 * 
	 * @param wiki The Wiki object to use
	 * @param report The name of the report, without the {@code .txt} extension.
	 * @return A HashSet with each item in the report, or the empty HashSet if something went wrong.
	 */
	public static HashSet<String> fetchLabsReportAsFiles(Wiki wiki, String report)
	{
		return fetchLabsReportSet(wiki, report, "File:");
	}

	/**
	 * Determine if a set of link(s) has existed on a page over a given time period.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title to query
	 * @param l The list of link(s) to look for in the history of {@code title}.
	 * @param start The time to start looking at (inclusive). Optional - set null to disable.
	 * @param end The time to stop the search at (exclusive). Optional - set null to disable.
	 * @return A list of link(s) that were found at some point in the page's history.
	 */
	public static ArrayList<String> detLinksInHist(Wiki wiki, String title, ArrayList<String> l, Instant start, Instant end)
	{
		ArrayList<String> texts = FL.toAL(wiki.getRevisions(title, -1, false, start, end).stream().map(r -> r.text));
		return FL.toAL(l.stream().filter(s -> texts.stream().noneMatch(t -> t.matches("(?si).*?\\[\\[:??(\\Q" + s + "\\E)\\]\\].*?"))));
	}

	/**
	 * Recursively searches a category for members.
	 * 
	 * @param wiki The Wiki object to use
	 * @param root The root/parent category to start searching in
	 * @return A Tuple in the form: ( categories visited, members found )
	 */
	public static Tuple<HashSet<String>, HashSet<String>> getCategoryMembersR(Wiki wiki, String root)
	{
		HashSet<String> seen = new HashSet<>(), l = new HashSet<>();
		getCategoryMembersR(wiki, root, seen, l);

		return new Tuple<>(seen, l);
	}

	/**
	 * Recursively searches a category for members.
	 * 
	 * @param wiki The Wiki object to use
	 * @param root The root/parent category to start searching in
	 * @param seen Lists the categories visited. Tracking this avoids circular self-categorizing categories.
	 * @param l Lists the category members encountered.
	 */
	private static void getCategoryMembersR(Wiki wiki, String root, HashSet<String> seen, HashSet<String> l)
	{
		seen.add(root);

		ArrayList<String> results = wiki.getCategoryMembers(root);
		ArrayList<String> cats = wiki.filterByNS(results, NS.CATEGORY);

		results.removeAll(cats); // cats go in seen
		l.addAll(results);

		for (String s : cats)
		{
			if (seen.contains(s))
				continue;

			getCategoryMembersR(wiki, s, seen, l);
		}
	}

	/**
	 * Extracts a template from text.
	 * 
	 * @param p The template's Regex
	 * @param text The text to look for {@code p} in
	 * @return The template, or the empty string if nothing was found.
	 */
	public static String extractTemplate(Pattern p, String text) // TODO: Bad form, fix me.
	{
		Matcher m = p.matcher(text);
		return m.find() ? m.group() : "";
	}

	/**
	 * Generates a Wiki-text ready, wiki-linked, unordered list from a list of titles.
	 * 
	 * @param header A header/lead string to apply at the beginning of the returned String.
	 * @param titles The titles to use
	 * @param doEscape Set as true to escape titles. i.e. adds a {@code :} before each link so that files and categories
	 *           are properly escaped and appear as links.
	 * @return A String with the titles as a linked, unordered list, in Wiki-text.
	 */
	public static String listify(String header, Collection<String> titles, boolean doEscape)
	{
		String fmtStr = "* [[" + (doEscape ? ":" : "") + "%s]]" + FSystem.lsep;

		StringBuilder x = new StringBuilder(header);
		for (String s : titles)
			x.append(String.format(fmtStr, s));

		return x.toString();
	}

	/**
	 * Gets the first shared (non-local) duplicate for each file with a duplicate. Filters out files which do not have
	 * duplicates.
	 * 
	 * @param wiki The Wiki object to use
	 * @param titles The titles to get duplicates for
	 * @return A Map where each key is the original, and each value is the first duplicate found.
	 */
	public static HashMap<String, String> getFirstOnlySharedDuplicate(Wiki wiki, ArrayList<String> titles)
	{
		HashMap<String, String> l = new HashMap<>();
		MQuery.getSharedDuplicatesOf(wiki, titles).forEach((k, v) -> {
			if (!v.isEmpty())
				l.put(k, v.get(0));
		});

		return l;
	}
	
	/**
	 * Writes Collection of String to disk at {@code path}, delineated by newlines. If {@code path} exists, then it will
	 * be overwritten.
	 * 
	 * @param path The location on disk to write to
	 * @param l The Collection to use
	 * @throws Throwable Throwable IO Error
	 */
	public static void writeStringsToFile(Path path, Collection<String> l) throws Throwable
	{
		Files.write(path, l, CREATE, WRITE, TRUNCATE_EXISTING);
	}
}