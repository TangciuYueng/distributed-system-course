#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: $0 <countdown_seconds>"
    exit 1
fi

left_seconds=$1
mod=$((left_seconds%10))
# 使用了两个输出流 分别负责输入到文件和控制台
while [ $left_seconds -gt 0 ];
do
	if ((left_seconds%10 == mod)); then
		echo $left_seconds
		echo $left_seconds >&2
	fi
    let left_seconds-=1
    sleep 1
done

if ((mod == 0)); then
	echo 0
	echo 0 >&2
fi
