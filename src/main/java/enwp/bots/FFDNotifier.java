package enwp.bots;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.MultiMap;
import fastily.wpkit.text.StrUtil;
import fastily.wpkit.text.WTP;
import fastily.wpkit.util.WikiX;
import util.BotUtils;

/**
 * Leaves courtesy notifications (where possible) for users whose files were nominated at FfD.
 * 
 * @author Fastily
 *
 */
public final class FFDNotifier
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = BotUtils.getFastilyBot();

	/**
	 * The start of today
	 */
	private static final ZonedDateTime today = ZonedDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.of(0, 0), ZoneOffset.UTC);

	/**
	 * Instants for the start of today and the current time (end)
	 */
	private static final Instant start = today.toInstant(), end = Instant.now();

	/**
	 * The title of today's FfD
	 */
	private static String targetFFD = String.format("Wikipedia:Files for discussion/%d %s %d", today.getYear(),
			today.getMonth().getDisplayName(TextStyle.FULL, Locale.US), today.getDayOfMonth());

	/**
	 * List of users which do {@code nobots}. These are avoided.
	 */
	private static final HashSet<String> noBots = WTP.nobots.getTransclusionSet(wiki, NS.USER_TALK);

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		MultiMap<String, String> l = new MultiMap<>();
		wiki.getSectionHeaders(targetFFD).stream().filter(t -> t.x == 4 && wiki.whichNS(t.y).equals(NS.FILE)).forEach(t -> {
			String author = WikiX.getPageAuthor(wiki, t.y);
			if (author != null && !noBots.contains(author = wiki.convertIfNotInNS(author, NS.USER_TALK)))
				l.put(author, t.y);
		});

		l.l.forEach((k, v) -> {
			ArrayList<String> rl = BotUtils.detLinksInHist(wiki, k, v, start, end);
			if (rl.isEmpty())
				return;

			String x = String.format("%n{{subst:User:FastilyBot/Task12Note|%s|%s}}", rl.get(0), targetFFD);
			if (rl.size() > 1)
				x += StrUtil.listify("\nAlso:\n", rl.subList(1, rl.size()), true);
			wiki.addText(k, x + BotUtils.botNote, "BOT: Notify user of FfD", false);
		});
	}
}