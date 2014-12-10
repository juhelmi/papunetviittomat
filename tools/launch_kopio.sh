#!/bin/bash
#find . -name "*png"|grep res|xargs -n 1 tools/launch_kopio.sh ic_launcher-web.png

if [ $# -ne 2 ] ; then
    echo Virheellinen parametrien määrä
    echo Käyttö: $0 lähde_tiedosto kohde_tiedosto
    echo Komentorivi on: find . -name "*png" __putki__ grep res __putki__ xargs -n 1 tools/launch_kopio.sh ic_launcher-web.png
    exit
fi
lahde=$1
kohde=$2

vastaus=$(identify $kohde)
set $vastaus
#poimitaan identify komennon kolmas sana, jossa koko
koko=$3

convert $lahde -resize $koko $kohde