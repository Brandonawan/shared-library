#!/bin/bash

# This script checks out the source code from the provided Git repository using the default workspace directory.

# Define your Git repository URL
repository_url="git@github.com:Brandonawan/shared-library.git"

# Use the Jenkins default workspace directory
target_directory="$WORKSPACE"

# Define the branch, tag, or commit you want to checkout
branch_or_tag="main"

# Check if the target directory is empty (no existing Git repo)
if [ -z "$(ls -A "$target_directory")" ]; then
  # If it's empty, perform a fresh Git clone
  git clone "$repository_url" "$target_directory"
else
  # If it's not empty, ensure it's a Git repository and fetch the latest changes
  if [ -d "$target_directory/.git" ]; then
    git -C "$target_directory" fetch origin
  else
    echo "The target directory is not a Git repository. Initialize it as a Git repository first."
    exit 1
  fi
fi

# Check out the specified branch, tag, or commit
git -C "$target_directory" checkout "$branch_or_tag"

# Optional: Update submodules if your repository uses them
# git -C "$target_directory" submodule update --init --recursive

echo "Source code checked out in $target_directory on branch/tag/commit: $branch_or_tag"
