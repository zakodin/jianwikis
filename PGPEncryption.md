

# Introduction #

I like to provide some guides on how to create PGP keys and how to use PGP to encrypt and decrypt messages as well as signing and verifying signatures.

# Prerequisites #

Your system should have PGP software installed. In Linux system, you can use GnuPG and most Linux systems have GnuPGP installed by default. If not, you can download the rpm or use "yum" to install it.

# Key Management #

Assume we have two users "Pgp Sender" and "Pgp Receiver" in all the following examples.

## Generate Key ##

```
[pgpsender@ ~]$ gpg --gen-key
gpg (GnuPG) 1.4.7; Copyright (C) 2006 Free Software Foundation, Inc.
This program comes with ABSOLUTELY NO WARRANTY.
This is free software, and you are welcome to redistribute it
under certain conditions. See the file COPYING for details.

gpg: directory `/home/pgpsender/.gnupg' created
gpg: new configuration file `/home/pgpsender/.gnupg/gpg.conf' created
gpg: WARNING: options in `/home/pgpsender/.gnupg/gpg.conf' are not yet active during this run
gpg: keyring `/home/pgpsender/.gnupg/secring.gpg' created
gpg: keyring `/home/pgpsender/.gnupg/pubring.gpg' created
Please select what kind of key you want:
   (1) DSA and Elgamal (default)
   (2) DSA (sign only)
   (5) RSA (sign only)
Your selection? 1
DSA keypair will have 1024 bits.
ELG-E keys may be between 1024 and 4096 bits long.
What keysize do you want? (2048) 
Requested keysize is 2048 bits
Please specify how long the key should be valid.
         0 = key does not expire
      <n>  = key expires in n days
      <n>w = key expires in n weeks
      <n>m = key expires in n months
      <n>y = key expires in n years
Key is valid for? (0) 
Key does not expire at all
Is this correct? (y/N) y

You need a user ID to identify your key; the software constructs the user ID
from the Real Name, Comment and Email Address in this form:
    "Heinrich Heine (Der Dichter) <heinrichh@duesseldorf.de>"

Real name: Pgp Sender
Email address: Pgp.Sender@telluriumsource.org
Comment: PGP Sender
You selected this USER-ID:
    "Pgp Sender (PGP Sender) <Pgp.Sender@telluriumsource.org>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
You need a Passphrase to protect your secret key.

We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.
++++++++++++++++++++++++++++++.++++++++++.+++++...++++++++++++++++++++++++++++++++++++++++.+++++++++++++++++++++++++++++++++++++++++++++>.+++++.+++++..................+++++

Not enough random bytes available.  Please do some other work to give
the OS a chance to collect more entropy! (Need 267 more bytes)

We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.
++++++++++.+++++++++++++++++++++++++..++++++++++..++++++++++.+++++...++++++++++++++++++++++++++++++......+++++++++++++++++++++++++++++++++++++++++++++.++++++++++>+++++..+++++>+++++........................................................................................+++++^^^^^
gpg: /home/pgpsender/.gnupg/trustdb.gpg: trustdb created
gpg: key 8A56628C marked as ultimately trusted
public and secret key created and signed.

gpg: checking the trustdb
gpg: 3 marginal(s) needed, 1 complete(s) needed, PGP trust model
gpg: depth: 0  valid:   1  signed:   0  trust: 0-, 0q, 0n, 0m, 0f, 1u
pub   1024D/8A56628C 2009-05-11
      Key fingerprint = 9F7F 968D 71D8 39A6 7BD2  53EB 4EDD 123D 8A56 628C
uid                  Pgp Sender (PGP Sender) <Pgp.Sender@telluriumsource.org>
sub   2048g/9275EE43 2009-05-11
```

## Export Public Key ##

```
  
[pgpsender@ ~]$  gpg --export --armor > PGPSender.asc

[pgpsender@ ~]$ cat PGPSender.asc 
-----BEGIN PGP PUBLIC KEY BLOCK-----
Version: GnuPG v1.4.7 (GNU/Linux)

