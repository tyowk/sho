import type { Message } from 'discord.js';

export default {
    name: 'messageCreate',
    ence: false,
    execute: async (message: Message) => {
        if (message.author.bot) return;
        // SOON!
    }
};
