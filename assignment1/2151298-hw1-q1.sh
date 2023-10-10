#!/bin/bash

is_prime() {
    param=$1
    if (($param < 2)); then
        return 1
    fi

    for ((i = 2; i * i <= param; i++));
    do
        if ((param % i == 0)); then
            return 1
        fi
    done

    return 0
}

ans=0

for ((num=1; num<=100; num++));
do
    if is_prime $num; then
        ans=$((ans + num))
    fi

done

echo $ans > 2151298-hw1-q1.log
echo $ans
