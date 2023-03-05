#!/bin/bash

file="./gradle.properties"

# Reads a property from a file by grepping and throwing away everything before "="
function prop {
    grep "${1}" ${file} | cut -d'=' -f2
}

version=$(prop 'version')
release_major=0
release_minor=0
release_patch=0

# break down the version number into it's components
regex="([0-9]+).([0-9]+).([0-9]+)-SNAPSHOT"
if [[ $version =~ $regex ]]; then
  release_major="${BASH_REMATCH[1]}"
  release_minor="${BASH_REMATCH[2]}"
  release_patch="${BASH_REMATCH[3]}"
fi

major=0
minor=0
patch=0

# check paramater to see which number to increment
if [[ "$1" == "major" ]]; then
  release_major=$((release_major + 1))
  release_minor=0
  release_patch=0
  major=${release_major}
  minor=1
  patch=0
elif [[ "$1" == "minor" ]]; then
  major=${release_major}
  minor=$((release_minor + 1))
  patch=0
elif [[ "$1" == "patch" ]]; then
  major=${release_major}
  minor=${release_minor}
  patch=$((release_patch + 1))
else
  echo "usage: ./$0.sh [major/minor/patch]"
  exit -1
fi

# echo the new version number
echo "release version: ${release_major}.${release_minor}.${release_patch}"
echo "new version: ${major}.${minor}.${patch}-SNAPSHOT"
