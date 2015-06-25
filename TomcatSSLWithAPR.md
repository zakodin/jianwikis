

# Introduction #

This article will introduce how to configure Tomcat to support SSL connection with Apr based on my experience. The Apache Portable Runtime (APR) based Native library for Tomcat is used to optimize tomcat performance.

## Tomcat SSL without APR ##

Without APR, you can put everything into Java key store and the configuration is simple. You should turn on the SSL connector in server.xml as follows,

```
 <Connector port="8443"
     maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
     enableLookups="false" disableUploadTimeout="true"
     acceptCount="100" scheme="https" secure="true"
     clientAuth="false" sslProtocol="TLS"
     keystoreFile="conf/keystore.jks" keystorePass="keystore_password"
     truststoreFile="conf/truststore.jks" truststorePass="truststore_password"/>
```

## Tomcat SSL with APR ##

With APR, you have to use openssl and have a different set of parameters in the SSL connector configuration. I will go over them in details.

### Check if APR is Available ###

In Unix/Linux, you can check TOMCAT\_HOME/native/lib to see if you have the native library files such as libtcnative-1.so.0.1.12 and libapr-1.so.0.3.3. To install the native library, you can install tomcat-native RPM or compile from source. Usually, the files under this native/lib directory are symbolic links and you need to check if they are really linked to the correct native library. For example,

```
[jfang@localhost lib]$ ldd libtcnative-1.so.0.1.12
        linux-vdso.so.1 =>  (0x00007fff567ff000)
        libssl.so.6 => not found
        libcrypto.so.6 => not found
        libapr-1.so.0 => /usr/lib64/libapr-1.so.0 (0x00007f564e1d8000)
        libuuid.so.1 => /lib64/libuuid.so.1 (0x00007f564dfd4000)
        librt.so.1 => /lib64/librt.so.1 (0x00007f564ddcb000)
        libcrypt.so.1 => /lib64/libcrypt.so.1 (0x00007f564db94000)
        libpthread.so.0 => /lib64/libpthread.so.0 (0x00007f564d978000)
        libdl.so.2 => /lib64/libdl.so.2 (0x00007f564d773000)
        libc.so.6 => /lib64/libc.so.6 (0x00007f564d405000)
        /lib64/ld-linux-x86-64.so.2 (0x0000003412e00000)
        libfreebl3.so => /lib64/libfreebl3.so (0x00007f564d1a7000)
```

Obviously, the libssl.so.6 and libcrypto.so.6 are missing. You need to link them to the correct library files until the ldd command shows all dependent library files are linked correctly. If not, tomcat will use default Http11BaseProtocol instead of Http11AprProtocol.

### SSL Certs ###

#### Cert CFG File ####

