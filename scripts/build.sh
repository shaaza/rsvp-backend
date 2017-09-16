#!/bin/bash
lein clean; lein compile; lein uberjar
rm -rf micro_rsvp
mkdir micro_rsvp
cp target/uberjar/rsvp-backend-0.1.0-SNAPSHOT-standalone.jar ./micro_rsvp/micro_rsvp.jar
cp -r resources micro_rsvp/
tar -czf micro_rsvp.tar.gz ./micro_rsvp
