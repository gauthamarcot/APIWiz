# APIWiz

A PyCharm plugin for automatically detecting and testing Flask APIs.

## Features
- Automatic Flask application detection
- API route discovery
- HTTP request testing

## Installation
1. Open PyCharm
2. Go to Settings/Preferences → Plugins
3. Click "Browse repositories..."
4. Search for "APIWiz"
5. Click Install

## Usage
1. Start your Flask application with `flask run --debug`
2. The APIWiz window will automatically detect your routes
3. Use the interface to test your API endpoints

## Building from Source
```bash
./gradlew clean buildPlugin
```

## License
MIT License - see LICENSE file
