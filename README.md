# Mutual TLS

## Execute

👉 Once the PKI setup is complete, these are the steps to run the modules.

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

> The key alias within the keystore will be `1`.
> The `noiter` and `nomaciter` options must be specified to allow the generated KeyStore to be recognized properly by JSSE.
> Use password `1234`

```sh
openssl pkcs12 -inkey ./private/bank-server.key -in ./certs/bank-root.crt -in ./certs/bank-server.crt -export -out ./bundles/keystore.p12 \
       -noiter -nomaciter
```

Copy the #PKCS12 bundle to server application directory:

```sh
cp bundles/keystore.p12 ../../server/
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

Copy the certificates and client key to the client directory:

```sh
cp certs/enterprise-root.crt ../../client/
cp certs/enterprise-client.crt ../../client/
cp private/enterprise-client.key ../../client/
```

> TODO: Implement client truststore

```sh
keytool -importcert -trustcacerts -file bank-root.crt -storepass secret -keystore keystore.jks -alias "root.bank.local"
```

Create and copy the truststore to the server directory:

```sh
keytool -import -trustcacerts -file certs/enterprise-root.crt -alias EnterpriseRootCA -keystore bundles/truststore.jks -storepass 123456

cp  bundles/truststore.jks ../../server/
```

Quick client test:

```sh
# From the client directory
curl --cert enterprise-client.crt --key enterprise-client.key --cacert bank-root.crt https://api.bank.local:8443
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

Verify with client testing:

```sh
openssl s_client -cert ./enterprise-client.crt -key ./enterprise-client.key -CAfile bank-root.crt -connect api.bank.local:8443

curl --cert enterprise-client.crt --key enterprise-client.key --cacert bank-root.crt https://api.bank.local:8443
```

Quick SSL tests can be performed at https://badssl.com/.

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

## References

[1]: https://docs.oracle.com/cd/E19509-01/820-3503/ggfhb/index.html
[2]: https://www.phcomp.co.uk/Tutorials/Web-Technologies/Understanding-and-generating-OpenSSL.cnf-files.html
[3]: https://www.mojohaus.org/exec-maven-plugin/usage.html

```
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
```
