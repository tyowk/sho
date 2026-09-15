# Sho

A simple Discord bot written in Java using JDA.

## Features

* Discord bot
* Custom prefixes
* SQLite database
* Economy system
* Command cooldowns
* YAML configuration

## Requirements

* Java 17+
* Maven

## Setup

Clone the repository:

```bash
git clone https://github.com/tyowk/sho.git
cd sho
```

Build:

```bash
mvn package
```

Create your configuration from `sho.example.yml`, then add your bot token.

Run:

```bash
java -jar target/Sho.jar
```

## Commands

```text
ping
prefix
balance
daily
```

## Tech Stack

* Java
* JDA
* SQLite
* Maven

## License

No license specified.
