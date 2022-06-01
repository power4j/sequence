# Relase new branch 
```
mvn release:branch -DbranchName=my-branch
mvn clean deploy
```

By default, the POM in the new branch keeps the same version as the local working copy, and the local POM is incremented to the next revision. If you want to update versions in the new branch and not in the working copy, run:
```
mvn release:branch -DbranchName=my-branch -DupdateBranchVersions=true -DupdateWorkingCopyVersions=false 
git checkout my-branch
mvn clean deploy
git checkout master
```

# Updating POM Versions

```
mvn release:update-versions
```
You will be prompted for the version number for each module of the project. If you prefer that each module version be the same as the parent POM, you can use the option autoVersionSubmodules.
```
mvn release:update-versions -DautoVersionSubmodules=true
```

# Update version 
```
mvn versions:set -DnewVersion=
```