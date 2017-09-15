package tools;

import java.util.ArrayList;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.tp.WParser;
import fastily.jwiki.tp.WTemplate;
import fastily.wpkit.text.WTP;
import util.BotUtils;
import util.DateUtils;

/**
 * Deletes the current day's PROD'd files. CAVEAT: Be sure to check each file before using.
 * 
 * @author Fastily
 *
 */
public class Fprod
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki = BotUtils.getFastily();

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		ArrayList<String> ftl = new ArrayList<>();
		String fprodTP = wiki.nss(WTP.fprod.title);
		
		ArrayList<String> pages = wiki.getCategoryMembers(
				"Category:Proposed deletion as of " + DateUtils.dateAsDMY(DateUtils.getUTCofNow().minusDays(8)), NS.FILE);
		pages.removeAll(WTP.ffd.getTransclusionSet(wiki, NS.FILE));

		MQuery.getPageText(wiki, pages).forEach((k, v) -> {
			try
			{
				String s = null;
				for (WTemplate wt : WParser.parseText(wiki, v).getTemplatesR())
					if (wt.title.equals(fprodTP))
					{
						s = wt.get("concern").toString();
						break;
					}

				if (s != null && !s.isEmpty() && wiki.delete(k, "Expired [[WP:PROD|PROD]], concern was: " + s))
					ftl.add(wiki.convertIfNotInNS(k, NS.FILE_TALK));
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		});

		for (String s : MQuery.exists(wiki, true, ftl))
			wiki.delete(s, BotUtils.csdG8talk);
	}
}