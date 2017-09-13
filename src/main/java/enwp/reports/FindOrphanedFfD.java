package enwp.reports;

import java.util.ArrayList;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;
import fastily.jwiki.core.MQuery;

/**
 * Lists pages tagged for FfD with no corresponding FfD page/entry.
 * 
 * @author Fastily
 *
 */
public class FindOrphanedFfD
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = BotUtils.getFastilyBot();

	/**
	 * The title to post the report to.
	 */
	private static String reportPage = String.format("User:%s/Orphaned FfD", wiki.whoami());

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		ArrayList<String> l = new ArrayList<>();
		MQuery.linksHere(wiki, false, wiki.whatTranscludesHere("Template:Ffd", NS.FILE)).forEach((k, v) -> {
			if (!v.stream().anyMatch(s -> s.startsWith("Wikipedia:Files for discussion")))
				l.add(k);
		});

		wiki.edit(reportPage, BotUtils.listify(BotUtils.updatedAt, l, true), String.format("Updating report (%d items)", l.size()));
	}
}