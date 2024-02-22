# mutual-tls



```
mvn package
mvn exec:java
```

```
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

```
./mvnw spring-boot:run
```

Generate a keystore, this case with a [P12 format][1]:


### PKI

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

Create the key store

> The `noiter` and `nomaciter` options must be specified to allow the generated KeyStore to be recognized properly by JSSE.

```sh
openssl pkcs12 -inkey server-key.pem -in server-certificate.crt -export -out server-keystore.p12 \
       -noiter -nomaciter
```

Copy to the resourc

For this project local development purposes use the password `demo`.

```sh
keytool -list -storetype pkcs12 -keystore server-keystore.p12 -storepass demo
```

Add to the `/etc/hosts` file:

```
127.0.0.1       server.pomatti.local
```

[1]: https://docs.oracle.com/cd/E19509-01/820-3503/ggfhb/index.html
[2]: https://www.phcomp.co.uk/Tutorials/Web-Technologies/Understanding-and-generating-OpenSSL.cnf-files.html
