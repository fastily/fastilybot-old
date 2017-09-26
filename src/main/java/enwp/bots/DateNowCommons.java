package enwp.bots;

import java.util.ArrayList;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;
import util.WTP;

/**
 * Fills in date parameter (and other missing parameters) for files in [[Category:Wikipedia files with the same name on
 * Wikimedia Commons as of unknown date]].
 * 
 * @author Fastily
 *
 */
public class DateNowCommons
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		
		ArrayList<String> l = wiki
				.getCategoryMembers("Category:Wikipedia files with the same name on Wikimedia Commons as of unknown date", NS.FILE);
		l.removeAll(BotUtils.getCategoryMembersR(wiki, "Category:Wikipedia files reviewed on Wikimedia Commons").y);

		String ncRegex = WTP.ncd.getRegex(wiki);
		for (String s : l)
			wiki.replaceText(s, ncRegex, "{{Subst:Ncd}}", "BOT: Dating {{Now Commons}}");
	}
}