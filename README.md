# Mutual TLS

## Execute

Once the PKI setup is complete, these are the steps to run the modules.

The implementation is represented by the two entities Enterprise (client) and the Bank (server).

Add the DNS configuration to the `/etc/hosts/` files.

> ℹ️ If using WSL, edit the Windows hosts file as well as it replaces the WSL file on reboot

```
localhost    api.bank.local
::1          api.bank.local
```

Initiate the server:

```sh
./mvnw spring-boot:run
```

Initiate the client application:

```sh
mvn package
mvn exec:exec
```

### Updates

To check for dependencies and plugins updates:

```sh
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

The following sections will demonstrate how to set up the infrastructure prior to running the applications.

## 1 - Create the Server PKI

Change to the Bank PKI directory:

```sh
cd pki/bank
```

Initialize the directory structure:

```sh
bash init.sh
```

Create the Root CA:

> Use password `1234`

```sh
openssl req -new \
    -config root.conf \
    -out csr/bank-root.csr \
    -keyout private/bank-root.key
```

Self-sign the Root CA certificate:

> Use the previous password, and accept the prompts

```sh
openssl ca -selfsign \
    -config root.conf \
    -in csr/bank-root.csr \
    -out certs/bank-root.crt
```

Create and sign the server certificate:

```sh
# Private key
openssl genrsa -out ./private/bank-server.key 4096

# CSR
openssl req -config ./server.conf -key ./private/bank-server.key -subj '/CN=api.bank.local' -new -sha256 -out ./csr/bank-server.csr

# Sign
openssl ca -batch -config ./root.conf -passin pass:1234 -extfile server.conf -extensions v3_req -days 30 -notext -md sha256 -in ./csr/bank-server.csr -out ./certs/bank-server.crt
```

Create the bundle to will be used by the Spring Boot server application:

> The `noiter` and `nomaciter` options must be specified to allow the generated KeyStore to be recognized properly by JSSE.
> Use password `1234`

```sh
openssl pkcs12 -inkey ./private/bank-server.key -in ./certs/bank-root.crt -in ./certs/bank-server.crt -export -out ./bundles/bank-server-keystore.p12 \
       -noiter -nomaciter
```

Copy the #PKCS12 bundle to server application directory:

```sh
cp bundles/bank-server-keystore.p12 ../../server/
```

Copy the Root CA to the client directory:

```sh
cp certs/bank-root.crt ../../client/
```

## 2 - Create the Client PKI

Change to the Enterprise PKI directory:

```sh
cd pki/enterprise
```

Initialize the directory structure:

```sh
bash init.sh
```

Create the Root CA:

> Use password `1234`

```sh
openssl req -new \
    -config root.conf \
    -out csr/enterprise-root.csr \
    -keyout private/enterprise-root.key
```

Self-sign the Root CA certificate:

> Use the previous password, and accept the prompts

```sh
openssl ca -selfsign \
    -config root.conf \
    -in csr/enterprise-root.csr \
    -out certs/enterprise-root.crt
```

Create and sign the client certificate:

```sh
# Private key
openssl genrsa -out ./private/enterprise-client.key 4096

# CSR
openssl req -config ./client.conf -key ./private/enterprise-client.key -subj '/CN=client.enterprise.local' -new -sha256 -out ./csr/enterprise-client.csr

# Sign
openssl ca -batch -config ./root.conf -passin pass:1234 -extfile client.conf -extensions v3_req -days 30 -notext -md sha256 -in ./csr/enterprise-client.csr -out ./certs/enterprise-client.crt
```

Copy the root certificate to the server:



Copy the the client key and certificate to the client:

```sh
cp certs/enterprise-client.crt ../../client/
cp private/enterprise-client.key ../../client/
```

Create and copy the truststore to the server directory:

```sh
keytool -import -trustcacerts -file certs/enterprise-root.crt -alias EnterpriseRootCA -keystore bundles/truststore.jks -storepass 123456

