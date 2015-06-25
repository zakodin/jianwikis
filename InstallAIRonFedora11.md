

## Adobe Air ##

I downloaded the AIR from

http://get.adobe.com/air/

and made the file executable

```
[root@Mars ~]# chmod +x AdobeAIRInstaller.bin
```

Then, tried to install it

```
[root@Mars ~]# ./AdobeAIRInstaller.bin
```

However, it threw the following error:

```
"Adobe AIR could not be installed. Either gnome-keyring or Kwallet must be installed prior to installing Adobe AIR."
```

It also had errors such as

```
"Gtk-Message: Failed to load module "pk-gtk-module": libpk-gtk-module.so: cannot open shared object file: No such file or directory
Gtk-Message: Failed to load module "gnomebreakpad": libgnomebreakpad.so: cannot open shared object file: No such file or directory"
```

The above errors were for the AIR installer and you do not really need to fix them. If you want to, you can use yum to find the appropriate RPMs, such as,

```
[root@Mars ~]# yum provides */libpk-gtk-module.so

Loaded plugins: fastestmirror, presto, refresh-packagekit
Loading mirror speeds from cached hostfile
* livna: rpm.livna.org
* rpmfusion-free: mirror.liberty.edu
* rpmfusion-free-updates: mirror.liberty.edu
* rpmfusion-nonfree: mirror.liberty.edu
* rpmfusion-nonfree-updates: mirror.liberty.edu
PackageKit-gtk-module-0.4.6-8.fc11.x86_64 : Install fonts automatically using PackageKit
Repo : fedora
Matched from:
Filename : /usr/lib64/gtk-2.0/modules/libpk-gtk-module.so

PackageKit-gtk-module-0.4.6-8.fc11.i586 : Install fonts automatically using PackageKit
Repo : fedora
Matched from:
Filename : /usr/lib/gtk-2.0/modules/libpk-gtk-module.so

PackageKit-gtk-module-0.4.8-2.fc11.i586 : Install fonts automatically using PackageKit
Repo : updates
Matched from:
Filename : /usr/lib/gtk-2.0/modules/libpk-gtk-module.so

PackageKit-gtk-module-0.4.8-2.fc11.x86_64 : Install fonts automatically using PackageKit
Repo : updates
Matched from:
Filename : /usr/lib64/gtk-2.0/modules/libpk-gtk-module.so

PackageKit-gtk-module-0.4.8-2.fc11.x86_64 : Install fonts automatically using PackageKit
Repo : installed
Matched from:
Filename : /usr/lib64/gtk-2.0/modules/libpk-gtk-module.so
```


Since the AIR is 32 bit, you need to install the 32 bit version of PackageKit-gtk-module, i.e.,

```
[root@Mars ~]# yum install PackageKit-gtk-module-0.4.6-8.fc11.i586
```

Go back the original error, seems I missed the gnome-keyring RPM, installed it as follows,

```
[root@Mars ~]# yum install gnome-keyring.i586
```

Because AIR is for 32 bits, I had to install the 32 bit version even I am on 64 bit Linux.

Tried to install the AIR again

```
[root@Mars ~]# ./AdobeAIRInstaller.bin
```

This time, it threw a different error:

```
"An error occurred while installing Adobe AIR. Installation may not be allowed by your administrator. Please contact your administrator."
```

Really strange, because I used the user root to install. Did some Google search again and found some hints from

http://rootblock.wordpress.com/2009/06/24/fedora-11-adobe-air-installation-not-allowed-by-administrator/

Seems the actual reason is that I missed a lot of dependencies. The suggested solution is to install the following RPMS:

```
yum -y install xterm gtk2-devel gnome-keyring libxml2-devel libxslt rpm-devel nss
```

But I am on 64 bit Linux, the default RPMs are 64 bits, thus, I need to use 32 bit versions instead. As a result, I installed the following RPMs:

```
yum -y install xterm.i586 gtk2-devel.i586 gnome-keyring.i586 libxml2-devel.i586 libxslt.i586 rpm-devel.i586 nss.i586
```

Retried again,

```
[root@Mars ~]# ./AdobeAIRInstaller.bin
```

This time, no error was thrown.

## Blaze DS ##

If you need to install BlazeDS, here is a good guide

http://sujitreddyg.wordpress.com/2009/05/07/blazemonster/

