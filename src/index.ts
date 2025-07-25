import { Sho, type Context } from './structures';

export default Sho.createClient({
    token: process.env.TOKEN ?? '',
    commands: 'dist/commands',
    events: 'dist/events',
    intents: ['Guilds', 'GuildMessages', 'MessageContent', 'GuildMembers'],
    partials: ['Channel'],
    globalPrefix: 'sho',
    prefix: async (ctx: Context): Promise<string | undefined | null> => {
        if (!ctx.guild?.id) return void 0;
        return await ctx.client.database.get('prefixes', ctx.guild?.id);
    },
    database: {
        path: 'database',
        tables: ['prefixes']
    }
});
