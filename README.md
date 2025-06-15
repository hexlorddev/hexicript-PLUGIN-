# Hexicript

[![Build Status](https://github.com/yourusername/hexicript/actions/workflows/build.yml/badge.svg)](https://github.com/yourusername/hexicript/actions)
[![GitHub license](https://img.shields.io/github/license/yourusername/hexicript)](https://github.com/yourusername/hexicript/blob/main/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/yourusername/hexicript)](https://github.com/yourusername/hexicript/releases)

Hexicript is a powerful scripting language for Minecraft servers, allowing server administrators and developers to extend server functionality with custom scripts.

## Features

- 🚀 High-performance script execution
- 🔄 Hot-reloading of scripts
- 📝 Easy-to-learn syntax
- 🔌 Extensible API for developers
- 🛡️ Sandboxed execution environment
- 🔄 Asynchronous script execution
- 📊 Built-in metrics and monitoring

## Installation

1. Download the latest release from the [releases page](https://github.com/yourusername/hexicript/releases)
2. Place the JAR file in your server's `plugins` directory
3. Restart your server
4. Start creating scripts in the `plugins/Hexicript/scripts` directory

## Quick Start

1. Create a new file in `plugins/Hexicript/scripts/hello.hxs`
   ```javascript
   // Simple Hello World script
   function onEnable() {
       console.log("Hello, Hexicript!");
   }
   ```
2. Run `/hexicript reload` in-game or from console
3. See the message in your server console!

## Documentation

For detailed documentation, please visit our [Wiki](https://github.com/yourusername/hexicript/wiki).

## Building from Source

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/hexicript.git
   cd hexicript
   ```
2. Build with Maven
   ```bash
   mvn clean package
   ```
3. Find the compiled JAR in `target/Hexicript-{version}.jar`

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before making a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please [open an issue](https://github.com/yourusername/hexicript/issues) or join our [Discord server](https://discord.gg/yourinvite).
