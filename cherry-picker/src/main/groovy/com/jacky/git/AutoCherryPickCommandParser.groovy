package com.jacky.git

/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class AutoCherryPickCommandParser {

    static AutoCherryPickContext parseArgs(String[] args) {
        CliBuilder cli = new CliBuilder(usage: 'AutoCherryPicksPR')
        cli.h(longOpt: 'help', 'prints this message')
        cli.o(longOpt: 'owner', args: 1, 'repository owner [REQUIRED] ')
        cli.r(longOpt: 'repo', args: 1, 'repository name [REQUIRED] ')
        cli.b(longOpt: 'branches', args: 1, 'branches e.g origin/v2,origin/master [REQUIRED]')
        cli.m(longOpt: 'emails', args: 1, 'emails e.g test@org.com')
        cli.d(longOpt: 'dryrun', 'dry-run mode')
        cli.v(longOpt: 'verbose', 'verbose mode')

        OptionAccessor options = cli.parse(args)
        if (!options || !options.o || !options.r || !options.b) {
            cli.usage()
            return null
        }
        String owner = options.o
        String repo = options.r
        String branches = options.b
        String emails = options.m
        boolean dryRun = options.d
        boolean verbose = options.v

        return new AutoCherryPickContext(owner, repo, branches, emails, dryRun, verbose)
    }
}
