
#!/usr/bin/env bash

directories="bundles certs csr db newcerts private"

rm -rf $directories
mkdir $directories

chmod 700 private
touch db/index
openssl rand -hex 16  > db/serial
echo 1001 > db/crlnumber
