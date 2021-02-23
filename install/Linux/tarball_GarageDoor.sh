#!/bin/sh

rm -rf GarageDoor

#Gather the files
mkdir GarageDoor
cp ../GarageDoor.jar GarageDoor
cp ../../license.txt GarageDoor
cp ../../CONTRIBUTORS.txt GarageDoor
cp ../../CHANGES.txt GarageDoor

#Make the tarball!
tar czf GarageDoor-0.1.0.tar.gz GarageDoor