mQGiBEoIe2kRBACQp9YyMOdS5oEAupc7t4MgSD4BQMiwl8+YqDHudDo2rlLqHjxi
WbVTV2p4QzwzH7JXp59NrHvF9Pt4Sh8Yt06br7oChgSJY14gzTxBN0C5YhZVgNem
avdqGdgH1elpRF3pfRLhhw3IwkxkrIeCA6Knb2m2HkfZqvw96E1N/iBTkwCg7rot
nEoh+wiUq5M4gTujx9MfsYsD/Rm57y7GDyB7jPD81A4MVMADr7gMEFR8qV+4RwyL
no8ILWKugce2Amo9Hp5B7JrXWbQzlKD6vLt8ylfY3Z/uEd/C3zBzNqul8S1Cak9j
U4pC0iijGfPCbQjDyQl+9WRvTc957REpjz5zlLfJNLfQ9/in7b+HZWOIYWZ2Htgn
6SRBA/9ZTw0Kaxx/2iF6UGMNA4eSCcU5b/9CCiy2E/042Nm0X9fXPpeqcbeXK2Hq
eFneWdQd5rvIgqNtvUTJez3gohPLcovzjkz6cCPgs0GTm4kbzvf9KqPYQlbiTFzX
i710OAsC3CHLlkbKX4FmL4cfwibVasqa3zUbBk3oL8+oqhAZLLQtUGdwIFNlbmRl
ciAoUEdQIFNlbmRlcikgPFJhbS5HdW1tYWRpQEpUVi5jb20+iGAEExECACAFAkoI
e2kCGwMGCwkIBwMCBBUCCAMEFgIDAQIeAQIXgAAKCRBO3RI9ilZijCD8AJ4nhTJX
5hVX2g1X4J6wa1I0bbbGngCfeyGyHfcB85xqS61C6J5nDoRLcHW5Ag0ESgh7aRAI
AKtF4X3Qig7vh0a/NUTXPYo4xJf7mIL++6TwSSfrcG/+wcjoZmVSag+iKUQMYbQW
mbHxNDwegC95OEvkfoC4h/KWJR6o6TEdPd8Ll7fQtF4M4nHzTrVgP4REgKyBszgs
+GyDy/8i3Gz7c9L6q0rV2SZiDXioRfC1pj/9HbrpfBL3UKheR7Ekee/ZJA/v7WJ8
h8tm26OnYqqTCHQyLbqRZovRPkqJLC2pRh+LahZZYjVD0/1Du/U3pIiQN/jHF4/p
xAuNegD5ATpuPm5DxYyo5IBnGpCEPvfU4OweWzWpAQkoU9pVGS4t0NOYljNbZBIc
CRcs/+3JZ3uM8JGRlFulsy8AAwcH/1CPCgkrnWOm5TJ5u+aQto0IeNXlxlAzIxbh
/3TWDaSvsJ2TBg34ICXDO8tfBV5xEzicCWPr4RicPgu339rMgTDkbH0MTCWXHphG
/39X7xaa04f9yOgdMf6H6WTzSwS+DI1Qw0qP0q57eJKosZ+8Ik31r6DOv0zcxpWg
meShb8LZoxgMpE0gDPiiNVPDyV3d/fVTT1M2jtBr90BmgcODsnaNF9qFJIyt5YI9
lqWdkYX/Zga9Nn3+77a4Beaz2bmj54XTVEgTOju96+G03/Yx5Hevf8yk1tY1697U
G90gUsfsUiCPefeHiqvU3slnGRzS4G+66A47mlbA1+DdjgWXQY+ISQQYEQIACQUC
Sgh7aQIbDAAKCRBO3RI9ilZijO41AJ4npFJNNwX97SR1hqETO68ZzRu2TwCg030g
rp46alPjGhTKZyL3OeB395Q==hET8
-----END PGP PUBLIC KEY BLOCK---

```


Similarly, you can use the following command to export your private key.

```
   gpg --armor --export-secret-keys Pgp.Sender@telluriumsource.org  > PGPSender.privatekey.asc
```

## Import Public Key ##

For the user "Pgp Sender", he needs to import the public key of the user "Pgp Receiver" into his key store.

```
[pgpsender@ ~]$  gpg --import PGPReceiver.asc 
gpg: key 04150FA1: public key "Pgp Receiver (PGP Receiver) <Pgp.Receiver@telluriumsource.org>" imported
gpg: Total number processed: 1
gpg:               imported: 1

```

After Import the public key, you can use the following command to check the public keys in the key store.

```
[pgpsender@ ~]$ gpg --list-keys
/home/pgpsender/.gnupg/pubring.gpg
----------------------------------
pub   1024D/8A56628C 2009-05-11
uid                  Pgp Sender (PGP Sender) <Pgp.Sender@telluriumsource.org>
sub   2048g/9275EE43 2009-05-11

pub   1024D/04150FA1 2009-05-11
uid                  Pgp Receiver (PGP Receiver) <Pgp.Receiver@telluriumsource.org>
sub   2048g/E4D0760D 2009-05-11

```

## Sign and Encrypt Messages ##

First, create a message file,

```
[pgpsender@ ~]$ echo "This is a test message......" > message.txt
```

Then, you can sign and encrypt the message,

```
[pgpsender@ ~]$ gpg -sear "pgp Receiver" message.txt

