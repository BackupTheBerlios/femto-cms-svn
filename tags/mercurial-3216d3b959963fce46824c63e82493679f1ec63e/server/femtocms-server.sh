#! /bin/bash
# Copyright (C) 2005 mobizcorp Europe Ltd., all rights reserved.
. ${0%${0##*/}}femtocms-java.sh
"${JAVA_HOME:?}"/bin/java -cp "$cp" de.mobizcorp.femtocms.httpd.ServerMain "$@"
