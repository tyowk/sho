import {
    Client as DiscordClient,
    type ClientOptions,
    GatewayIntentBits,
    Partials as DiscordPartials
} from 'discord.js';
import { Database, type DatabaseOptions } from './Database';
import { red, yellow, green, blue } from 'chalk';
import type { Context } from './Context';
import FS from 'node:fs';
import Path from 'node:path';

interface ShoOptions extends Omit<ClientOptions, 'intents' | 'partials'> {
    token: string;
    prefix: (ctx: Context) => Promise<string | undefined | null>;
    globalPrefix: string;
    commands: string;
    events: string;
    intents: (keyof typeof GatewayIntentBits)[];
    partials?: (keyof typeof DiscordPartials)[];
    production?: boolean;
    database: DatabaseOptions;
}

declare module 'discord.js' {
    interface Client {
        database: Database;
        debug(message: string, type?: 'info' | 'warn' | 'error' | 'success'): void;
        commands: Map<string, any>;
        prefix(ctx: Context): Promise<string | undefined | null>;
        sho: Sho;
    }
}

export class Sho {
    private readonly client: DiscordClient;
    public readonly options: ShoOptions;
    private readonly commands: Map<string, any> = new Map();
    private database: Database;

    constructor(opts: ShoOptions) {
        this.options = this.#normalizeOptions(opts) as any;
        this.database = new Database(this.options.database);
        this.client = new DiscordClient({
            ...this.options,
            intents: this.options.intents,
            partials: this.options.partials as any
        });

        this.client.commands = this.commands;
        this.client.database = this.database;
        this.client.prefix = this.options.prefix.bind(this);
        this.client.debug = this.debug.bind(this);
        this.client.sho = this;

        if (this.options.commands) this.loadCommands(this.options.commands);
        if (this.options.events) this.loadEvents(this.options.events);
        this.client.login(this.options.token);
    }

    public static createClient(opts: ShoOptions): Sho {
        return new Sho(opts);
    }

    #normalizeOptions(opts: ShoOptions): any {
        const intents = (opts.intents || []).map((i) => GatewayIntentBits[i as keyof typeof GatewayIntentBits]);
        const partials = (opts.partials || []).map((p) => DiscordPartials[p as keyof typeof DiscordPartials]);
        const commands = this.#validateDir(opts.commands);
        const events = this.#validateDir(opts.events);

        const token = opts.token;
        if (!token) throw new Error('Discord bot token is required for the client to login!');
        if (typeof token !== 'string') throw new Error('The hell... Discord bot token must be a string!');

        let database: DatabaseOptions | undefined;
        if (opts.database?.path && Array.isArray(opts.database.tables)) {
            const resolvedPath = Path.resolve(opts.database.path);
            FS.mkdirSync(resolvedPath, { recursive: true });
            database = { path: resolvedPath, tables: opts.database.tables.filter(Boolean) };
        }

        return {
            ...opts,
            token,
            intents,
            partials,
            commands,
            events,
            database
        };
    }

    public loadCommands(dir: string): void {
        const files = FS.readdirSync(dir);

        for (const file of files) {
            const filePath = Path.join(dir, file);
            const stat = FS.statSync(filePath);
            if (stat.isDirectory()) {
                this.loadCommands(filePath);
                continue;
            }

            if (!/\.(js|ts|cjs|mjs)$/.test(file)) continue;
            const commandModule = require(filePath);
            const command = commandModule.default || commandModule;

            if (command?.name && typeof command.execute === 'function') {
                this.commands.set(command.name, command);
                this.debug(`Loaded command ${command.name}`, 'success');
                continue;
            }

            this.debug(`Invalid command in ${filePath}`, 'error');
        }
    }

    public loadEvents(dir: string): void {
        const files = FS.readdirSync(dir);

        for (const file of files) {
            const filePath = Path.join(dir, file);
            const stat = FS.statSync(filePath);
            if (stat.isDirectory()) {
                this.loadEvents(filePath);
                continue;
            }

            if (!/\.(js|ts|cjs|mjs)$/.test(file)) continue;
            const eventModule = require(filePath);
            const event = eventModule.default || eventModule;
            if (event?.name && typeof event.execute === 'function') {
                const listener = event.execute.bind(null, this.client);
                event.once ? this.client.once(event.name, listener) : this.client.on(event.name, listener);
                this.debug(`Loaded event ${event.name}`, 'success');
                continue;
            }

            this.debug(`Invalid event in ${filePath}`, 'error');
        }
    }

    #validateDir(path?: string): string | undefined {
        if (!path) return undefined;
        const resolved = Path.resolve(path);
        return FS.existsSync(resolved) && FS.statSync(resolved).isDirectory() ? resolved : undefined;
    }

    public debug(message: string, type: 'info' | 'warn' | 'error' | 'success' = 'info'): void {
        const tag = {
            info: blue('INFO'),
            warn: yellow('WARN'),
            error: red('ERROR'),
            success: green('SUCCESS')
        }[type];

        console.log(`[${tag}] :: ${message}`);
    }
}