You need a passphrase to unlock the secret key for
user: "Pgp Sender (PGP Sender) <Pgp.Sender@telluriumsource.org>"
1024-bit DSA key, ID 8A56628C, created 2009-05-11

gpg: E4D0760D: There is no assurance this key belongs to the named user

pub  2048g/E4D0760D 2009-05-11 Pgp Receiver (PGP Receiver) <Pgp.Receiver@telluriumsource.org>
 Primary key fingerprint: 8FD4 6A17 C897 C699 4B7E  8238 7F5A 80EC 0415 0FA1
      Subkey fingerprint: F586 ABCB 3A63 8ED5 A44D  DAE2 CC95 4DC1 E4D0 760D

It is NOT certain that the key belongs to the person named
in the user ID.  If you *really* know what you are doing,
you may answer the next question with yes.

Use this key anyway? (y/N) y

```

Now, you can check the generated file.

```
[pgpsender@ ~]$ cat message.txt.asc 
-----BEGIN PGP MESSAGE-----
Version: GnuPG v1.4.7 (GNU/Linux)

hQIOA8yVTcHk0HYNEAf/RJ9JPbkczsOhlcFVDO98fTDfT/tGU7qGBaQwOX3fMWdc
M905sZrj8UOiFWcsfgsgeQKt9ZOGQKD3qBjqUpk/YlH21zvNU4B7i8id8il9BT3d
nCd6YucLJjQs/Q6PXELIIY13eeqgeNaJOUB1g6ZslOZDAnO1ERGfLD9mF7RCqb0f
5HmNSEVgmylJsYoUcVSKMCcZZM6nLhRUkJPzhxsG38BKQ00UnT8qGJOoT3FSTDQu
hmyHaC3FDxFGalfhws8pc1U21N+wEsv4b6N+Gnc5ksEouKNxdeWUPlbSPzp1EiIT
gb/OPOiB1/xZ++uQa9wvXFRHAkrj+bwh4sm69m/xkwf/eGYtl5Pe6g/xC9zYNJ/v
zuoZ3fRwyWVd/IhePpUgbGiTLyTmOl2BaeGZIdh8GTTMdkt81vTCYcwvgBoqhpm4
ji5ur7ZN7UrQK5dL+YMgPDs64eL/D8DzihJCsGTdrGQD3R/pyzLOtWwpdpvYkQrB
Vri9tnuVd8ze84KsU3/GUk39EqJmT1YpeRypmm1MHMaeKgCRjEFgACpKbcDzvFx9
Tq70s98MdjZRvsUVAgaAqEwWog/XG7K4Sg4E7660zy+OrYJ7Pj1JjDk0JRMrZUIW
b0uiCZj2iPGSlTePwsIzmNBcNXMC1x2PcSTFMa9cHI70sRca8vgDlrl4lUl4FS5q
btKiAepG/jB+gE9ScHqjTLzOrNQhi9kdz3nSkfZ+qKQi9OvZJz1uBmwZK6uSnCxp
CckaU8Sa+GNeK93iYANB4lou939C1KnJmY5jr6UQy/zlv8Cmx+G6MVk4d6+deEkE
MAJgxKPNjcUUUQoqpm1LD4Bmwb46Ie9GpOlhizgZnhQ1yZlWfgM2sCFRhRkQpw56
ODzOzUQFqV7I5zOYOnCAS+jmw9vl=BsbH
-----END PGP MESSAGE-----
```

## Decrypt Messages ##

The message recipient "Pgp Receiver" should import the sender's public key into his key store and then decrypt the message.

```
[pgpreceiver@ ~]$ gpg -d message.txt.asc 

You need a passphrase to unlock the secret key for
user: "Pgp Receiver (PGP Receiver) <Pgp.Receiver@telluriumsource.org>"
2048-bit ELG-E key, ID E4D0760D, created 2009-05-11 (main key ID 04150FA1)

gpg: encrypted with 2048-bit ELG-E key, ID E4D0760D, created 2009-05-11
      "Pgp Receiver (PGP Receiver) <Pgp.Receiver@telluriumsource.org>"
This is a test message......
gpg: Signature made Mon 11 May 2009 03:45:21 PM EDT using DSA key ID 8A56628C
gpg: Good signature from "Pgp Sender (PGP Sender) <Pgp.Sender@telluriumsource.org>"
gpg: WARNING: This key is not certified with a trusted signature!
gpg:          There is no indication that the signature belongs to the owner.
Primary key fingerprint: 9F7F 968D 71D8 39A6 7BD2  53EB 4EDD 123D 8A56 628C

