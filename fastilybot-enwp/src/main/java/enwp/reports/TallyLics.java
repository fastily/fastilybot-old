package enwp.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import okhttp3.HttpUrl;
import okhttp3.Request;
import util.BStrings;
import util.BotUtils;

/**
 * Counts up free license tags and checks if a Commons counterpart exists.
 * 
 * @author Fastily
 *
 */
public class TallyLics
{
	/**
	 * The Wiki objects to use
	 */
	private static Wiki enwp = BotUtils.getFastilyBot(), com = BotUtils.getCommons(enwp);

	/**
	 * The title to post the report to.
	 */
	private static String reportPage = String.format("User:%s/Free License Tags", enwp.whoami());

	/**
	 * The blacklist of pages to omit from the final report
	 */
	private static ArrayList<String> bl = enwp.getLinksOnPage(reportPage + "/Ignore", NS.TEMPLATE);

	/**
	 * A list of en.wikipedia free license templates
	 */
	protected static ArrayList<String> enwptpl = FL.toAL(enwp.getLinksOnPage(reportPage + "/Sources", NS.CATEGORY).stream()
			.flatMap(cat -> enwp.getCategoryMembers(cat, NS.TEMPLATE).stream()).filter(s -> !bl.contains(s) && !s.endsWith("/sandbox")));

	/**
	 * The list of Commons templates with the same name as the enwp templates.
	 */
	protected static HashSet<String> comtpl = new HashSet<>(MQuery.exists(com, true, enwptpl));

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Collections.sort(enwptpl);

		String dump = BStrings.updatedAt
				+ "\n{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;width:100%;\" \n! # !! Name !! Transclusions !! Commons? \n";

		int i = 0;
		for (String s : enwptpl)
			dump += String.format("|-%n|%d ||{{Tlx|%s}} || %d ||[[c:%s|%b]] %n", ++i, enwp.nss(s), tCount(enwp.nss(s)), s,
					comtpl.contains(s));

		dump += "|}";

		enwp.edit(reportPage, dump, "Updating report");
	}

	/**
	 * Use Jarry's {@code templatecount} tool to count the number of transclusions for a Template.
	 * 
	 * @param tp Count transclusions for this template, excluding {@code Template:} prefix.
	 * @return The number of transclusions, or {@code -1} on error.
	 */
	private static int tCount(String tp)
	{
		try
		{
			Matcher m = Pattern
					.compile(
							"(?<=\\<p\\>)\\d+(?= transclusion)")
					.matcher(BotUtils.httpClient.newCall(
							new Request.Builder().url(HttpUrl.parse("https://tools.wmflabs.org/templatecount/index.php?lang=en&namespace=10")
									.newBuilder().addQueryParameter("name", tp).build()).get().build())
							.execute().body().string());
			if (m.find())
			{
				int cnt = Integer.parseInt(m.group());				
				return 0 < cnt && cnt < 20 ? enwp.whatTranscludesHere(enwp.convertIfNotInNS(tp, NS.TEMPLATE), NS.FILE).size() : cnt;
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return -1;
	}
}