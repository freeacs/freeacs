#!/bin/bash
whattodownload="-bin|tables|shell"
curl -s https://api.github.com/repos/freeacs/freeacs/releases/latest | jq -r ".assets[] | select(.name | test(\"${whattodownload}\")) | .browser_download_url" > files.txt
awk '{print $0;}' files.txt | xargs -l1 wget
unzip "*.zip"
rm -rf *.zip