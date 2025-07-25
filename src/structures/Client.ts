import { Client as Dis, type ClientOptions, GatewayIntentBits as Intents, Partials } from 'discord.js';
import { red, yellow, green, blue } from 'chalk';
import KeyvSqlite from '@keyv/sqlite';
import Keyv from 'keyv';
import FS from 'node:fs';
import Path from 'node:path';

interface ClientOP extends Omit<ClientOptions, 'intents' | 'partials'> {
    commands?: string;
    token: string;
    intents: string[];
    partials?: string[];
    events?: string;
    production?: boolean;
    database?: {
        path: string;
        tables: string[];
    };
}

declare module 'discord.js' {
    interface Client {
        database: Map<string, Keyv<any>>;
        debug(message: string, type?: 'info' | 'warn' | 'error' | 'success'): void;
        commands: Map<string, any>;
        sho: Sho;
    }
}

export class Sho {
    public readonly options: ClientOP;
    public readonly client: Dis;
    public readonly commands: Map<string, any>;
    public readonly database: Map<string, Keyv<any>>;

    public constructor(_options: ClientOP) {
        const { token, commands, events, database, ...options } = this.#loadOptions(_options);
        this.client = new Dis(options);
        this.commands = new Map<string, any>();
        this.database = new Map<string, Keyv<any>>();
        this.options = {
            ...options,
            commands,
            token,
            database,
            events
        };

        this.#loadDatabase(database.path, database.tables);
        this._loadCommands(commands);
        this._loadEvents(events);
        this.client.commands = this.commands;
        this.client.database = this.database;
        this.client.debug = this.debug.bind(this);
        this.client.sho = this;
        this.client.login(token);
    }

    public static createClient(options: ClientOP): Sho {
        return new Sho(options);
    }

    public _loadCommands(path: string): void {
        if (!this.#isDirectoryExists(path)) return;
        const files = FS.readdirSync(Path.resolve(path));

        for (const file of files) {
            const filePath = Path.join(path, file);
            const stat = FS.statSync(filePath);
            if (stat.isDirectory()) {
                this._loadCommands(filePath);
            } else if (file.endsWith('.js') || file.endsWith('.ts') || file.endsWith('.cjs') || file.endsWith('.mjs')) {
                let command = require(filePath);
                command = command.default || command;
                if (command?.name && command.execute) {
                    this.commands.set(command.name, command);
                    this.debug(`Loaded command ${command.name}`, 'success');
                } else {
                    this.debug(`Failed to load command ${filePath}`, 'error');
                }
            }
        }
    }

    public _loadEvents(path: string): void {
        if (!this.#isDirectoryExists(path)) return;
        const files = FS.readdirSync(Path.resolve(path));

        for (const file of files) {
            const filePath = Path.join(path, file);
            const stat = FS.statSync(filePath);
            if (stat.isDirectory()) {
                this._loadEvents(filePath);
            } else if (file.endsWith('.js') || file.endsWith('.ts') || file.endsWith('.cjs') || file.endsWith('.mjs')) {
                let event = require(filePath);
                event = event.default || event;
                if (event?.name && event.execute) {
                    if (!event.once) this.client.on(event.name, event.execute.bind(null, this.client));
                    else this.client.once(event.name, event.execute.bind(null, this.client));

                    this.debug(`Loaded event ${event.name}`, 'success');
                } else {
                    this.debug(`Failed to load event ${filePath}`, 'error');
                }
            }
        }
    }

    #loadDatabase(path: string, tables: string[]): void {
        if (!this.#isDirectoryExists(path)) return;
        if (!Array.isArray(tables) || !tables.length) return;
        FS.mkdirSync(Path.resolve(path), { recursive: true });

        for (const table of tables) {
            const db = new KeyvSqlite(`sqlite://${Path.resolve(Path.join(path, `${table}.sqlite`))}`);
            const keyv = new Keyv({ store: db, namespace: table });
            this.database.set(table, keyv);
        }
    }

    #loadOptions(_options: ClientOP): any {
        const {
            commands: _commands,
            events: _events,
            database: _database,
            intents: _intents,
            partials: _partials,
            ...options
        } = _options;
        let intents: (Intents | string)[] = (
            Array.isArray(_intents) ? _intents.filter(Boolean) : [_intents]
        ) as string[];
        let partials: (Partials | string)[] = (
            Array.isArray(_partials) ? _partials.filter(Boolean) : [_partials]
        ) as string[];
        const commands = this.#isDirectoryExists(_commands) ? Path.resolve(_commands || 'commands') : null;
        const events = this.#isDirectoryExists(_events) ? Path.resolve(_events || 'events') : null;
        const database: Record<string, string | string[]> = {
            path: '',
            tables: []
        };
        if (_database?.path) {
            FS.mkdirSync(Path.resolve(_database.path), { recursive: true });
            database.path = Path.resolve(_database.path);
            database.tables = Array.isArray(_database.tables) ? _database.tables.filter(Boolean) : [_database.tables];
        }

        intents = intents.map((i) => Intents[i as keyof typeof Intents] || i) as Intents[];
        partials = partials.map((p) => Partials[p as keyof typeof Partials] || p) as Partials[];

        return {
            ...options,
            commands,
            events,
            database,
            intents,
            partials
        };
    }

    #isDirectoryExists(path: string | undefined): boolean {
        if (!path) return false;
        const pat = Path.resolve(path);
        return FS.existsSync(pat) && FS.lstatSync(pat).isDirectory();
    }

    public debug(message: string, type: 'info' | 'warn' | 'error' | 'success' = 'info'): void {
        switch (type) {
            case 'info':
                console.log(`[${blue('INFO')}] :: ${message}`);
                break;
            case 'warn':
                console.log(`[${yellow('WARN')}] :: ${message}`);
                break;
            case 'error':
                console.log(`[${red('ERROR')}] :: ${message}`);
                break;
            case 'success':
                console.log(`[${green('SUCCESS')}] :: ${message}`);
                break;
            default:
                console.log(`[${blue('INFO')}] :: ${message}`);
        }
    }
}