```

## Trust Public Key ##

You may notice that the GnuPG warns you that the key may not be trusted. The reason is that you have not chosen to trust it yet. You should contact the sender to validate his public key and then do the following step to trust his key.

```

[pgpreceiver@dt-acalhoun ~]$ gpg --edit-key 8A56628C
gpg (GnuPG) 1.4.7; Copyright (C) 2006 Free Software Foundation, Inc.
This program comes with ABSOLUTELY NO WARRANTY.
This is free software, and you are welcome to redistribute it
under certain conditions. See the file COPYING for details.


pub  1024D/8A56628C  created: 2009-05-11  expires: never       usage: SC  
                     trust: unknown       validity: unknown
sub  2048g/9275EE43  created: 2009-05-11  expires: never       usage: E   
[ unknown] (1). Pgp Sender (PGP Sender) <Pgp.Sender@telluriumsource.org>

Command> trust
pub  1024D/8A56628C  created: 2009-05-11  expires: never       usage: SC  
                     trust: unknown       validity: unknown
sub  2048g/9275EE43  created: 2009-05-11  expires: never       usage: E   
[ unknown] (1). Pgp Sender (PGP Sender) <Pgp.Sender@telluriumsource.org>

Please decide how far you trust this user to correctly verify other users' keys
(by looking at passports, checking fingerprints from different sources, etc.)

  1 = I don't know or won't say
  2 = I do NOT trust
  3 = I trust marginally
  4 = I trust fully
  5 = I trust ultimately
  m = back to the main menu

Your decision? 5
Do you really want to set this key to ultimate trust? (y/N) y

pub  1024D/8A56628C  created: 2009-05-11  expires: never       usage: SC  
                     trust: ultimate      validity: unknown
sub  2048g/9275EE43  created: 2009-05-11  expires: never       usage: E   
[ unknown] (1). Pgp Sender (PGP Sender) <Pgp.Sender@telluriumsource.org>
Please note that the shown key validity is not necessarily correct
unless you restart the program.

Command> quit

```

After that, try to decrypt the message again and you will see a different message.

```
[pgpreceiver@ ~]$ gpg -d message.txt.asc 

You need a passphrase to unlock the secret key for
user: "Pgp Receiver (PGP Receiver) <Pgp.Receiver@telluriumsource.org>"
2048-bit ELG-E key, ID E4D0760D, created 2009-05-11 (main key ID 04150FA1)

gpg: encrypted with 2048-bit ELG-E key, ID E4D0760D, created 2009-05-11
      "Pgp Receiver (PGP Receiver) <Pgp.Receiver@telluriumsource.org>"
This is a test message......
gpg: Signature made Mon 11 May 2009 03:45:21 PM EDT using DSA key ID 8A56628C
gpg: checking the trustdb
gpg: 3 marginal(s) needed, 1 complete(s) needed, PGP trust model
gpg: depth: 0  valid:   2  signed:   0  trust: 0-, 0q, 0n, 0m, 0f, 2u
gpg: Good signature from "Pgp Sender (PGP Sender) <Pgp.Sender@telluriumsource.org>"

```

# PGP Java APIs #

[Bouncy Castle](http://www.bouncycastle.org/java.html) provides PGP Java API. You can download it from Bouncy Castle website, or use the following Maven dependency.

```
        <dependency>
            <groupId>bouncycastle</groupId>
            <artifactId>bcprov-jdk15</artifactId>
            <version>140</version>
        </dependency>
        <dependency>
            <groupId>bouncycastle</groupId>
            <artifactId>bcpg-jdk15</artifactId>
            <version>140</version>
        </dependency>
```

# Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy #

For PGP, some encryption key such as Elgamal key is longer than 1024. In order to use the strong encryption, you will need to use Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 6 in your JRE.
  * Download the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 6
    * Go to this page: [Java SE Downloads](http://java.sun.com/javase/downloads/index.jsp).
    * Locate the Other Downloads section.
    * Download the jce\_policy-6.zip file.
  * Locate your JDK installation and navigate to the jre/lib/security folder.
  * Copy the local\_policy.jar and US\_export\_policy.jar files to a backup location.
  * Unpack the jce\_policy-6.zip and copy the previous two jars into the jre/lib/security folder.

# PGP Email Plugins #

[Enigmail](http://enigmail.mozdev.org/home/index.php) is a PGP Email plugin for Thunderbird and it is a .xpi file. You can simply download it and install it just like installing a Firefox plugin.

For Windows system, you can try [Gpg4win](http://www.gpg4win.org/) or [Crypto Anywhere](http://www.gnu.org/software/software.html)