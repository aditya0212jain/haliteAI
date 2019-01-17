#!/bin/sh

set -e

javac MyBot.java
javac best.java
./halite --replay-directory replays/ -vvv --width 48 --height 48 "java MyBot" "java best"
