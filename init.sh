#!/bin/bash

sleep 5
mongo --host mongo_1:27017 -u admin -p pass < /repl_init.js
