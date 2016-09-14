# Merging Pull Requests

## Prerequisites
 * Review code, make sure tests and docs are not missing.
 * Review commits, make sure they are well written and well separated.
 * Otherwise, ask contributor to squash his commits.
 * Make sure Github says that pull request can be merged without conflicts.
 * Otherwise, ask contributor to rebase his commits.
 * Make sure Github says that pull request build is passing.
 * If there is no indication whether build is passing, ask CI team to enable pull request builds for you.

## Manual merge

We prefer not to merge using the big green button since:
 * It creates redundant commits
 * It messes up git history (merge instead of rebase)
 * It is confusing to revert
 * It doesn't give you a chance to fixup things before pushing
Instead, we use Github's nice command line tool, [hub](https://hub.github.com/).

Make sure hub is installed
```sh
brew install hub
```

Rebase pull request on top of master:
```sh
hub am -3 PULL_REQUEST_URL
hub am -3 https://github.com/wix/wix-angular/pull/1
```

Amend commit so it will automatically close the pull request:
```sh
git commit --amend
Add commit message footer: "Closes #1"
```

Refactor, squash and rebase if needed and that's it, the pull request is merged and closed once you push:
```sh
git push
```
