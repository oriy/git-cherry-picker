git-cherry-picker.jar
---------------------
creates and auto-merges GitHub pull requests of cherry-pick commits

```
git-cherry-picker -r https://github.com/org/repo.git -b 'origin/fromBranch,origin/toBranch' -m admin@org.com -v
```

#### Current Do not cherry pick messages are listed here:

#### [doNotCherryPickMessages](cherry-picker/src/main/groovy/com/kenshoo/git/AutoCherryPicksPR.groovy#L28)
```
            "no-cherry-pick",
            "no cherry pick", "no cherrypick", "no cherry-pick",
            "don't cherry pick", "don't cherrypick", "don't cherry-pick",
            "do not cherry pick", "do not cherrypick", "do not cherry-pick"
```
