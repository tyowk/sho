import type { Context } from '../structures';

export default {
    name: 'ping',
    description: 'Ping! Pong! Ping! Pong!',
    execute: async (ctx: Context) => {
        await ctx.reply({
            content: `Pong! ${ctx.client.ws.ping}ms`,
            allowedMentions: {
                repliedUser: false,
                parse: []
            }
        });
    }
};
