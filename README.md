# DevToolsTerminal

DevToolsTerminal is an open-source universal terminal emulator designed for developers. It provides native support for OpenAI AI integration, powerful startup commands, and self-assignable command shortcuts.

Development for this project has largely stopped and has been replaced with DevToolsTerminal-LITE: https://github.com/CadenFinley/DevToolsTerminal-LITE

## Features

- **TimeEngine**: A timer and stopwatch utility.
- **TextEngine**: Methods for printing text with delays, clearing the console, and handling user input.
- **TerminalPassthrough**: Interact with the terminal, execute commands, and manage terminal cache.
- **OpenAIPromptEngine**: Generate prompts using OpenAI API and manage chat history.
- **Engine**: Main application logic, including user settings, command parsing, and startup commands.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- OpenAI API key (optional for AI features)

### Downloading the Project

You can download the project by cloning the repository using Git:

```sh
git clone https://github.com/yourusername/DevTools_CadenFinley.git
```

Alternatively, you can download the ZIP file from the GitHub repository and extract it to your desired location.

### Building the Project

To build the project, navigate to the project directory and use the following command:

```sh
mvn compile
```

### Running the Project

After building the project, you can run it using the following command:

```sh
mvn exec:java
```

## Usage

### Commands

- **General Commands**:
  - `exit`: Exit the application.
  - `clear`: Clear the console screen.
  - `help`: Display available commands.

- **AI Commands**:
  - `ai log`: Log the last chat conversation.
  - `ai log extract o[ARGS]`: Log and extract generated code from openai with optional file name
  - `ai apikey set [API_KEY]`: Set the OpenAI API key.
  - `ai apikey get`: Get the current OpenAI API key.
  - `ai chat [MESSAGE]`: Send a message to OpenAI and get a response.
  - `ai get [KEY]`: Get specific response data.
  - `ai dump`: Dump all response data.

- **User Commands**:
  - `user startup add [COMMAND]`: Add a startup command.
  - `user startup remove [COMMAND]`: Remove a startup command.
  - `user startup clear`: Clear all startup commands.
  - `user startup enable`: Enable startup commands.
  - `user startup disable`: Disable startup commands.
  - `user startup list`: List all startup commands.
  - `user startup runall`: Run all startup commands.
  - `user chat history enable`: Enable chat history.
  - `user chat history disable`: Disable chat history.
  - `user chat history save`: Save chat history.
  - `user chat history clear`: Clear chat history.
  - `user chat cache enable`: Enable chat cache.
  - `user chat cache disable`: Disable chat cache.
  - `user chat cache clear`: Clear chat cache.
  - `user text textbuffer enable`: Enable text buffer.
  - `user text textbuffer disable`: Disable text buffer.
  - `user text defaultentry ai`: Set default text entry to AI.
  - `user text defaultentry terminal`: Set default text entry to terminal.
  - `user text commandprefix [PREFIX]`: Set default text entry to terminal.
  - `user shortcut clear`: Clear all shortcuts.
  - `user shortcut enable`: Enable shortcuts.
  - `user shortcut disable`: Disable shortcuts.
  - `user shortcut add [SHORTCUT] [COMMAND]`: Add a shortcut.
  - `user shortcut remove [SHORTCUT]`: Remove a shortcut.
  - `user shortcut list`: List all shortcuts.
  - `user testing enable`: Enable testing mode.
  - `user testing disable`: Disable testing mode.
  - `user data get userdata`: Get user data.
  - `user data get userhistory`: Get user command history.
  - `user data get all`: Get all user data and history.
  - `user data clear`: Clear user data and history.

## Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Author

Caden Finley
