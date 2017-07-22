package tools;

import java.util.ArrayList;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;
import util.DateUtils;

/**
 * Delete orphaned non-free files from the specified dated category.
 * 
 * @author Fastily
 *
 */
public final class OrphanedFU
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki = BotUtils.getFastily();

	/**
	 * Main driver.
	 * 
	 * @param args Accepts one argument, the dated category to process. ex: {@code 15 January 2017 }.
	 */
	public static void main(String[] args)
	{
		String cat = "Category:Orphaned non-free use Wikipedia files as of ";

		if (args.length > 0)
			cat += args[0];
		else
			cat += DateUtils.dateAsDMY(DateUtils.getUTCofNow().minusDays(8));

		ArrayList<String> ftl = new ArrayList<>();
		MQuery.fileUsage(wiki, wiki.getCategoryMembers(cat, NS.FILE)).forEach((k, v) -> {
			if (v.isEmpty() && wiki.delete(k, "[[WP:CSD#F5|F5]]: Unused non-free media file for more than 7 days"))
				ftl.add(wiki.convertIfNotInNS(wiki.nss(k), NS.FILE_TALK));
		});

		MQuery.exists(wiki, true, ftl)
				.forEach(s -> wiki.delete(s, BotUtils.csdG8talk));

		if (wiki.getCategorySize(cat) == 0)
			wiki.delete(cat, "[[WP:CSD#G6|G6]]: Housekeeping and routine (non-controversial) cleanup");
	}
}