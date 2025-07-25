import type { Message, Client } from 'discord.js';
import { Context } from '../structures';

export default {
    name: 'messageCreate',
    ence: false,
    execute: async (client: Client, message: Message) => {
        if (message.author.bot) return;
        const ctx = new Context(message);
        const prefix = (await client.prefix(ctx)) ?? client.sho.options.globalPrefix;
        if (!prefix || !message.content.trim().toLowerCase().startsWith(prefix)) return;

        const args = message.content.trim().slice(prefix.length).trim().split(/ +/g);
        const commandName = args.shift()?.toLowerCase();
        if (!commandName) return;
        const command = client.commands.get(commandName);
        if (!command) return;

        try {
            await command.execute(ctx.setArgs(args), args);
        } catch (err) {
            client.debug(`Error executing command ${commandName}: ${err}`, 'error');
        }
    }
};
