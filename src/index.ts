import { Sho } from './structures';

export default Sho.createClient({
    token: process.env.TOKEN ?? '',
    commands: 'dist/commands',
    events: 'dist/events',
    database: {
        path: 'database',
        tables: ['prefix', 'users', 'cooldowns']
    },
    intents: ['Guilds', 'GuildMessages', 'MessageContent'],
    partials: ['Channel']
});
