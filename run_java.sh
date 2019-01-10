#!/usr/bin/env bash

function _run_java() {
    jar="$(find -type f -wholename "*build/libs/$1")"

    cmd="java -jar -Xmx1024m -Xms1024m -XX:MaxNewSize=512m $jar ${@:2}"
    echo "> $cmd"
    ${cmd}
}
