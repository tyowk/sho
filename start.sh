#!/bin/bash

set -e

log() {
	echo "[INFO] [$(date '+%H:%M:%S')] :: $1"
}

line() {
	echo " "
	echo "+-------------------------------------------------+"
	echo " "
}

clear
log "Installing dependencies..."
sleep 1
npm install aoi.js@dev @biomejs/biome@latest @typescript/native-preview@latest aoijs.mysql@latest
sleep 2
clear

log "Cleaning up old build..."
sleep 1
rm -rf dist
sleep 2

line
log "Starting build process..."
sleep 1
echo " "

log "Formatting source code..."
sleep 1
biome format --write --config-path biome.json

line
log "Linting and fixing issues..."
sleep 1
biome lint --fix --config-path biome.json
sleep 2
clear

log "Building project..."
sleep 1
npx tsgo -p tsconfig.json

line
log "Launching project..."
sleep 2
clear

node dist/index.js