package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.wpkit.text.WPStrings;
import fastily.wpkit.util.Toolbox;

/**
 * Finds broken SPI pages on enwp.
 * 
 * @author Fastily
 *
 */
public final class FindBrokenSPI
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * The title to post reports on
	 */
	private static final String report = "Wikipedia:Sockpuppet investigations/SPI/Malformed Cases Report";

	/**
	 * Main driver
	 * 
	 * @param args N/A
	 */
	public static void main(String[] args)
	{
		HashSet<String> spiCases = FL.toSet(wiki.prefixIndex(NS.PROJECT, "Sockpuppet investigations/").stream()
				.filter(s -> !(s.endsWith("/Archive") || s.startsWith("Wikipedia:Sockpuppet investigations/SPI/"))));

		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI case status", NS.PROJECT));
		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI archive notice", NS.PROJECT));
		spiCases.removeAll(wiki.getLinksOnPage(report + "/Ignore"));

		ArrayList<String> l = new ArrayList<>();
		MQuery.resolveRedirects(wiki, new ArrayList<>(spiCases)).forEach((k, v) -> {
			if (k.equals(v)) // filter redirects
				l.add(v);
		});

		wiki.edit(report, Toolbox.listify("{{/Header}}\n" + WPStrings.updatedAt, l, false),
				String.format("BOT: Update list (%d items)", l.size()));
	}
}