The simplest way is to download the turnkey bundle from

http://opensource.adobe.com/wiki/display/blazeds/Release+Builds

Unzip it and run it directly because it comes with a tomcat instance.

```
[jfang@Mars BlazeDS]$ tree . -L 2
.
|-- blazeds-turnkey-readme.htm
|-- blazeds.war
|-- docs
|   `-- javadoc.zip
|-- ds-console.war
|-- resources
|   |-- ColdFusion
|   |-- clustering
|   |-- config
|   |-- fds-ajax-bridge
|   |-- flex_sdk
|   |-- lib
|   `-- security
|-- sampledb
|   |-- flexdemodb
|   |-- hsqldb.jar
|   |-- server.properties
|   |-- startdb.bat
|   |-- startdb.sh
|   |-- stopdb.bat
|   `-- stopdb.sh
|-- samples.war
`-- tomcat
    |-- LICENSE
    |-- NOTICE
    |-- RELEASE-NOTES
    |-- RUNNING.txt
    |-- bin
    |-- conf
    |-- lib
    |-- logs
    |-- temp
    |-- webapps
    `-- work

19 directories, 15 files
```

To run the BlazeDS, you need to first run the sampledb, i.e.,

```
[jfang@Mars BlazeDS]$ cd sampledb/
[jfang@Mars BlazeDS]$ ./startdb.sh
```

and then start the BlazeDS instance,

```
[jfang@Mars BlazeDS]$ cd tomcat/bin/
[jfang@Mars BlazeDS]$ ./startup.sh
```

## Blaze Monster ##

To install Blaze Monster, simply download the following air file and open it with the AIR you just installed,

http://www.sujitreddyg.com/applicationinstallers/blazemonster/BlazeMonster.air

Sometimes, the installation fails due to Linux SELinux. You can change the SELinux enforcing mode to _Permissive_ to work around this problem.

To make Blaze Monster work for your web application, you need the following extra steps:

  1. Download http://www.sujitreddyg.com/applicationinstallers/blazemonster/BlazeMonsterServerSetup.zip
  1. Unzip the zip file
  1. Copy the invokeremotingservicebrowser.jsp from the extracted folder in to the root folder of your web application
  1. Copy the folder named com from the extracted folder in to the WEB-INF/classes folder of your web application

Here, I repeated the steps described in [the Blaze Monster post](http://www.sujitreddyg.com/applicationinstallers/blazemonster/BlazeMonster.air).

Then, launch the BlazeMonster and point it to your web application, we will see the UI as follows,

http://sacct-users.googlegroups.com/web/BlazeMonster.png?gda=kwZ__EMAAABwsiXcufpgHYK0Fq-t18-VWQHwp0ITWfIVDRuUB7oKG-BgIm08D66Zub3Qbeyhf_gytiJ-HdGYYcPi_09pl8N7FWLveOaWjzbYnpnkpmxcWg&gsc=JQ3SnwsAAABu3-D6c61F463lHbtjmjTX

Here, I used the sample web application in the BlazeDS turnkey bundle.

If you click on the generate code button, you will see the code in the following pop-up window,

http://sacct-users.googlegroups.com/web/BlazeMonsterGenerateCode.png?gda=EsXWMU8AAABwsiXcufpgHYK0Fq-t18-VobQ7jwDT6-UPKZ2bFVq-ISRT-0g7S8VhQnVGrzTt-Lk3MdYt7p2aWs5rVIz3g0D8nHMhSp_qzSgvndaTPyHVdA&gsc=XD7ZcwsAAAAo2AYPwJrdmwF0uBWU8c_V

## Resources ##

  * [Adobe AIR](http://get.adobe.com/air/)
  * [BlazeDS](http://opensource.adobe.com/wiki/display/blazeds/Release+Builds)
  * [Blaze Monster](http://sujitreddyg.wordpress.com/2009/05/07/blazemonster/)
  * [Flash Builder 4 beta](http://labs.adobe.com/technologies/flashbuilder4/)
  * [FlexMonkey](http://code.google.com/p/flexmonkey/)
  * [fluint - Flex Unit and Integration Testing framework](http://code.google.com/p/fluint/)
  * [FlexUnit](http://opensource.adobe.com/wiki/display/flexunit/FlexUnit)