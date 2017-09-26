package enwp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import enwp.bots.DDNotifier;
import enwp.bots.DateNowCommons;
import enwp.bots.FFDNotifier;
import enwp.bots.FindCommonsFFD;
import enwp.bots.FindDelComFFD;
import enwp.bots.FindKeptComFFD;
import enwp.bots.FindLicConflict;
import enwp.bots.FlagOI;
import enwp.bots.MTCHelper;
import enwp.bots.RemoveBadMTC;
import enwp.bots.UnflagOI;
import enwp.reports.MTCRedirs;
import enwp.reports.CountFfD;
import enwp.reports.FprodSum;
import enwp.reports.BrokenSPI;
import enwp.reports.OrphanedFfD;
import enwp.reports.UntaggedDD;
import enwp.reports.MissingFCT;
import enwp.reports.OrphanedKL;
import enwp.reports.OversizedFU;
import enwp.reports.TallyLics;
import fastily.wpkit.FCLI;

/**
 * CLI interface which makes it easy to launch enwp bots/reports
 * 
 * @author Fastily
 *
 */
public final class BMgr
{
	/**
	 * Format String for errors caused by bad arguments
	 */
	private static final String badNumberFmt = "'%s' is not a valid %s number%n";

	/**
	 * Main driver
	 * 
	 * @param args Program arguments. Additional arguments will be passed to tasks/reports.
	 * @throws Throwable If one of the called bots/reports had an error.
	 */
	public static void main(String[] args) throws Throwable
	{
		CommandLine cl = FCLI.gnuParse(makeOpts(), args, "BMgr [-r <report number> |-b <task number>] [--help] [Task/Report Args...]");

		String[] pArgs = cl.getArgs();
		if (cl.hasOption('b'))
			switch (Integer.parseInt(cl.getOptionValue('b')))
			{
				case 1:
					MTCHelper.main(pArgs);
					break;
				case 2:
					RemoveBadMTC.main(pArgs);
					break;
				case 3:
					BrokenSPI.main(pArgs);
					break;
				case 4:
					UnflagOI.main(pArgs);
					break;
				case 5:
					FindLicConflict.main(pArgs);
					break;
				case 6:
					DDNotifier.main(pArgs);
					break;
				case 7:
					FindCommonsFFD.main(pArgs);
					break;
				case 8:
					FindDelComFFD.main(pArgs);
					break;
				case 9:
					FindKeptComFFD.main(pArgs);
					break;
				case 10:
					FlagOI.main(pArgs);
					break;
				case 11:
					DateNowCommons.main(pArgs);
					break;
				case 12:
					FFDNotifier.main(pArgs);
					break;
				default:
					System.err.printf(badNumberFmt, cl.getOptionValue('b'), "task");
			}
		else if (cl.hasOption('r'))
			switch (Integer.parseInt(cl.getOptionValue('r')))
			{
				case 1:
					UntaggedDD.main(pArgs);
					break;
				case 2:
					OrphanedFfD.main(pArgs);
					break;
				case 3:
					TallyLics.main(pArgs);
					break;
				case 4:
					MTCRedirs.main(pArgs);
					break;
				case 5:
					CountFfD.main(pArgs);
					break;
				case 6:
					OrphanedKL.main(pArgs);
					break;
				case 7:
					OversizedFU.main(pArgs);
					break;
				case 8:
					FprodSum.main(pArgs);
					break;
				case 9:
					MissingFCT.main(pArgs);
					break;
				default:
					System.err.printf(badNumberFmt, cl.getOptionValue('r'), "report");
			}
		else
			System.out.println("Invalid argument, please run with --help for usage instructions");
	}

	/**
	 * Creates the Options list for the program
	 * 
	 * @return A list of Options
	 */
	private static Options makeOpts()
	{
		Options ol = FCLI.makeDefaultOptions();
		ol.addOptionGroup(FCLI.makeOptGroup(FCLI.makeArgOption("b", "Triggers a bot task to be run", "Task number"),
				FCLI.makeArgOption("r", "Triggers a report task to be run", "Report number")));

		return ol;
	}
}