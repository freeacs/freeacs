![Freeacs Logo](https://github.com/freeacs/readme/blob/master/logo.png)

[![Build Status](https://travis-ci.org/freeacs/freeacs.svg?branch=master)](https://travis-ci.org/freeacs/freeacs)
[![Maintainability](https://api.codeclimate.com/v1/badges/4b32968512c546806799/maintainability)](https://codeclimate.com/github/freeacs/freeacs/maintainability)
[![Gitter chat](https://badges.gitter.im/FreeACS-on-Gitter/Freeacs.png)](https://gitter.im/FreeACS-on-Gitter/Freeacs)
[![Donate](https://img.shields.io/badge/Patreon-Donate-blue.svg)](https://www.patreon.com/freeacs)

FreeACS is the most complete TR-069 ACS available for free under the MIT License. You can download and install it, or contribute to the project! 

## Prerequisites

FreeACS requires Java and MySQL. It has been tested to work on Java 8 and latest version of MySQL (the latter with some minor quirk in the install script).

## Social

* [Freeforums] (deprecated)



## Build it

FreeACS is built with SBT on unix/linux systems:

```bash
$ ./mvnw test
```

Packaged to deployable zips with:

```bash
$ ./mvnw package
```

See the distribution modules target folder or the individual module target folders for deployable zip files.

## Contributing

Please read [CONTRIBUTING.md](https://github.com/freeacs/freeacs/blob/master/CONTRIBUTING.md) for instructions to set up your development environment to build FreeACS

## Versioning

We use SemVer for versioning.

## License

This project is licensed under the The MIT License.

## Active project Members

* **Jarl André Hübenthal (@jarlah)**

See https://github.com/freeacs/freeacs/wiki/About for information.
