# Contributing to Hexicript

Thank you for your interest in contributing to Hexicript! We appreciate your time and effort in making this project better.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Pull Requests](#pull-requests)
- [Development Setup](#development-setup)
- [Coding Guidelines](#coding-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)

## Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the [existing issues](https://github.com/yourusername/hexicript/issues) to see if the problem has already been reported.

### Suggesting Enhancements

Enhancement suggestions are tracked as [GitHub issues](https://github.com/yourusername/hexicript/issues).

### Pull Requests

1. Fork the repository
2. Create a new branch for your feature/fix: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a pull request

## Development Setup

1. Fork and clone the repository
2. Install JDK 17 or later
3. Install Maven
4. Run `mvn clean install` to build the project
5. Import the project into your favorite IDE

## Coding Guidelines

- Follow the existing code style
- Write clear, concise, and well-documented code
- Add unit tests for new features
- Ensure all tests pass before submitting a PR
- Update documentation when adding new features

## Commit Message Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification. Example commit message:

```
feat: add new command system

Add a flexible command system that supports subcommands and tab completion.

Fixes #123
```

### Commit Types

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that do not affect the meaning of the code
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `perf`: A code change that improves performance
- `test`: Adding missing tests or correcting existing tests
- `chore`: Changes to the build process or auxiliary tools
