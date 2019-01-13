#!/bin/sh

set -e

javac MyBot.java
javac initialBot.java
./halite --replay-directory replays/ -vvv --width 32 --height 32 "java MyBot" "java initialBot"
