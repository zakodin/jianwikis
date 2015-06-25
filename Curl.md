What if you do not have UI in the Linux system, but you want to test your REST or SOAP web services? You can use a Linux tool curl to achieve this.

## Request XML ##

First, you need to create the request XML file. For example, I have a sign on service to login, the SOAP message looks as follows,

```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sig="http://mycompany.ws/services/SignOnService" xmlns:ent="http://mycompany.ws/entities">
   <soapenv:Header/>
   <soapenv:Body>
      <sig:login>
         <ent:LoginRequest>
            <ent:context>
               <ent:locale>us_EN</ent:locale>
               <ent:taskId>default</ent:taskId>
            </ent:context>
            <ent:password>P@ssw0rd</ent:password>
            <ent:userId>jfang</ent:userId>
         </ent:LoginRequest>
      </sig:login>
   </soapenv:Body>
</soapenv:Envelope>
```

## Post Request Using Curl ##

The command is

```
[jfang@localhost ~]$ curl -k --request POST --header "Content-type: text/xml" --data @loginrequest.xml https://localhost:8843/server/SignOnService
```

and the response message will be dumped out as follows,

```
<?xml version="1.0" ?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
    <S:Body>
        <ns2:loginResponse xmlns:ns2="http://mycompany.ws/services/SignOnService"
                           xmlns:ns3="http://mycompany.ws/entities">
            <ns3:EmployeeAuthenticationResponse>
                <ns3:responseType>NORMAL</ns3:responseType>
                <ns3:isSuccessful>true</ns3:isSuccessful>
                <ns3:returnValue>
                    <ns3:isAuthenticated>true</ns3:isAuthenticated>
                    <ns3:employee>
                        <ns3:person>
                            <ns3:firstName>Jian</ns3:firstName>
                            <ns3:lastName>Fang</ns3:lastName>
                        </ns3:person>
                        <ns3:userId>jfang</ns3:userId>
                    </ns3:employee>
                    <ns3:roles>
                        <ns3:description>QCSupervisor</ns3:description>
                        <ns3:name>QCSupervisor</ns3:name>
                        <ns3:roleId>170</ns3:roleId>
                    </ns3:roles>
                    <ns3:ticketId>TGT-100023-kVMpsxODT5mQqbgbyEPoWduF5olvmT12-JTV</ns3:ticketId>
                </ns3:returnValue>
            </ns3:EmployeeAuthenticationResponse>
        </ns2:loginResponse>
    </S:Body>
</S:Envelope>
```