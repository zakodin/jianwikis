Sometimes, we create a self-signed cert without specifying which host it binds to. The cert may look like as follows in SoupUI

```
CipherSuite:
SSL_RSA_WITH_RC4_128_MD5
PeerPrincipal:
EMAILADDRESS=jian.fang@mycompany.com, CN=Server, OU=IT, O=Mycompany, L=CITY, ST=STATE, C=US
Peer Certificate 1:
[
[
  Version: V3
  Subject: EMAILADDRESS=jian.fang@jtv.com, CN=CoreServer, OU=IT, O=JTV, L=Knoxville, ST=TN, C=US
  Signature Algorithm: SHA1withRSA, OID = 1.2.840.113549.1.1.5

  Key:  Sun RSA public key, 2048 bits
  modulus: 29704319558460839930768270956795123030162010294273915833888408835424637131012853787449179433626588880734705610286116744898139486749643041136596039186132550022507289958098222623712292899617832798705895906207251922966771609841156335123152016240757228878409348829742071395241204409155124586001758694057064758251458305383317357931024928526993937376325291623378343714489745152658729763212214396153777231579534086981854954868338987371006861700203089715375936379238062962388256233770015171729365171337414803157958789982969592995073053945852541635020870230430641736723741644918191169930143001372760787981731713951969628457027
  public exponent: 65537
  Validity: [From: Wed Jun 17 13:24:56 GMT-05:00 2009,
               To: Thu Jun 17 13:24:56 GMT-05:00 2010]
  Issuer: EMAILADDRESS=jian.fang@Mycompany, CN=Server, OU=IT, O=Mycompany, L=CITY, ST=STATE, C=US
  SerialNumber: [    ca31be22 0594f835]

Certificate Extensions: 3
[1]: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: 93 B9 54 B8 89 87 DC 3A   46 7E C6 71 79 1A 9E 36  ..T....:F..qy..6
0010: 64 52 02 F5                                        dR..
]
]

[2]: ObjectId: 2.5.29.35 Criticality=false
AuthorityKeyIdentifier [
KeyIdentifier [
0000: 93 B9 54 B8 89 87 DC 3A   46 7E C6 71 79 1A 9E 36  ..T....:F..qy..6
0010: 64 52 02 F5                                        dR..
]

[EMAILADDRESS=jian.fang@mycompany.com, CN=Server, OU=IT, O=Mycompany, L=CITY, ST=STATE, C=US]
SerialNumber: [    ca31be22 0594f835]
]

[3]: ObjectId: 2.5.29.19 Criticality=false
BasicConstraints:[
  CA:true
  PathLen:2147483647
]

]
  Algorithm: [SHA1withRSA]
  Signature:
0000: C9 A6 FA A9 59 F8 1F EF   DB A0 E5 33 1A 68 C2 55  ....Y......3.h.U
0010: B8 AF 0C DD 9D 52 55 DE   A4 4B 47 AB 11 54 6E 11  .....RU..KG..Tn.
0020: 60 41 1C 9F 00 40 7B 8F   A7 84 9B AD 3B 27 B0 04  `A...@......;'..
0030: 12 0D BE 8B 8A CF B7 F3   25 54 C2 38 BA D7 6B 0D  ........%T.8..k.
0040: 44 B4 C8 31 DE 85 23 52   58 A4 1E E1 AF DC 73 7A  D..1..#RX.....sz
0050: 08 2E 55 64 C1 58 7B AB   B6 7E D4 43 97 00 FA C5  ..Ud.X.....C....
0060: EC 13 C3 FE 87 A0 E6 B9   7A 20 F6 4F 50 A3 78 7A  ........z .OP.xz
0070: 1F 42 DB D3 1B C7 85 A1   20 E8 9F A9 FF 1F 28 17  .B...... .....(.
0080: 8F 71 08 C6 20 22 CA 93   C0 99 18 14 18 F5 04 89  .q.. "..........
0090: FC 9E 7C 14 43 AD A7 EC   12 2A B8 C0 48 33 DE 59  ....C....*..H3.Y
00A0: 8A F6 9D 69 D0 36 3C 83   0B D8 44 7C 22 02 BD 88  ...i.6...D."...
00B0: 62 D3 F2 14 DB D7 D4 AB   0E 25 53 5F 24 04 5A C5  b........%S_$.Z.
00C0: 05 CA 17 04 A7 5A FE 02   E4 4A D3 47 E4 10 42 0A  .....Z...J.G..B.
00D0: 79 59 FC A5 5E AA 6B F7   B8 09 75 E5 F4 1F 29 25  yY..^.k...u...)%
00E0: 96 72 E0 E4 F0 F2 9C 22   24 B0 E7 3C 9C 26 1C CE  .r....."$...&..
00F0: 6A 51 D4 86 F2 A5 5C 9E   08 55 80 BA A6 68 28 55  jQ....\..U...h(U

]
```

If you use this cert, some client may be picky about it because the client
tries to validate the host, but only sees the email address.

One way to get around this is to ask Https to not validate the host name. You
can achieve this using the following code

```
class NullHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}

HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
```

The Java doc for the above method could be found at

http://java.sun.com/j2se/1.5.0/docs/api/javax/net/ssl/HttpsURLConnection.html