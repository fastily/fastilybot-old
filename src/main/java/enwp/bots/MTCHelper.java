package enwp.bots;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.wpkit.text.WPStrings;
import fastily.wpkit.text.WTP;
import fastily.wpkit.util.Toolbox;
import fastily.wpkit.util.WikiX;

/**
 * Find and fix tags for files tagged for transfer to Commons which have already transferred.
 * 
 * @author Fastily
 *
 */
public final class MTCHelper
{
	/**
	 * The Wiki to use
	 */
	private static Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * Creates the regular expression matching Copy to Wikimedia Commons
	 */
	private static String tRegex = WTP.mtc.getRegex(wiki);

	/**
	 * The list of pages transcluding {@code Template:Now Commons}
	 */
	private static HashSet<String> nowCommons = WTP.ncd.getTransclusionSet(wiki, NS.FILE);

	/**
	 * The ncd template to fill out
	 */
	private static String ncdT = WPStrings.makeNCDBotTemplate(wiki.whoami());

	/**
	 * Main driver
	 * 
	 * @param args Not used - program does not accept arguments
	 */
	public static void main(String[] args)
	{
		HashSet<String> l = Toolbox.fetchLabsReportListAsFiles(wiki, "report1");
		l.retainAll(WTP.mtc.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(WTP.keeplocal.getTransclusionSet(wiki, NS.FILE)); // lots of in-line tags

		WikiX.getFirstOnlySharedDuplicate(wiki, new ArrayList<>(l)).forEach((k, v) -> {
			if (nowCommons.contains(k))
				wiki.replaceText(k, tRegex, "BOT: File has already been copied to Commons");
			else
			{
				String oText = wiki.getPageText(k);
				String nText = oText.replaceAll(tRegex, "");
				if (oText.equals(nText)) // avoid in-line tags
					return;

				wiki.edit(k, String.format(ncdT, v) + nText, "BOT: File is available on Commons");
			}
		});
	}
}