package fastilybot.reports;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.wptoolbox.BotUtils;
import fastilybot.shared.Settings;

/**
 * Finds broken SPI pages on enwp.
 * 
 * @author Fastily
 *
 */
public final class BrokenSPI
{
	/**
	 * Main driver
	 * 
	 * @param args N/A
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastilyBot();
		String report = "Wikipedia:Sockpuppet investigations/SPI/Malformed Cases Report";
		
		HashSet<String> spiCases = FL.toSet(wiki.prefixIndex(NS.PROJECT, "Sockpuppet investigations/").stream()
				.filter(s -> !(s.endsWith("/Archive") || s.startsWith("Wikipedia:Sockpuppet investigations/SPI/"))));

		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI case status", NS.PROJECT));
		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI archive notice", NS.PROJECT));
		spiCases.removeAll(wiki.getLinksOnPage(report + "/Ignore"));

		ArrayList<String> l = new ArrayList<>();
		MQuery.resolveRedirects(wiki, spiCases).forEach((k, v) -> {
			if (k.equals(v)) // filter redirects
				l.add(v);
		});

		wiki.edit(report, BotUtils.listify("{{/Header}}\n" + Settings.updatedAt, l, false),
				String.format("BOT: Update list (%d items)", l.size()));
	}
}