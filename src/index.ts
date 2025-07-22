import { createClient } from './structures';

export default createClient({
    token: process.env.TOKEN ?? '',
    prefix: ['+', 'sho'],
    commands: './commands',
    debug: true,
    functions: './functions',
    intents: ['Guilds', 'GuildMessages', 'MessageContent', 'GuildMembers', 'GuildBans'],
    events: ['onMessage', 'onInteractionCreate'],
    database: {
        url: process.env.MYSQL ?? '',
        path: './database.sqlite',
        tables: ['main'],
        keepAoiDB: false
    },
    status: {
        type: 'Custom',
        name: "/help   |   I'm Sho!",
        time: 12,
        status: 'idle'
    },
    variables: {
        main: {
            test: 'value'
        }
    }
});
