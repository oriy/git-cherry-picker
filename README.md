git-cherry-picker.jar
---------------------
creates and auto-merges GitHub pull requests of cherry-pick commits

```
git-cherry-picker -r https://github.com/kenshoo/repo.git -b 'origin/fromBranch,origin/toBranch' -m _rndMergelist@kenshoo.com -v
```

#### Current Do not cherry pick messages are listed here:

#### [doNotCherryPickMessages](https://github.com/kenshoo/jenkins-automation/blob/master/git-cherry-picker/src/main/groovy/com/kenshoo/git/AutoCherryPicksPR.groovy#L28)
```
            "no-cherry-pick",
            "no cherry pick", "no cherrypick", "no cherry-pick",
            "don't cherry pick", "don't cherrypick", "don't cherry-pick",
            "do not cherry pick", "do not cherrypick", "do not cherry-pick"
```
