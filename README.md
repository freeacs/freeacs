![Freeacs Logo](https://github.com/freeacs/readme/blob/master/logo.png)

[![Build Status](https://travis-ci.org/freeacs/freeacs.svg?branch=master)](https://travis-ci.org/freeacs/freeacs)
[![Build Status](https://travis-ci.org/freeacs/mysql.svg?branch=master)](https://travis-ci.org/freeacs/mysql)
[![Build Status](https://travis-ci.org/freeacs/nginx.svg?branch=master)](https://travis-ci.org/freeacs/nginx)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bbbaea0fbfd84abb9013ece867747e30)](https://www.codacy.com/app/Freeacs/freeacs?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=freeacs/freeacs&amp;utm_campaign=Badge_Grade)
[![Gitter chat](https://badges.gitter.im/FreeACS-on-Gitter/Freeacs.png)](https://gitter.im/FreeACS-on-Gitter/Freeacs)
[![Donate](https://img.shields.io/badge/Patreon-Donate-blue.svg)](https://www.patreon.com/freeacs)

Fusion Free ACS is the most complete TR-069 ACS available for free under the MIT License. You can download and install it, or contribute to the project! 

## Prerequisites

Freeacs requires Java and MySQL. It has been tested to work on Java 8 and latest version of MySQL (the latter with some minor quirk in the install script).

## Social

* [Freeforums](https://www.tapatalk.com/groups/freeacs/)
* [Gitter](https://gitter.im/FreeACS-on-Gitter/Freeacs)


## Build it

Freeacs is built with SBT:

```bash
$ sbt clean compile test
```

## Play with it
Start tr069 server in discovery mode with:

```
discovery.mode=true
```

Fire up a tr069 test client in docker:

```bash
docker run --rm -it --net=host --entrypoint /bin/bash --name="easycwmp" xateam/easycwmp_docker
apt install nano
nano /etc/config/easycwmp
change:
    option url http://5.5.5.12/acs
to
    option url http://[your-lan-ip-on-host-machine]:[tr069-port]/tr069
Ctrl-X+y+Enter
chmod +x startup.sh
./startup.sh
```

## Contributing

Open a pull request, add an issue or discuss in the forums. 

## Versioning

We use SemVer for versioning.

## License

This project is licensed under the The MIT License.

## Active project Members

* **Jarl André Hübenthal (@jarlah)**

See https://github.com/freeacs/freeacs/wiki/About for information.
