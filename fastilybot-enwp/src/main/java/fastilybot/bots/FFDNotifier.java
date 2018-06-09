package fastilybot.bots;

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
import fastilybot.util.BStrings;
import fastilybot.util.BotUtils;
import fastilybot.util.WTP;

/**
 * Leaves courtesy notifications (where possible) for users whose files were nominated at FfD.
 * 
 * @author Fastily
 *
 */
public final class FFDNotifier
{
	/**
	 * The start of today
	 */
	private static final ZonedDateTime today = ZonedDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.of(0, 0), ZoneOffset.UTC);

	/**
	 * Instants for the start of today and the current time (end)
	 */
	private static final Instant start = today.toInstant(), end = Instant.now();

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		// Constants
		Wiki wiki = BotUtils.getFastilyBot();
		String targetFFD = String.format("Wikipedia:Files for discussion/%d %s %d", today.getYear(),
				today.getMonth().getDisplayName(TextStyle.FULL, Locale.US), today.getDayOfMonth());
		HashSet<String> noBots = WTP.nobots.getTransclusionSet(wiki, NS.USER_TALK);

		// Associate possibly eligible files by user
		MultiMap<String, String> l = new MultiMap<>();
		wiki.splitPageByHeader(targetFFD).stream().filter(t -> t.level == 4 && wiki.whichNS(t.header).equals(NS.FILE)).forEach(t -> {
			String author = wiki.getPageCreator(t.header);
			if (author != null && !noBots.contains(author = wiki.convertIfNotInNS(author, NS.USER_TALK)))
				l.put(author, t.header);
		});

		// Skip files if user(s) have been notified, then notify accordingly
		l.l.forEach((k, v) -> {
			ArrayList<String> rl = BotUtils.detLinksInHist(wiki, k, v, start, end);
			if (rl.isEmpty())
				return;

			String x = String.format("%n{{subst:User:FastilyBot/Task12Note|%s|%s}}", rl.get(0), targetFFD);
			if (rl.size() > 1)
				x += BotUtils.listify("\nAlso:\n", rl.subList(1, rl.size()), true);
			wiki.addText(k, x + BStrings.botNote, "BOT: Notify user of FfD", false);
		});
	}
}