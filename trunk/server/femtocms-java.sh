#! /bin/sh
# femtocms minimalistic content management.
# Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

if [ -z "$WINDIR" ]; then
  PATHSEP=':'
else
  PATHSEP=';'
fi

if [ -z "$JAVA_HOME" ]; then
  for dir in `ls -rd /opt/jre1.5* /opt/jdk1.5* 2>/dev/null`; do
    if [ -x "$dir"/bin/java ]; then
      JAVA_HOME="$dir"
      break;
    fi
  done
  if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME not set, and cannot find Java." 1>&2
    exit 1
  else
    export JAVA_HOME
  fi
fi

cp=bin
for jar in jar/*.jar; do
  cp="$cp$PATHSEP$jar"
done
