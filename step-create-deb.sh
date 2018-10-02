#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

echo ----[ Create .deb ]----
DEB_FILE=james-manager_${VERSION}_amd64.deb
DEB_PATH=$RUN_PATH/build/debian_out/james-manager
rm -rf $DEB_PATH
mkdir -p $DEB_PATH $DEB_PATH/DEBIAN/ $DEB_PATH/opt/james-manager/


cat > $DEB_PATH/DEBIAN/control << _EOF
Package: james-manager
Version: $VERSION
Maintainer: Foilen
Architecture: amd64
Description: This is an application to update the Apache James Email service by applying the config from a file.
_EOF

cp -rv DEBIAN $DEB_PATH/
tar -xf build/distributions/james-manager-boot-$VERSION.tar --strip 1 -C $DEB_PATH/opt/james-manager/

cd $DEB_PATH/..
dpkg-deb --no-uniform-compression --build james-manager
mv james-manager.deb $DEB_FILE
