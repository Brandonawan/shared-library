#!/bin/bash

# Update the local repository by pulling changes from the remote
git pull

# Check if there are any changes to commit
if [[ -n $(git status -s) ]]; then
    # Perform 'git add .' to stage all changes
    git add .

    # Create a timestamp for the commit message
    timestamp=$(date +"%Y-%m-%d %H:%M:%S")

    # Commit changes with a timestamped message
    git commit -m "updates ${timestamp}"

    # Push changes to the remote repository
    git push

    echo "Changes committed and pushed."
else
    echo "No changes to commit."
fi
