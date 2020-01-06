#!/usr/bin/env bash

function _docker_exec() {

    containerName="cherry${JOB_NAME}${BUILD_NUMBER}"

    # remove any existing cherry container
    docker rm --force "${containerName}" >/dev/null

    docker pull nortecview/cherry_picker:latest

    # run cherry container in background
    docker run --name "${containerName}" --detach --tty nortecview/cherry_picker:latest >/dev/null

    echo "running on docker: '$1' arguments: '${@:2}'"
    docker exec --interactive "${containerName}" /bin/bash -c "$1 $(echo "${@:2}" | sed 's/ /\\ /g')"

    docker rm --force "${containerName}" >/dev/null
}
