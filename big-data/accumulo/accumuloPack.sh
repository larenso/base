#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=accumulo
mode=bdproduct
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/accumulo

	accumuloVersion=1.6.1

	# Create directories that are required for the debian package
	mkdir -p $tempDirectory	
	mkdir -p $confDirectory

	# download accumulo
	# wget http://archive.apache.org/dist/accumulo/$accumuloVersion/accumulo-$accumuloVersion-bin.tar.gz -P $tempDirectory
	wget http://archive.apache.org/dist/accumulo/1.6.1/accumulo-1.6.1-bin.tar.gz -P $tempDirectory
	
	pushd $tempDirectory
	tar -xzpf accumulo-*.tar.gz

	# remove tar file
	rm accumulo-*.tar.gz
	
	# move configuration files 
	mv accumulo-$accumuloVersion/conf/* $confDirectory

	# copy sample configuration files
	cp -a $confDirectory/examples/3GB/standalone/* $confDirectory/
	popd
}

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkVersion $productName $mode
# 2) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
# 5) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
