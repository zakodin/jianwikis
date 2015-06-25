

## Setup remote X window client ##

To use the graphical configuration tool in Redhat/Centos (I will refer it to Redhat 5 since Centos is a clone of it) such as system-config-services, system-config-cluster, you need to be able to run X window in your local Linux box to configure the remote Redhat server. Here, I will use appserv35 as the remote Redhat server and my laptop 192.168.159.24 (Linux) as my local box.

First, check the DISPLAY attribute

```
[jiafan1@localhost ~]$ echo $DISPLAY
:0.1
```

Then, check if my local box X server is listening for remote request

```
ps -ef | grep listen
```

and see if it returns your Xserver daemon. If it returns a line with "-nolisten" in it (the default for lots of distros), you'll have to change your X configuration to remove the -nolisten option.

For Fedora Linux, modify /etc/gdm/custom.conf to add DisallowTCP=false in /etc/gdm/custom.conf to make gdm listen as follows,

```
[security]
DisallowTCP=false
```

Also add the port 6000 to firewall rule file /etc/sysconfig/iptables

```
-A RH-Firewall-1-INPUT -p tcp -m tcp --dport 6000 -j ACCEPT
```

After that, reboot to restart the X server.

Do the following to allow X server for remote connections

```
[jiafan1@localhost ~]$ xhost +
access control disabled, clients can connect from any host
```

At this point, you can log to the remote Redhat Server

```
[jiafan1@localhost ~]$ ssh -Y jfang@appserv35
```

Sudo as root:

```
[jfang@appserv35 ~]$ sudo su - 
```

Set up the DISPLAY environment variable on appserv35 to remote client (my Linux box)

```
[root@appserv35 ~]#  export DISPLAY=192.168.159.24:0.1
```

After that, you can use the graphical tools in appserv35 to display on your local machine.

## Create gfs file system ##

We have a hard drive /etc/emcpowera with Fabric Connection. First create the file system

```
[root@appserv35 /]# gfs_mkfs -p lock_dlm -t build:gfs1 -j 8 /dev/emcpowera

This will destroy any data on /dev/emcpowera.

Are you sure you want to proceed? [y/n] y

Device:                    /dev/emcpowera
Blocksize:                 4096
Filesystem Size:           17415600
Journals:                  8
Resource Groups:           266
Locking Protocol:          lock_dlm
Lock Table:                build:gfs1

Syncing...
All Done
```

That is to say, we use this device for cluster name "build"

## Configure the Redhat Cluster ##

Run cluster configuration tool

```
[root@appserv35 ~]# /usr/sbin/system-config-cluster
```

to setup the cluster name, cluster nodes, fence devices, resources and services as shown in /etc/cluster/cluster.conf

```
<?xml version="1.0" ?>
<cluster alias="build" config_version="25" name="build">
        <fence_daemon clean_start="1" post_fail_delay="0" post_join_delay="30"/>
        <clusternodes>
                <clusternode name="appserv35" nodeid="1" votes="1">
                        <fence>
                                <method name="1">
                                        <device lanplus="" name="appserv35-ilom"/>
                                </method>
                        </fence>
                </clusternode>
                <clusternode name="appserv34" nodeid="2" votes="1">
                        <fence>
                                <method name="1">
                                        <device lanplus="" name="appserv34-ilom"/>
                                </method>
                        </fence>
                </clusternode>
        </clusternodes>
        <cman expected_votes="1" two_node="1"/>
        <fencedevices>
                <fencedevice agent="fence_ipmilan" auth="password" ipaddr="192.168.10.67" login="root" name="appserv34-ilom" passwd="P@ssw0rd"/>
                <fencedevice agent="fence_ipmilan" auth="password" ipaddr="192.168.10.68" login="root" name="appserv35-ilom" passwd="P@ssw0rd"/>
                <fencedevice agent="fence_ipmilan" auth="password" ipaddr="192.168.10.130" login="root" name="appserv36-ilom" passwd="P@ssw0rd"/>
        </fencedevices>
        <rm>
                <failoverdomains/>
                <resources>
                        <clusterfs device="/dev/emcpowera" force_unmount="0" fsid="14332" fstype="gfs" mountpoint="/mnt/gfs" name="builddisk" options=""/>
                </resources>
                <service autostart="1" name="buildservice">
                        <clusterfs ref="builddisk"/>
                </service>
        </rm>
</cluster>
```


