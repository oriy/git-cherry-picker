
### skip cherry-pick phrases:

`"(no|don'?t|do not|skip):?[ _-]*cherry[ _-]*pick"`

```
> '.*no cherry pick.*',
> '.*no cherry-pick.*',
> '.*no-cheRRy pick.*',
> '.*no-cherry-pick.*',
> '.*dont cherry pick.*',
> '.*dont cherry-pick.*',
> '.*dont-cherry pick.*',
> '.*dont-cherry-pick.*',
> ".*don't cherry pick.*",
> ".*don't cherry-pick.*",
> ".*don't-cherry pick.*",
> ".*don't-cherry-pick.*",
> '.*do not cherry pick.*',
> '.*do not cherry-pick.*',
> '.*do not-cherry pick.*',
> '.*do not-cherry-pick.*',
etc.
```

### local run

* **AutoCherryPicksPR**
`./run_cherry_picker.sh  [args]`

* **GitGreenMergerMain**
`./run_gitk_merger.sh  [args]`

---

### docker build
`./docker_build.sh` 

### docker exec

* **AutoCherryPicksPR**
`./docker_exec_cherry_picker.sh  [args]`

* **GitGreenMergerMain**
`./docker_exec_gitk_merger.sh  [args]`