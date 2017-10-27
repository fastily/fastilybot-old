package enwp.bots;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;
import util.WTP;

/**
 * Finds files on enwp nominated for deletion on Commons and flags the local file.
 * 
 * @author Fastily
 *
 */
public class FindCommonsFFD
{
	/**
	 * Main driver
	 * 
	 * @param args Program args, not used
	 */
	public static void main(String[] args)
	{
		Wiki enwp = BotUtils.getFastilyBot();
		String ncRegex = WTP.ncd.getRegex(enwp);
		
		HashSet<String> fl = new HashSet<>(BotUtils.getCommons(enwp).whatTranscludesHere("Template:Deletion template tag", NS.FILE));

		BotUtils.getFirstOnlySharedDuplicate(enwp, enwp.whatTranscludesHere(WTP.ncd.title, NS.FILE)).forEach((k, v) -> {
			if (fl.contains(enwp.convertIfNotInNS(v, NS.FILE)))
				enwp.replaceText(k, ncRegex, String.format("{{Nominated for deletion on Commons|%s}}", enwp.nss(v)),
						"BOT: File is up for deletion on Commons");
		});
	}
}