Enable the cluster services by running

```
[root@appserv35 ~]#  /usr/sbin/system-config-services
```

make sure clvmd, cman, gfs, luci, modclusterd, rgmanager, and ricci are checked to automatically start after reboot

Modify firewall rules to allow multicast connections for cluster nodes, i.e., add the following line to /etc/sysconfig/iptables

```
-A RH-Firewall-1-INPUT -p udp -d 239.195.2.35 -j ACCEPT
```

Then restart the firewall,

```
[root@appserv35 ~]#  service iptables restart
```

Change cluster manager bindaddress

```
[root@appserv35 ~]# vi /etc/ais/openais.conf
```

and change the bindaddress as follows,

```
totem {
        version: 2
        secauth: off
        threads: 0
        interface {
                ringnumber: 0
                bindnetaddr: 192.168.2.0   <--this address, the last digit of the address is always zero
                mcastaddr: 226.94.1.1
                mcastport: 5405
        }
}
```

Start cluster

```
[root@appserv35 ~]# service cman start
Starting cluster: 
   Enabling workaround for Xend bridged networking... done
   Loading modules... done
   Mounting configfs... done
   Starting ccsd... done
   Starting cman... done
   Starting daemons... done
   Starting fencing... done
                                                           [  OK  ]
```

Check cluster status

```
[root@appserv35 ~]# cman_tool nodes
Node  Sts   Inc   Joined               Name
   1   M      8   2008-12-15 13:53:25  appserv35

After the gfs disk is mounted, you can check the services as follows,

[root@appserv35 ~]# cman_tool services
type             level name     id       state       
fence            0     default  00010001 none        
[1]
dlm              1     gfs1     00050001 none        
[1]
gfs              2     gfs1     00040001 none        
[1]
```

The status is shown as follows,

```
[root@appserv35 ~]# cman_tool status
Version: 6.1.0
Config Version: 8
Cluster Name: build
Cluster Id: 3240
Cluster Member: Yes
Cluster Generation: 8
Membership state: Cluster-Member
Nodes: 1
Expected votes: 1
Total votes: 1
Quorum: 1  
Active subsystems: 7
Flags: Dirty 
Ports Bound: 0  
Node name: appserv35
Node ID: 1
Multicast addresses: 239.195.2.35 
Node addresses: 192.168.2.49 
```

As we see, we only have one node in the cluster for the time being.

## Mount the gfs disk ##

If you want to manually mount the disk, use the following command

```
[root@appserv35 ~]# mount -t gfs -o lockproto=lock_dlm /dev/emcpowera /mnt/gfs
```

Check the status

```
[root@appserv35 ~]# df
Filesystem           1K-blocks      Used Available Use% Mounted on
/dev/md2              53091004   2541368  47809264   6% /
/dev/md0                 99035     19460     74462  21% /boot
tmpfs                  8142244         0   8142244   0% /dev/shm
/dev/emcpowera        69662400        48  69662352   1% /mnt/gfs
```

You should add the mount point to /etc/fstab so that the disk will be automatically mounted after reboot,

```
/dev/md2                /                       ext3    defaults        1 1
/dev/md0                /boot                   ext3    defaults        1 2
tmpfs                   /dev/shm                tmpfs   defaults        0 0
devpts                  /dev/pts                devpts  gid=5,mode=620  0 0
sysfs                   /sys                    sysfs   defaults        0 0
proc                    /proc                   proc    defaults        0 0
/dev/md1                swap                    swap    defaults        0 0
/dev/emcpowera          /mnt/gfs                gfs     defaults        0 0   <---this line 
```

Sometimes, you want to mount the gfs system as a local disk instead of a cluster file system, you can use the following command

```
 mount -t gfs -o lockproto=lock_nolock /dev/emcpowera /mnt/gfs
```