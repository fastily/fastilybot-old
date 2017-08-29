package tools;

import java.util.ArrayList;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.BotUtils;

/**
 * Deletes empty categories on enwp that are ready for deletion.
 * 
 * @author Fastily
 *
 */
public class EmptyCats
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 */
	public static void main(String[] args)
	{
		Wiki wiki = BotUtils.getFastily();

		ArrayList<String> tpl = new ArrayList<>();

		MQuery.getCategorySize(wiki, wiki.getCategoryMembers("Category:Candidates for speedy deletion as empty categories", NS.CATEGORY))
				.forEach((k, v) -> {
					if (v.equals(0) && wiki.delete(k, "[[WP:CSD#C1|C1]]: Empty category"))
						tpl.add(wiki.convertIfNotInNS(wiki.nss(k), NS.CATEGORY_TALK));
				});

		MQuery.exists(wiki, tpl).forEach((k, v) -> {
			if (v)
				wiki.delete(k, BotUtils.csdG8talk);
		});
	}
}