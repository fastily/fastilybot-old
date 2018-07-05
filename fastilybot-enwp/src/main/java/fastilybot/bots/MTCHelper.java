package fastilybot.bots;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastilybot.util.BStrings;
import fastilybot.util.BotUtils;
import fastilybot.util.WTP;

/**
 * Find and fix tags for files tagged for transfer to Commons which have already transferred.
 * 
 * @author Fastily
 *
 */
public final class MTCHelper
{
	/**
	 * Main driver
	 * 
	 * @param args Not used - program does not accept arguments
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();

		HashSet<String> l = BotUtils.fetchLabsReportAsFiles(wiki, 1);
		l.retainAll(WTP.mtc.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(WTP.keeplocal.getTransclusionSet(wiki, NS.FILE)); // lots of in-line tags

		String tRegex = WTP.mtc.getRegex(wiki);
		HashSet<String> ncdL = WTP.ncd.getTransclusionSet(wiki, NS.FILE);
		String ncdT = BStrings.ncdTemplateFor(wiki.whoami());

		BotUtils.getFirstOnlySharedDuplicate(wiki, new ArrayList<>(l)).forEach((k, v) -> {
			if (ncdL.contains(k))
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