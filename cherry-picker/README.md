cherry-picker.jar
-----------------
creates GitHub pull requests of cherry-pick commits

```
cherry-picker -o owner -r repo -b 'origin/fromBranch,origin/toBranch' -m admin@org.com -v
```

```
usage: AutoCherryPicksPR
 -o,--owner <arg>      repository owner [REQUIRED]
 -r,--repo <arg>       repository name [REQUIRED]
 -b,--branches <arg>   branches e.g origin/v2,origin/master [REQUIRED]
 -m,--emails <arg>     emails e.g test@org.com
 -v,--verbose          verbose mode
 -d,--dryrun           dry-run mode
 -h,--help             prints this message
```

#### Current Do not cherry pick messages are listed here:

#### [doNotCherryPickMessages](cherry-picker/src/main/groovy/com/jacky/git/AutoCherryPicksPR.groovy#L28)
```
            "no-cherry-pick",
            "no cherry pick", "no cherrypick", "no cherry-pick",
            "don't cherry pick", "don't cherrypick", "don't cherry-pick",
            "do not cherry pick", "do not cherrypick", "do not cherry-pick"
```