cp  bundles/truststore.jks ../../server/
```


## 3  Build the Server Trust Store

Enter the server directory:

```sh
cd server
```


## 4 - Build the Client truststore



```sh
keytool -importcert -trustcacerts -file bank-root.crt -storepass secret -keystore keystore.jks -alias "root.bank.local"
```


## Troubleshooting

Verifying the contents of requests and certificates:

```sh
openssl req -text -noout -verify -in ./csr/bank-server.csr | grep 'DNS'
openssl req -text -noout -verify -in ./csr/bank-server.csr
openssl x509 -text -noout -in ./certs/bank-server.crt
```

Verify TLS trust with the root CA:

```sh
# ❌ This should fail
openssl s_client -showcerts -connect api.bank.local:8443

# ✅ This should work
openssl s_client -showcerts -CAfile certs/bank-root.crt -connect api.bank.local:8443
```

Verify with

```sh
# openssl s_client -cert ./client-cert.pem -key ./client-key.key -CApath /etc/ssl/certs/ -connect foo.example.com:443
openssl s_client -cert ./enterprise-client.crt -key ./enterprise-client.key -CAfile bank-root.crt -connect api.bank.local:8443

curl --cert enterprise-client.crt --key enterprise-client.key --cacert bank-root.crt https://api.bank.local:8443
```

### Wireshark TLS

Create an environment variable:

- Name: `SSLKEYLOGFILE`
- Value: `C:\Users\<USER>\SSLKeys\sslkeylog.log`

Start a Wireshark session. In Chrome, navigate the desired site.

In Wireshark > Preferences > Protocols > TLS, add set the (Pre)-Master-Secret log filename.

Filter the traffic:

```
frame contains "api.bank.local"
```







############



Generate a keystore, this case with a [P12 format][1]:

## Maven

Useful note from the [exec-maven-plugin][3] documentation:

> **Note:** The `java` goal doesn't spawn a new process. Any VM specific option that you want to pass to the executed class must be passed to the Maven VM using the `MAVEN_OPTS` environment variable. E.g.
> ```
> MAVEN_OPTS=-Xmx1024m
> ```
> Otherwise consider using the `exec` goal.


## Setup

## Root

### Server

```sh
bash init.sh
```

```sh
openssl req -new \
    -config root-ca.conf \
    -out csr/bank-root-ca.csr \
    -keyout private/bank-root-ca.key
```

```sh
openssl ca -selfsign \
    -config root-ca.conf \
    -in csr/bank-root-ca.csr \
    -out certs/bank-root-ca.crt
```

```sh
# Private key
openssl genrsa -out ./private/bank-server-key.pem 4096

# CSR
openssl req -config ./server.conf -key ./private/bank-server-key.pem -subj '/CN=api.bank.local' -new -sha256 -out ./csr/bank-server-csr.pem

# Sign
openssl ca -batch -config ./root-ca.conf -passin pass:1234 -extfile server.conf -extensions v3_req -days 30 -notext -md sha256 -in ./csr/bank-server-csr.pem -out ./certs/bank-server-cert.pem
```

Testing:

```sh
openssl req -text -noout -verify -in ./csr/bank-server-csr.pem | grep 'DNS'
openssl req -text -noout -verify -in ./csr/bank-server-csr.pem
openssl x509 -noout -text -in ./certs/bank-server-cert.pem
```

```sh
openssl pkcs12 -inkey ./private/bank-server-key.pem -in ./certs/bank-root-ca.pem -in ./certs/bank-server-cert.pem -export -out ./bundles/bank-server-keystore.p12 \
       -noiter -nomaciter
```






https://badssl.com/

### Server

Create the private key:

```sh
openssl genrsa -out server-key.pem 2048
```

Create the server certificate:

```sh
openssl req \
       -config openssl-server.cnf \
       -key server-key.pem \
       -new \
       -x509 -days 365 -out server-certificate.crt
