gitk
----

#### config.yml
config holds github account details as well as gmail credentials
passwords for both github and gmail are encrypted

run `ConfigurationUtil` main() in order to encrypt passwords

```
::: config.yml :::
---
repository: "git-cherry-picker"
organization: "org"
gitUserName: "user"
gitUserEmail: "user@org.com"
gitUserPass: "****"
gmailUser: "gmailuser"
gmailPass: "********"

Please enter git user user's password: pass
Please enter gmail account gmailuser's password: password

--- passwords saved ---


::: config.yml :::
---
repository: "git-cherry-picker"
organization: "org"
gitUserName: "user"
gitUserEmail: "user@org.com"
gitUserPass: "cITmQYaAWR8"
gmailUser: "gmailuser"
gmailPass: "LCevAcQ_jOIICs7XUhsm-A"
```