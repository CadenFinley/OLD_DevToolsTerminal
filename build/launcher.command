#!/usr/bin/env bash

cd -- "$(dirname -- "$0")"

# Function to check if the Engine class is running
is_engine_running() {
    jps | grep -q "Engine"
}

# Open a new terminal window and run the Java command
if [[ "$OSTYPE" == "darwin"* ]]; then
    osascript -e 'tell application "Terminal" to do script "cd '$(pwd)' && java -jar DevToolsTerminal.jar; while true; do sleep 5; if ! jps | grep -q \"Engine\"; then exit; fi; done"'
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    gnome-terminal -- bash -c "cd '$(pwd)' && java -jar DevToolsTerminal.jar; while true; do sleep 5; if ! jps | grep -q \"Engine\"; then exit; fi; done; exec bash"
else
    echo "Unsupported OS"
fi
