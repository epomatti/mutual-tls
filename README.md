# Mutual TLS

In this a

The implementation is represented by the two entities CRM (client) and the Bank (server).


```
mvn package

mvn exec:exec "-Djavax.net.ssl.trustStore=$PWD/keystore.jks"
```

```
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

```
./mvnw spring-boot:run
```



```
::1       server.pomatti.local
```


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
127.0.0.1       server.pomatti.local
```

### Client

```
keytool -importcert -trustcacerts -file server-certificate.crt -keystore keystore.jks -alias "server"
```

For development purposes use a simple password such as `secret`.


## OpenSSL

```
verify error:num=18:self-signed certificate
Verification error: self-signed certificate
```



openssl s_client -showcerts -connect server.pomatti.local:8443
openssl s_client -connect server.pomatti.local:8443
openssl s_client -CApath ./certs/ -connect server.pomatti.local:8443

openssl verify -CAfile <ca_cert.pem> <target_cert.pem>

[1]: https://docs.oracle.com/cd/E19509-01/820-3503/ggfhb/index.html
[2]: https://www.phcomp.co.uk/Tutorials/Web-Technologies/Understanding-and-generating-OpenSSL.cnf-files.html
[3]: https://www.mojohaus.org/exec-maven-plugin/usage.html


https://stackoverflow.com/questions/5871279/ssl-and-cert-keystore
https://www.feistyduck.com/library/openssl-cookbook/online/openssl-command-line/private-ca-creating-root.html