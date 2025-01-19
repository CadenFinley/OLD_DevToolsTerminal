# DevToolsTerminal

## Overview

DevToolsTerminal is a open-source, command-line tool designed to interact with OpenAI's API, manage terminal commands, and handle user settings. It provides a robust interface for executing commands, managing chat history, and configuring various settings. It also provides built-in functionality for startup commands, command shortcuts, and script execution.

## Features

- **OpenAI Integration**: Connects to OpenAI's API to send and receive chat messages.
- **Terminal Commands**: Allows execution of terminal commands and manages terminal cache.
- **User Settings Management**: Provides commands to manage user settings, including startup commands, chat settings, text settings, and shortcuts.
- **Chat History**: Saves and manages chat history with options to enable/disable incognito mode.
- **Startup Commands**: Allows adding, removing, and running startup commands.
- **Shortcuts**: Enables the creation and management of command shortcuts.
- **Testing Mode**: Provides a testing mode for debugging and development purposes.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) installed
- OpenAI API key

## Usage

### Main Commands
It natively sends commands with no prefix or other predetermmined function to either the OpenAI chat bot function or to the device terminal depening on the current set menu.
- **restart**: Restarts the application.
- **clear**: Clears the terminal screen and cache.
- **exit**: Exits the application.
- **aihelp**: Provides help using OpenAI's API.
- **ss [shortcut]**: Executes a shortcut command.
- **.[command]**: Executes a DevToolsTerminal command.

### AI Settings Commands

- **.ai log**: Saves the last chat to a file.
- **.ai apikey set [key]**: Sets the OpenAI API key.
- **.ai apikey get**: Retrieves the current OpenAI API key.
- **.ai chat [message]**: Sends a chat message to OpenAI.
- **.ai get [data]**: Retrieves specific data from OpenAI.
- **.ai dump**: Dumps all response data from OpenAI.
- **.ai help**: Lists available AI commands.

### User Settings Commands

- **.user startup [subcommand]**: Manages startup commands.
- **.user chat [subcommand]**: Manages chat settings.
- **.user text [subcommand]**: Manages text settings.
- **.user shortcut [subcommand]**: Manages shortcuts.
- **.user testing [enable/disable]**: Toggles testing mode.
- **.user data [get/clear]**: Manages user data file.
- **.user help**: Lists available user settings commands.

### Startup Commands

- **.user startup add [command]**: Adds a startup command.
- **.user startup remove [command]**: Removes a startup command.
- **.user startup clear**: Clears all startup commands.
- **.user startup enable/disable**: Enables or disables startup commands.
- **.user startup list**: Lists all startup commands.
- **.user startup runall**: Runs all startup commands.

### Chat Settings Commands

- **.user chat history enable/disable**: Enables or disables chat history.
- **.user chat history save**: Saves the chat history.
- **.user chat history clear**: Clears the chat history.
- **.user chat cache enable/disable**: Enables or disables chat cache.
- **.user chat cache clear**: Clears the chat cache.

### Text Settings Commands

- **.user text textspeed [speed]**: Sets the text speed.
- **.user text textbuffer enable/disable**: Enables or disables text buffer.
- **.user text defaultentry ai/terminal**: Sets the default text entry mode.
- **.user text displayfullfilepath enable/disable**: Toggles displaying the full file path.

### Shortcut Commands

- **.user shortcut clear**: Clears all shortcuts.
- **.user shortcut enable/disable**: Enables or disables shortcuts.
- **.user shortcut add [shortcut] [command]**: Adds a shortcut.
- **.user shortcut remove [shortcut]**: Removes a shortcut.
- **.user shortcut list**: Lists all shortcuts.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request for any improvements or bug fixes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Author

Caden Finley
