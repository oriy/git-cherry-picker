#!/usr/bin/env bash

function _docker_exec() {
    # remove any existing cherry container
    docker rm --force cherry >/dev/null

    # run cherry container in background
    docker run --name cherry --detach --tty nortecview/cherry_picker:latest >/dev/null

    echo "running on docker: '$1' arguments: '${@:2}'"
    docker exec --interactive cherry /bin/bash -c "$1 $(echo "${@:2}" | sed 's/ /\\ /g')"
}
