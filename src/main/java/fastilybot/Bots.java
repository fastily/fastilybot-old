package fastilybot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.gson.reflect.TypeToken;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.WParser;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;
import fastily.jwiki.util.MultiMap;
import fastily.wptoolbox.WikiX;
import fastily.wptoolbox.Dates;
import fastily.wptoolbox.WTP;

/**
 * Fastily's Wikipedia Bots
 * 
 * @author Fastily
 *
 */
class Bots
{
	/**
	 * Wiki-text message stating that a bot did not nominate any files for deletion.
	 */
	private static final String botNote = "\n{{subst:User:FastilyBot/BotNote}}";
	
	/**
	 * Template string for ncd instances.
	 */
	private String ncdFmt;
	
	/**
	 * The main Wiki object to use
	 */
	private Wiki wiki;

	/**
	 * Constructor, takes a logged-in Wiki object to enwp.
	 * 
	 * @param wiki The Wiki object to use
	 */
	protected Bots(Wiki wiki)
	{
		this.wiki = wiki;
		
		this.ncdFmt = String.format("{{Now Commons|%%s|date=%s|bot=%s}}%n", DateTimeFormatter.ISO_LOCAL_DATE.format(Dates.getUTCofNow()), wiki.whoami());
	}

	/**
	 * Fills in date parameter (and other missing parameters) for files in Category:Wikipedia files with the same name
	 * on Wikimedia Commons as of unknown date.
	 */
	public void dateNowCommons()
	{
		ArrayList<String> l = wiki
				.getCategoryMembers("Category:Wikipedia files with the same name on Wikimedia Commons as of unknown date", NS.FILE);
		l.removeAll(WikiX.getCategoryMembersR(wiki, "Category:Wikipedia files reviewed on Wikimedia Commons").y);

		String ncRegex = WTP.ncd.getRegex(wiki);
		for (String s : l)
			wiki.replaceText(s, ncRegex, "{{Subst:Ncd}}", "BOT: Dating {{Now Commons}}");
	}

	/**
	 * Checks daily deletion categories on enwp and notifies users if they have not been notified.
	 */
	public void ddNotifier()
	{
		//constants
		String baseConfig = String.format("User:%s/Task/6/", wiki.whoami());
		ZonedDateTime targetDT = ZonedDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.of(0, 0), ZoneOffset.UTC)
				.minusDays(1);
		
		Instant start = Instant.from(targetDT), end = Instant.now();
		
		String targetDateStr = String.format("%d %s %d", targetDT.getDayOfMonth(),
				targetDT.getMonth().getDisplayName(TextStyle.FULL, Locale.US), targetDT.getYear());
		HashSet<String> talkPageBL = WTP.nobots.getTransclusionSet(wiki, NS.USER_TALK);
		HashSet<String> idkL = FL.toSet(
				wiki.getLinksOnPage(baseConfig + "Ignore", NS.TEMPLATE).stream().flatMap(s -> wiki.whatTranscludesHere(s, NS.FILE).stream()));

		
		HashMap<String, String> rules = GSONP.gson.fromJson(wiki.getPageText(baseConfig + "Rules"), new TypeToken<HashMap<String, String>>(){}.getType());
		
