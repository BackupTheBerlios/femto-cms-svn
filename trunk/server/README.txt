femtocms
========

femtocms attempts to implement web content generation and presentation with as few prerequisites and dependencies as possible. It shares some functionality with similar content management systems like Apache Lenya, but is much simpler in design. The current implementation consists of less than 40 classes.

Development of the initial software was funded by Mobizcorp Europe Ltd.

Java Platform
-------------

femtocms is written in Java 5.0, the latest Java version at the time of this writing. There are no attempts at compatibility with earlier or later versions. If it happens to run on a different version that's nice but unpredictable results may occur.

HTTP Container
--------------

femtocms uses Simple as its HTTP container. See http://simpleweb.sourceforge.net/ for details.

Site Preview
------------

The preview of the current working copy of the repository is visible for authenticated users. Elements that originated from an input file are editable through an inline editor. Currently, the FCK editor from Fredericio Caldeira Knabben is used for this function.

Version Control
---------------

The SCM portion of femtocms is provided by Mercurial, which is a distributed version control system. This allows content collaboration and server colocation without the added overhead of a central repository. See http://www.selenic.com/mercurial/ for more information on Mercurial.

The current Mercurial version in use is 0.6b.

Mercurial is covered by the GNU General Public License. femtocms uses it as installed, through the command line. If you install Mercurial (the hg command) in a location that is not in the search path, point the preference for hg.command there.

Authentication
--------------

Depending on your requirements, you can use client certificates or https + basic authentication. Authenticated access is only available through an encrypted connection.

Authorization
-------------

The public content is the committed tip from any repository that happens to be mounted to the URL path at that location. Whoever controls that repository will become the integrator for public content and is responsible for pulling changes from other repositories and merging it into the public version.

