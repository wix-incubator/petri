# Working with Pull Requests

Even if you are already familiar with pull requests or prefer to use IntelliJ tools for controlling git and not command line (lamer), please keep on reading because there are some other important things here...

## Introduction

So what is a pull request? Well, in general the idea is that you don't make changes directly on the real wix-dashboard-framework repository. Instead, you make the changes on a "fork" of the repository which is simply a duplicate of the repository that resides in your Github user. Once you are done making your change, you can create a "pull request" which is simply a request to merge the changes in some branch in your fork into the real repository.

## The Fork

Creating a fork is easy (and you only need to do it once) - just go to the repository you want to fork on Github (for example, https://github.com/wix/wix-dashboard-framework-statics) and click the "fork" button in the upper right corner. Once the creation of the fork is complete you'll see that you've been redirected to a new address: https://github.com/{your-username}/wix-dashboard-framework-statics.

Now you can copy the git url of this new fork (the url is near the bottom of the right pane) and clone this repo to you machine like you would clone any repo:
```
git clone git@github.com:{your-username}/wix-dashboard-framework-statics.git
```
Or, if you have the [Github for Mac](https://mac.github.com/) application installed (highly recommended), you can just click the "Clone to Desktop" button in the bottom of the right pane.

One time thing - please run this command (doesn't matter from which directory), it will make things easier:
```
git config --global push.default simple
```

Okay, now that you have a clone you can start making your changes, committing them and push them to your repo. **BUT**, instead of making the changes on the master branch, first create a new branch for you change:
```
git checkout -b name-that-describes-your-change
```

The first time you push this branch you need to run a command that is a bit more verbose, so just run `git push` and do what git tells you to do. Once you are finished you can simply open your browser and go to your repository on Github, you will see that Github already noticed that you pushed a branch and recommends you to create a pull request, so just click the button. If you don't see it, just select your branch from the branch drop down in the upper left corner and then press the green button next to it.

## The PR

Okay, so you've created the pull request. This is really cool since now we can do the code review in Github, which has really nice tools for commenting on code. Every commit you push to this branch is automatically added to the existing pull request (remember? it is just a request to merge your branch into the real repository) so we can discuss and fix everything about the new change until we are ready to merge. If during the code review process you need to make another change, you simply create a new branch and a new pull request (this is the reason we are using branches, so that you can have multiple pull requests and won't be stuck just because the code review for some feature takes too long).

Two important things to remember:
 1. Always start the new branch from the master branch and not from your feature branch so that your new branch only includes your new feature.
 2. Make sure your master branch is updated before you start your new branch.

How to do this? Easy (really):

Add the real repo as remote repo so that you can pull stuff from it (you only need to do this one time):
```
git remote add upstream git@github.com:wix/wix-dashboard-framework-statics.git
```

Go to master branch and pull all the latest updates from the upstream remote (it can be any name, but let's use upstream as convention):
```
git checkout master
git pull --rebase upstream master
```

Create your new branch:
```
git checkout -b my-new-feature-description
```

## Merging

Okay, so back to the code review - as we said you just keep pushing stuff that fixes the comments from the code review until everyone is happy. Now, the code is fine and we want to merge the pull request to the real repo, but first we want to do 2 things:
 * Make sure the branch is updated by simply running the following command from your branch. (and fixing conflicts obviously)
```
git pull --rebase upstream master
```
 * Squash your commits into a single commit with a meaningful message about what you actually did :) More on this soon.

After rebasing and squashing you will need to `git push --force` instead of just `git push` since both of those steps change the history of stuff that were already pushed to your repository, and that's okay since you are the only one working on your fork. **BUT**, before you push make sure you look at your `git log` carefully and make sure that your commits are placed nicely in the end because if you did something wrong there is no way back after you `git push --force`...

## Squashing

Okay, about squashing - this is important so that when looking on the git log we can actually understand what is going on. The nice thing about working with branches is that each branch is one feature and all of the commits in this branch can (most of the time) be represented by only one commit. This is a nice way to work even if we are not using pull requests.

Squashing is really easy ​ - All we have to do is look at the git log and see how many new commits we have in this branch and then the following command (replace X with the number of commits you have):​
```
git rebase --interactive ​HEAD~X
```
Next, what happens is that a text editor will open with something that looks like this:
```
pick 1fc6c95 do something
pick 6b2481b do something else
pick dd1475d changed some things
pick c619268 fixing typos
```
You just change it to this:
```
pick 1fc6c95 do something
squash 6b2481b do something else
squash dd1475d changed some things
squash c619268 fixing typos
```
Or if you want the second commit to be squashed to the first and the fourth commit to be squashed to the third (you can also change the order of lines):
```
pick 1fc6c95 do something
squash 6b2481b do something else
pick dd1475d changed some things
squash c619268 fixing typos
```
Or if you want to squash the first three and only want to change the commit message of the last:
```
pick 1fc6c95 do something
squash 6b2481b do something else
squash dd1475d changed some things
reword c619268 fixing typos
```

Once you save and exit the text editor, git will do the squashing and will open an editor again so you can enter the new commit message. **BUT**, about this commit message... :) Let's try to follow the guidelines described [here](https://github.com/angular/angular.js/blob/master/CONTRIBUTING.md#commit-message-format) for how we format the message, it makes it really easy to understand what is going on in the git log and also makes it easy to produce a change log when releasing a version. In general I think that the message format and the branching and squashing is stuff we can adopt regardless of pull requests.

That's it!