		//logic
		rules.forEach((rootCat, templ) -> {
			
			Optional<String> cat = wiki.getCategoryMembers(rootCat, NS.CATEGORY).stream().filter(s -> s.endsWith(targetDateStr)).findAny();
			if (!cat.isPresent())
				return;

			MultiMap<String, String> ml = new MultiMap<>();
			wiki.getCategoryMembers(cat.get(), NS.FILE).forEach(s -> {
				if (idkL.contains(s))
					return;

				String author = wiki.getPageCreator(s);
				if (author != null)
					ml.put(wiki.convertIfNotInNS(author, NS.USER_TALK), s);
			});

			ml.l.forEach((k, v) -> {
				if (talkPageBL.contains(k))
					return;

				ArrayList<String> notifyList = WikiX.detLinksInHist(wiki, k, v, start, end);
				if (notifyList.isEmpty())
					return;

				String x = String.format("%n{{subst:%s|1=%s}}%n", templ, notifyList.get(0));
				if (notifyList.size() > 1)
					x += BUtils.listify("\nAlso:\n", notifyList.subList(1, notifyList.size()), true);

				wiki.addText(k, x + botNote, "BOT: Notify user of possible file issue(s)", false);
			});
		});
	}
	
	/**
	 * Leaves courtesy notifications (where possible) for users whose files were nominated at FfD.
	 */
	public void ffdNotifier()
	{
		// Constants
		ZonedDateTime today = ZonedDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.of(0, 0), ZoneOffset.UTC);
		Instant start = today.toInstant(), end = Instant.now();

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
			ArrayList<String> rl = WikiX.detLinksInHist(wiki, k, v, start, end);
			if (rl.isEmpty())
				return;

			String x = String.format("%n{{subst:User:FastilyBot/Task12Note|%s|%s}}", rl.get(0), targetFFD);
			if (rl.size() > 1)
				x += BUtils.listify("\nAlso:\n", rl.subList(1, rl.size()), true);
			wiki.addText(k, x + botNote, "BOT: Notify user of FfD", false);
		});
	}

	/**
	 * Finds files on enwp nominated for deletion on Commons and flags the local file.
	 */
	public void findCommonsFFD()
	{
		String ncRegex = WTP.ncd.getRegex(wiki);
		HashSet<String> fl = new HashSet<>(WikiX.getCommons(wiki).whatTranscludesHere("Template:Deletion template tag", NS.FILE));

		WikiX.getFirstOnlySharedDuplicate(wiki, wiki.whatTranscludesHere(WTP.ncd.title, NS.FILE)).forEach((k, v) -> {
			if (fl.contains(v))
				wiki.replaceText(k, ncRegex, String.format("{{Nominated for deletion on Commons|%s}}", wiki.nss(v)),
						"BOT: File is up for deletion on Commons");
		});
	}

	/**
	 * Finds local enwp files transferred to Commons which have then been deleted on Commons.
	 */
	public void findDelComFFD()
	{
		Wiki com = WikiX.getCommons(wiki);

		Pattern nomDelTemplPattern = Pattern.compile(WTP.nomDelOnCom.getRegex(wiki));
		HashMap<String, String> pageTexts = MQuery.getPageText(wiki, wiki.whatTranscludesHere(WTP.nomDelOnCom.title, NS.FILE));

		HashMap<String, String> comPairs = new HashMap<>();
		pageTexts.forEach((k, v) -> {
			try
			{
				//TODO: should ideally be parsing the entire page
				String comFile = WParser.parseText(wiki, WikiX.extractTemplate(nomDelTemplPattern, v)).getTemplates().get(0).get("1")
						.toString();
				if (comFile != null)
					comPairs.put(k, wiki.convertIfNotInNS(comFile, NS.FILE));
			}
			catch (Throwable e)
			{
				comPairs.put(k, k);
			}
		});

		FL.toHM(MQuery.exists(com, false, comPairs.keySet()).stream()
				.filter(s -> !com.getLogs(comPairs.get(s), null, "delete", 1).isEmpty()), Function.identity(), comPairs::get)
				.forEach((k, v) -> wiki.edit(k,
						pageTexts.get(k).replaceAll(WTP.nomDelOnCom.getRegex(wiki), String.format("{{Deleted on Commons|%s}}", wiki.nss(v))),
						"BOT: Adding note that file has been deleted on Commons"));
	}

	/**
	 * Finds local enwp files which were nominated for deletion on Commons but kept.
	 */
	public void findKeptComFFD()
	{
		String nfdcRegex = WTP.nomDelOnCom.getRegex(wiki);
		HashSet<String> cffdl = new HashSet<>(WikiX.getCommons(wiki).whatTranscludesHere("Template:Deletion template tag", NS.FILE));

		WikiX.getFirstOnlySharedDuplicate(wiki,
				wiki.getCategoryMembers("Category:Files nominated for deletion on Wikimedia Commons", NS.FILE)).forEach((k, v) -> {
					if (!cffdl.contains(v))
						wiki.replaceText(k, nfdcRegex, String.format(ncdFmt, wiki.nss(v)), "BOT: File is not up for deletion on Commons");
				});
	}

	/**
	 * Finds enwp files which are flagged as both free and non-free.
	 */
	public void findLicConflict()
	{
		HashSet<String> fl = BUtils.fetchLabsReportAsFiles(wiki, 2);

		for (String s : wiki.getLinksOnPage(String.format("User:%s/Task5/Ignore", wiki.whoami())))
			fl.removeAll(wiki.whatTranscludesHere(s, NS.FILE));

		for (String s : MQuery.exists(wiki, true, fl))
			wiki.addText(s, "{{Wrong-license}}\n", "BOT: Possible conflict in copyright status", true);
	}

	/**
	 * Finds and flags orphaned free media files on enwp.
	 */
	public void flagOI()
	{
		HashSet<String> l = BUtils.fetchLabsReportAsFiles(wiki, 3);
		// omit flagged orphaned files
		l.removeAll(BUtils.fetchLabsReportAsFiles(wiki, 9));

		for (String c : wiki.getLinksOnPage(String.format("User:%s/Task10/Ignore", wiki.whoami())))
			l.removeAll(wiki.getCategoryMembers(c, NS.FILE));

		l.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(wiki.whatTranscludesHere("Template:Deletable file", NS.FILE));

		l.removeAll(BUtils.fetchLabsReportAsFiles(wiki, 4));
		l.removeAll(MQuery.exists(wiki, false, l));

		for (String s : l)
			wiki.addText(s, "\n{{Orphan image}}", "BOT: Noting that file has no inbound file usage", false);
	}

	/**
	 * Find and fix tags for files tagged for transfer to Commons which have already transferred.
	 */
	public void mtcHelper()
	{
		HashSet<String> l = BUtils.fetchLabsReportAsFiles(wiki, 1);
		l.retainAll(WTP.mtc.getTransclusionSet(wiki, NS.FILE));
		// omit in-line tags
		l.removeAll(WTP.keeplocal.getTransclusionSet(wiki, NS.FILE));

		String tRegex = WTP.mtc.getRegex(wiki);
		HashSet<String> ncdL = WTP.ncd.getTransclusionSet(wiki, NS.FILE);

		WikiX.getFirstOnlySharedDuplicate(wiki, l).forEach((k, v) -> {
			if (ncdL.contains(k))
				wiki.replaceText(k, tRegex, "BOT: File has already been copied to Commons");
			else
			{
				String oText = wiki.getPageText(k);
				String nText = oText.replaceAll(tRegex, "");
				if (oText.equals(nText)) // avoid in-line tags
					return;

				wiki.edit(k, String.format(ncdFmt, wiki.nss(v)) + nText, "BOT: File is available on Commons");
			}
		});
	}

	/**
	 * Untags non-eligible files for Commons.
	 */
	public void removeBadMTC()
	{
		HashSet<String> l = WTP.mtc.getTransclusionSet(wiki, NS.FILE);
		l.removeAll(WikiX.getCategoryMembersR(wiki, "Category:Copy to Wikimedia Commons reviewed by a human").y);
		l.removeAll(wiki.getCategoryMembers("Category:Copy to Wikimedia Commons (inline-identified)"));

		String tRegex = WTP.mtc.getRegex(wiki);

		wiki.getLinksOnPage(String.format("User:%s/Task2/Blacklist", wiki.whoami())).stream()
				.flatMap(s -> wiki.getCategoryMembers(s, NS.FILE).stream()).filter(l::contains)
				.forEach(s -> wiki.replaceText(s, tRegex, "BOT: file may not be eligible for Commons"));
	}

	/**
	 * Removes {@code Orphan image} from freely licensed files which contain file links in the main space.
	 */
	public void unflagOI()
	{
		// Generate the set of files with no links of any sort
		HashSet<String> oL = BUtils.fetchLabsReportAsFiles(wiki, 3);
		oL.removeAll(BUtils.fetchLabsReportAsFiles(wiki, 4));

		// Get all files tagged with Orphan image which are not orphans
		HashSet<String> l = BUtils.fetchLabsReportAsFiles(wiki, 9);
		l.removeAll(oL);
		l.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));

		// Restrict working set to Free files only
		l.retainAll(BUtils.fetchLabsReportAsFiles(wiki, 6));

		String oiRegex = WTP.orphan.getRegex(wiki);
		for (String s : l)
			wiki.replaceText(s, oiRegex, "BOT: File contains inbound links");
	}
}