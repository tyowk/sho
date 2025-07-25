import type { Context } from '../structures';

export default {
    name: 'prefix',
    description: 'Change the prefix for the guild',
    execute: async (ctx: Context, args: any[]) => {
        if (!ctx.guild) return;
        const prefix = args[0];
        const globalPrefix = ctx.client.sho.options.globalPrefix;

        if (!prefix)
            return await ctx.reply({
                content: `The current prefix is \`${(await ctx.client.prefix(ctx)) ?? globalPrefix}\``,
                allowedMentions: {
                    repliedUser: false,
                    parse: []
                }
            });

        await ctx.client.database.set('prefixes', ctx.guild.id, prefix.trim());
        return await ctx.reply({
            content: `The prefix has been changed to \`${prefix}\``,
            allowedMentions: {
                repliedUser: false,
                parse: []
            }
        });
    }
};