```

Create the Java key store in PKCS12 format:

> The `noiter` and `nomaciter` options must be specified to allow the generated KeyStore to be recognized properly by JSSE.

```sh
openssl pkcs12 -inkey server-key.pem -in server-certificate.crt -export -out server-keystore.p12 \
       -noiter -nomaciter
```

For this project local development purposes use the password `demo`.

The `resources` plugin in the [server/pom.xml](./server/pom.xml) is already configured to load the respective bundles for Spring Boot.

The key alias within the keystore will be `1`.

In case troubleshooting is required, here is a sample command:

```sh
keytool -list -storetype pkcs12 -keystore server-keystore.p12 -storepass demo
```

After that, copy the server certificate to the client:

```sh
cp server-certificate.crt ../client/
```

### Network

Add the host mapping to the `/etc/hosts` file:

```
127.0.0.1       api.bank.local
```

### Client

```sh
keytool -importcert -trustcacerts -file bank-root-ca.crt -storepass secret -keystore keystore.jks -alias "root.bank.local"
keytool -importcert -trustcacerts -file bank-server-cert.pem -storepass secret -keystore keystore.jks -alias "api.bank.local"
```

mvn exec:exec "-Djavax.net.ssl.trustStore=$PWD/keystore.jks"

For development purposes use a simple password such as `secret`.


## OpenSSL

```
verify error:num=18:self-signed certificate
Verification error: self-signed certificate
```



openssl s_client -showcerts -connect api.bank.local:8443
openssl s_client -connect api.bank.local:8443
openssl s_client -CApath ./certs/ -connect api.bank.local:8443


# This is ok
openssl verify -verbose -CAfile bank-root-ca.crt bank-server-cert.pem
openssl s_client -showcerts -CAfile bank-root-ca.crt -connect api.bank.local:8443


⚠️
openssl s_client -cert ./client-cert.pem -key ./client-key.key -CApath /etc/ssl/certs/ -connect foo.example.com:443

keytool -keystore ./keystore.jks -storepass secret -list | grep root.bank.local


openssl verify -CAfile <ca_cert.pem> <target_cert.pem>

[1]: https://docs.oracle.com/cd/E19509-01/820-3503/ggfhb/index.html
[2]: https://www.phcomp.co.uk/Tutorials/Web-Technologies/Understanding-and-generating-OpenSSL.cnf-files.html
[3]: https://www.mojohaus.org/exec-maven-plugin/usage.html


https://www.ibm.com/docs/en/hpvs/1.2.x?topic=reference-openssl-configuration-examples
https://stackoverflow.com/questions/5871279/ssl-and-cert-keystore
https://www.feistyduck.com/library/openssl-cookbook/online/openssl-command-line/private-ca-creating-root.html
https://www.feistyduck.com/library/openssl-cookbook/online/openssl-command-line/private-ca-creating-root.html
https://github.com/epomatti/az-iot-dps
https://www.ibm.com/support/pages/how-create-csr-multiple-subject-alternative-name-san-entries-pase-openssl-3rd-party-or-internet-ca
https://www.ibm.com/support/pages/how-create-csr-multiple-subject-alternative-name-san-entries-pase-openssl-3rd-party-or-internet-ca
https://stackoverflow.com/questions/11548336/openssl-verify-return-code-20-unable-to-get-local-issuer-certificate
https://stackoverflow.com/questions/45522363/difference-between-java-keytool-commands-when-importing-certificates-or-chain
https://stackoverflow.com/questions/45522363/difference-between-java-keytool-commands-when-importing-certificates-or-chain

https://www.baeldung.com/x-509-authentication-in-spring-security
https://www.baeldung.com/x-509-authentication-in-spring-security
https://medium.com/geekculture/authentication-using-certificates-7e2cfaacd18b
https://medium.com/@salarai.de/how-to-enable-mutual-tls-in-a-sprint-boot-application-77144047940f