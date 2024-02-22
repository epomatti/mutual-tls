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

```
cat mykey.pem.txt mycertificate.pem.txt>mykeycertificate.pem.txt

openssl pkcs12 -export -in mykeycertificate.pem.txt -out mykeystore.pkcs12 -name myAlias -noiter -nomaciter
```


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

[1]: https://docs.oracle.com/cd/E19509-01/820-3503/ggfhb/index.html
[2]: https://www.phcomp.co.uk/Tutorials/Web-Technologies/Understanding-and-generating-OpenSSL.cnf-files.html
