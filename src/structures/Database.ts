import { Keyv, type KeyvEntry } from 'keyv';
import KeyvSqlite from '@keyv/sqlite';
import Path from 'node:path';

export interface DatabaseOptions {
    path: string;
    tables: string[];
}

export class Database {
    readonly #tables: Record<string, Keyv>;
    private readonly options: DatabaseOptions;
    private readonly tableNames: string[];
    private readonly path: string;

    constructor(options: DatabaseOptions) {
        if (!options?.path || !Array.isArray(options.tables)) throw new Error('Invalid database options provided!');
        if (!options.tables.length) throw new Error('No tables provided for the database!');
        if (!Path.isAbsolute(options.path)) throw new Error('Database path must be absolute!');
        if (Path.extname(options.path)) throw new Error('Database path must be a directory!');
        if (!Path.basename(options.path)) throw new Error('Database path must be a directory!');

        this.options = options;
        this.path = this.options.path;
        this.#tables = {};
        this.tableNames = this.options.tables;
        this.#init();
    }

    #init() {
        for (const table of this.tableNames) {
            const dbPath = `sqlite://${Path.join(this.path, `${table}.sqlite`)}`;
            const store = new KeyvSqlite(dbPath);
            this.#tables[table] = new Keyv({ store, namespace: table });
        }
    }

    public async get(table: string, key: string): Promise<any> {
        return await this.getTable(table).get(key);
    }

    public async set(table: string, key: string, value: any): Promise<boolean> {
        return await this.getTable(table).set(key, value);
    }

    public async delete(table: string, key: string): Promise<boolean> {
        return await this.getTable(table).delete(key);
    }

    public async clear(table: string): Promise<void> {
        return await this.getTable(table).clear();
    }

    public async has(table: string, key: string): Promise<boolean> {
        return await this.getTable(table).has(key);
    }

    public async getMany(table: string, keys: string[]): Promise<any[]> {
        return await this.getTable(table).getMany(keys);
    }

    public async setMany(table: string, entries: KeyvEntry[]): Promise<boolean[]> {
        return await this.getTable(table).setMany(entries);
    }

    public async deleteMany(table: string, keys: string[]): Promise<boolean> {
        return await this.getTable(table).deleteMany(keys);
    }

    public async hasMany(table: string, keys: string[]): Promise<boolean[]> {
        return await this.getTable(table).hasMany(keys);
    }

    public async disconnect(table: string): Promise<void> {
        return await this.getTable(table).disconnect();
    }

    public async disconnectAll(): Promise<void> {
        for (const table of this.tableNames) {
            await this.getTable(table).disconnect();
        }
    }

    public async clearAll(): Promise<void> {
        for (const table of this.tableNames) {
            await this.getTable(table).clear();
        }
    }

    public getTable(table: string): Keyv {
        const tableInstance = this.#tables[table] as Keyv;
        if (!tableInstance)
            throw new Error(
                `Database table with name "${table}"" does not exist! Make sure to add it to the database options.`
            );
        return tableInstance;
    }

    public getTables(): Record<string, Keyv> {
        return this.#tables;
    }
}
