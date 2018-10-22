# How to Contribute

# Building FreeACS

System Requirements:

* JDK 8
* SBT 1.X

The first step to contributing to FreeACS is to clone the FreeACS
repo from Github and build the project using SBT.

## Build FreeACS:

To build FreeACS, do the following:

* Fork the FreeACS repo on Github -
  [https://github.com/freeacs/freeacs](https://github.com/freeacs/freeacs).

* Clone the forked repo's master branch (or preferred branch) with no commit
  history:

      git clone https://github.com/<github-username>/freeacs --branch master --single-branch --depth 1

* Add the main FreeACS repo as an upstream for fetching changes:

      git remote add upstream https://github.com/freeacs/freeacs

* Build master branch:

      cd freeacs
      sbt test

* Create runnable builds:

      sbt universal:stage
      
The latter will create runnable versions of each module in:
       
      ./<module>/target/universal/stage/

for ex, to run tr069 (it will crash if you have not setup a database, loaded the acs table and added an acs user):

      ./tr069/target/universal/stage/bin/freeacs-tr069

You can find tables.zip on release page, but its easier to load up docker to get the database set up.

# Making Changes

When making changes, it's best to start off by creating an issue
and referencing the issue number from within
any commits and pull requests.

## Github

Submit your custom changes to Github using the following process:

* Create a topic branch to hold your changes based on upstream/master:

      git fetch upstream
      git checkout -b my-custom-change upstream/master

* Commit logical units of work including a reference to your issue number. For
  example:

      #234 Make the example in CONTRIBUTING imperative and concrete

* Test your changes thoroughly! Make sure your changes in one environment 
  don't break something in another environment. Not always important,
  but keep it in mind.

* Before pushing your branch to your fork on Github, it's a good idea to rebase
  on the updated version of upstream/master:

      git fetch upstream
      git rebase upstream/master

* Push changes in your branch to your fork:

      git push origin my-custom-change

* Submit the pull request to the freeacs/freeacs repo.

You're done! Well, not quite---be sure to respond to comments and questions to
your pull request until it is closed.

# Additional Resources

* [General GitHub documentation](http://help.github.com/)
* [GitHub pull request documentation](http://help.github.com/send-pull-requests/)