import { AoiClient, type ClientOptions, type CacheOptions } from 'aoi.js';
import { Database, Functions } from 'aoijs.mysql';
import FS from 'node:fs';
import PATH from 'node:path';
import { red, blue, yellow, green } from 'chalk';

interface Status {
    type: 'Playing' | 'Streaming' | 'Listening' | 'Watching' | 'Custom';
    name: string;
    url?: string;
    time: number;
    status: 'online' | 'idle' | 'dnd' | 'invisible';
}

interface Variables {
    [key: string | number | `${string}`]: any;
}

interface DatabaseOP {
    url: string;
    tables?: string[];
    keepAoiDB?: boolean;
    debug?: boolean;
    backup?: {
        enable: boolean;
        directory: string;
    };
}

interface Client extends Omit<ClientOptions, 'cache' | 'commands' | 'status' | 'variables' | 'database'> {
    cache?: CacheOptions;
    commands?: string;
    database?: DatabaseOP;
    functions?: string;
    debug?: boolean;
    status?: Status[] | Status;
    variables?: {
        [table: string | number | `${string}`]: Variables;
    };
}

declare module 'aoi.js' {
    interface AoiClient {
        logger(message: string, type?: 'error' | 'warn' | 'info' | 'success'): void;
    }
}

export function createClient(options: Client): AoiClient {
    const { commands, status: statuses, database, debug, variables, ...rest } = options ?? {};

    const client = new AoiClient({
        cache: rest.cache ?? {},
        disableAoiDB: database?.keepAoiDB !== true,
        ...rest
    });

    if (database) {
        initializeDatabase(client, database, debug);
    }

    if (typeof variables === 'object') {
        for (const [table, vars] of Object.entries(variables)) {
            client.variables(vars, table ?? 'main');
        }
    }

    if (Array.isArray(statuses)) {
        for (const { type, name, url, time, status } of statuses) {
            client.status({
                type,
                name,
                url: url ?? '',
                time,
                status,
                shardID: 0
            });
        }
    } else if (typeof statuses === 'object') {
        const { type, name, url, time, status } = statuses;
        client.status({
            type,
            name,
            url: url ?? '',
            time,
            status,
            shardID: 0
        });
    }

    if (typeof options.functions === 'string' && FS.existsSync(PATH.join(__dirname, '..', options.functions))) {
        new Functions(client, PATH.join(__dirname, '..', options.functions), debug ?? false);
    }

    if (typeof commands === 'string' && FS.existsSync(PATH.join(__dirname, '..', commands))) {
        client.loadCommands(PATH.join(__dirname, '..', commands), debug ?? false);
    }

    client.logger = debugLog.bind(client);
    return client;
}

function initializeDatabase(client: AoiClient, options: DatabaseOP, debug = false): Database {
    return new Database(client, {
        url: options.url,
        tables: options.tables ?? ['main'],
        keepAoiDB: options.keepAoiDB ?? false,
        debug: debug ?? false
    });
}

function debugLog(message: string, type: 'error' | 'warn' | 'info' | 'success' = 'info'): void {
    switch (type) {
        case 'error':
            console.error(`[${red('ERROR')}] :: ${message}`);
            break;
        case 'warn':
            console.warn(`[${yellow('WARN')}] :: ${message}`);
            break;
        case 'info':
            console.info(`[${blue('INFO')}] :: ${message}`);
            break;
        case 'success':
            console.log(`[${green('SUCCESS')}] :: ${message}`);
            break;
        default:
            console.log(`[${blue('INFO')}] :: ${message}`);
    }
}
