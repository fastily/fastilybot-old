package tools;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import fastily.jwiki.core.Wiki;
import fastily.wpkit.util.FCLI;
import fastily.wpkit.util.WikiX;
import util.BotUtils;

/**
 * Simple utility to upload files to Commons.
 * 
 * @author Fastily
 *
 */
public class QUpload
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki;

	/**
	 * Main driver.
	 * 
	 * @param args Program arguments.
	 */
	public static void main(String[] args)
	{
		CommandLine l = FCLI.gnuParse(makeOptList(), args, "SimpleUpload [-c] [-d] [-h] files");

		wiki = BotUtils.getFSock();
		if (l.hasOption('c'))
			wiki = BotUtils.getCommons(wiki);

		String extRegex = WikiX.allowedFileExtsRegex(wiki), desc = l.getOptionValue('d', "");
		
		l.getArgList().stream().filter(s -> s.matches(extRegex)).map(Paths::get).filter(Files::exists)
				.forEach(p -> wiki.upload(p, p.getFileName().toString(), desc, ""));
	}

	/**
	 * Makes the list of CLI options.
	 * 
	 * @return The list of CommandLine options.
	 */
	private static Options makeOptList()
	{
		Options ol = FCLI.makeDefaultOptions();
		ol.addOption(FCLI.makeArgOption("d", "Set the description text", "descText"));
		ol.addOption("c", "Upload to Commons instead of en.wikipedia.org");
		return ol;
	}
}