You can first create a cert configuration file. For example, I use the following Tellurium-Cert.cfg file assuming our [Tellurium Automated Testing Framework](http://code.google.com/p/aost/) needs a server with SSL support:

```
[jfang@localhost]$ cat Tellurium-Cert.cfg 
 [ req ]
 default_bits           = 1024
 default_keyfile        = key.pem 
 distinguished_name     = req_distinguished_name
 attributes             = req_attributes
 req_extensions         = v3_req
 prompt                 = no

 [ req_distinguished_name ]
 C                      = US
 ST                     = Atlanta
 L                      = Georgia
 O                      = Tellurium Framework 
 OU                     = Information Technology
 CN                     = AppServer 
 emailAddress           = John.Jian.Fang@tellurium.org

 [ req_attributes ]
 challengePassword              = P@ssw0rd

 [ v3_req ]
 #Extensions to add to a certificate request

 basicConstraints = CA:FALSE
 keyUsage = nonRepudiation, digitalSignature, keyEncipherment

 subjectAltName = @alt_names

 [alt_names]
 DNS.1                  = appserver-vip.tellurium.org
 DNS.2                  = appserver1.tellurium.org
 DNS.3                  = appserver2.tellurium.org
```

Here, I assume that the server is a cluster with VIP appserver-vip.tellurium.org and physical nodes appserver1.tellurium.org and appserver2.tellurium.org.

#### Generate Private Key and Cert Request ####

```
[jfang@localhost]$ openssl req -new -out cert.csr -config Tellurium-Cert.cfg
Generating a 1024 bit RSA private key
.........++++++
....................................................................................................................................................................................................................................................++++++
writing new private key to 'key.pem'
Enter PEM pass phrase:
Verifying - Enter PEM pass phrase:
-----
```

After you create the cert request file cert.csr, you can send it to your CA to sign it or you can generate a self-signed certificate.

#### Self-Signed Certificate ####

First, export the private key to unencrypted one.

```
[jfang@localhost]$ openssl rsa -in key.pem -out server.key
Enter pass phrase for key.pem:
writing RSA key
```

Then, create a self-signed certificate

```
[jfang@localhost]$ openssl x509 -in cert.csr -out server.crt -req -signkey server.key -days 365
Signature ok
subject=/C=US/ST=Tennessee/L=Knoxville/O=Tellurium Framework/OU=Information Technology/CN=AppServer/emailAddress=John.Jian.Fang@tellurium.org
Getting Private key
```

You will see the self-signed cert sits right there,

```
[jfang@localhost]$ ll
-rw-rw-r--. 1 jfang jfang  972 2009-06-25 14:30 cert.csr
-rw-rw-r--. 1 jfang jfang  963 2009-06-25 14:30 key.pem
-rw-rw-r--. 1 jfang jfang 1062 2009-06-25 14:34 server.crt
-rw-rw-r--. 1 jfang jfang  887 2009-06-25 14:32 server.key
-rw-rw-r--. 1 jfang jfang  961 2009-06-25 14:26 Tellurium-Cert.cfg
```

#### Combining Files ####

Optionally, you can combining a Self-Signed Certificate and a Private Key into one file in "PKC12" format,

```
[jfang@localhost]$ openssl pkcs12 -export -in server.crt -inkey key.pem -out server.p12 -name "tomcat"
Enter pass phrase for key.pem:
Enter Export Password:
Verifying - Enter Export Password:
```

#### CA and CA Chain ####

If your cert is signed by you CA and you have multiple CAs along the CA chain, you can combine the CAs into one file as follows,

```
[jfang@localhost]$ cat issuing-ca.crt root-ca.crt | sed -e 's/-----END CERTIFICATE-----/-----END CERTIFICATE-----\n/g' > authorities.crt
```

Here I assume you have the following CA Chain

```
  Server Cert

      |

  Issuing CA

      |

  Root CA
```

#### Verify Cert ####

Now verify that your certificate works fine against the combined CA file

```
[jfang@localhost]$ openssl verify -CAfile authorities.crt -verbose server.crt
server.crt: OK
```

#### Combine to One File ####

Optionally, you can combine everything into one file as follows,

```
[jfang@localhost]$ openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12 -name "tomcat" -CAfile authorities.crt -caname root-ca.crt -chain
Enter Export Password:
Verifying - Enter Export Password:
```

### Configure SSL Connector ###

Tomcat with APR has to use a different set of parameters for the SSL connector. A typical SSL connector in server.xml is as follows,

```
<Connector port="8443" maxHttpHeaderSize="8192"
              maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
              enableLookups="false" disableUploadTimeout="true"
              acceptCount="100" scheme="https" secure="true"
              clientAuth="false"
              sslProtocol="TLS"
              SSLEngine="on"
              SSLEnabled="true"
              SSLPassword="private_key_password"
              SSLCertificateFile="${SERVICE_HOME}/conf/cert.crt"
              SSLCertificateKeyFile="${SERVICE_HOME}/conf/key.pem"
              SSLCACertificateFile="${SERVICE_HOME}/conf/ca.crt"
              SSLCertificateChainFile="${SERVICE_HOME}/conf/authorities.crt"/>
```

where SSLCertificateKeyFile is your server private key file, SSLCertificateFile is your  server public cert file, and SSLCACertificateFile is your root CA, not the issuing CA. The SSLCertificateChainFile, i.e., authorities.crt, is a file containing the concatenation of PEM encoded CA certificates which form the certificate chain for the server certificate.

### Trust Store ###

For the trust store, you need to pass in the following environment variables either from command line,

```
-Djavax.net.ssl.trustStorePassword=${KEYSTORE_PASSWORD} \
-Djavax.net.ssl.trustStoreType=PKCS12 \
-Djavax.net.ssl.trustStore=${SERVICE_HOME}/conf/cert.p12 \
```

or Java code,

```
System.setProperty("javax.net.ssl.trustStore", this.trustStore);
System.setProperty("javax.net.ssl.trustStoreType", this.trustStoreType);
System.setProperty("javax.net.ssl.trustStorePassword", this.trustStorePassword);
```

## Test SSL ##

### Test with openssl ###

After the tomcat server is up and running, you can use openssl to test it.

```
openssl s_client -connect hostname:port -debug
```

To see the server cert and cert chain, use the following command,

```
openssl s_client -connect hostname:port -showcerts
```

### Test with curl ###

curl is a Linux tool and you can use it to send Http requests to a server including Https requests. For example, I have a Sign-On Service, which uses Https. I first created an XML file, loginrequest.xml, and then post it to the server as follows,

```
[jfang@localhost ~]$ curl -k --request POST --header "Content-type: text/xml" --data @loginrequest.xml https://localhost:8843/server/SignOnService
```


### Host Name Verification Problem ###

If you use a self-signed cert, which does bind to a host, some client may be picky about it because the client tries to validate the host name, but only sees the email address.

One way to get around this is to ask Https to not validate the host name. You can achieve this using the following code,

```
class NullHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
```

and ask HttpsURLConnection to not check host name,

```
HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
```

The Java doc for HttpsURLConnection could be found [here](http://java.sun.com/j2se/1.5.0/docs/api/javax/net/ssl/HttpsURLConnection.html)

# Resources #

  * [Tomcat SSL Configuration HOW-TO](http://tomcat.apache.org/tomcat-5.5-doc/ssl-howto.html)
  * [Apache Portable Runtime and Tomcat](http://tomcat.apache.org/tomcat-5.5-doc/apr.html)
  * [OpenSSL](http://www.openssl.org/)
  * [A few frequently used SSL commands](http://shib.kuleuven.be/docs/ssl_commands.shtml)
  * [Tellurium Automated Testing Framework](http://code.google.com/p/aost/)