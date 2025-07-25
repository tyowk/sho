import {
    type Client,
    type User,
    type Guild,
    type GuildMember,
    type OmitPartialGroupDMChannel,
    type InteractionCallbackResponse,
    type MessagePayload,
    type MessageReplyOptions,
    type MessageCreateOptions,
    type Channel,
    type CategoryChannel,
    type PartialGroupDMChannel,
    type PartialDMChannel,
    type ForumChannel,
    type MediaChannel,
    type InteractionReplyOptions,
    type InteractionEditReplyOptions,
    type InteractionResponse,
    Message,
    ChatInputCommandInteraction,
    MessageComponentInteraction,
    ModalSubmitInteraction,
    ContextMenuCommandInteraction
} from 'discord.js';

export type Interaction =
    | ChatInputCommandInteraction
    | MessageComponentInteraction
    | ModalSubmitInteraction
    | ContextMenuCommandInteraction;

export type InteractionEdit = string | MessagePayload | InteractionEditReplyOptions;

export type InteractionWithMessage = Interaction | Message;

export type SendData = string | MessagePayload | MessageReplyOptions | MessageCreateOptions;

export type MessageReplyData = string | MessagePayload | MessageReplyOptions;

export type InteractionReplyData =
    | string
    | (InteractionReplyOptions & {
          fetchReply?: boolean;
          withResponse?: boolean;
      });

export type SendableChannel = Exclude<
    Channel,
    CategoryChannel | PartialGroupDMChannel | PartialDMChannel | ForumChannel | MediaChannel
> | null;

export class Context {
    public readonly client: Client;
    public args?: Array<string>;
    public channel?: SendableChannel;
    public user?: User | null;
    public member?: GuildMember | null;
    public guild?: Guild | null;
    public message?: Message | null;
    public interaction?: Interaction | null;
    #msg: Message | null = null;

    constructor(ctx: InteractionWithMessage, args?: Array<string>) {
        this.client = ctx.client;
        this.interaction = ctx instanceof Message ? void 0 : ctx;
        this.user = ctx instanceof Message ? ctx.author : ctx.user;
        this.message = ctx instanceof Message ? ctx : void 0;
        this.channel = ctx.channel as SendableChannel;
        this.args = args ?? [];
        this.member = ctx.member as GuildMember;
        this.guild = ctx.guild;
    }

    private get isInteraction(): boolean {
        return (
            !!this.interaction &&
            (this.interaction instanceof ChatInputCommandInteraction ||
                this.interaction instanceof MessageComponentInteraction ||
                this.interaction instanceof ModalSubmitInteraction ||
                this.interaction instanceof ContextMenuCommandInteraction)
        );
    }

    private get isReplied(): boolean {
        if (!this.isInteraction) return false;
        return (this.interaction?.replied || this.interaction?.deferred) ?? false;
    }

    public async send(data: SendData): Promise<Message<boolean> | undefined> {
        if (this.isInteraction && !this.isReplied) return await this.reply(data);

        if (!this.channel) return void 0;
        if (this.channel.partial) await this.channel.fetch();
        this.#msg = await this.channel.send(data);
        return this.#msg;
    }

    public async reply(
        data: MessageReplyData
    ): Promise<Message<boolean> | OmitPartialGroupDMChannel<Message<boolean>> | undefined>;
    public async reply(data: InteractionReplyData): Promise<InteractionCallbackResponse>;
    public async reply(data: any): Promise<any> {
        if (this.isInteraction && this.interaction && !this.isReplied) {
            const callback = await this.interaction.reply({ ...data, withResponse: true });
            this.#msg = callback.resource?.message ?? null;
            return this.#msg;
        }

        if (!this.message) return await this.send(data);
        if (this.message.partial) await this.message.fetch();
        this.#msg = await this.message.reply(data);
        return this.#msg;
    }

    public async editReply(data: InteractionEdit): Promise<Message<boolean> | undefined> {
        if (this.isInteraction && this.interaction && this.isReplied) {
            return await this.interaction.editReply(data);
        }

        if (!this.#msg) return void 0;
        if (this.#msg.partial) this.#msg = await this.#msg.fetch();
        return await this.#msg.edit(data);
    }

    public async deleteReply(): Promise<void> {
        if (this.isInteraction && this.interaction && this.isReplied) {
            return await this.interaction.deleteReply();
        }

        if (!this.#msg) return void 0;
        if (this.#msg.partial) this.#msg = await this.#msg.fetch();
        if (this.#msg.deletable) await this.#msg.delete();
    }

    public async fetchReply(): Promise<Message<boolean> | undefined> {
        if (this.isInteraction && this.interaction && this.isReplied) {
            return await this.interaction.fetchReply();
        }

        if (!this.#msg) return void 0;
        if (this.#msg.partial) this.#msg = await this.#msg.fetch();
        return this.#msg;
    }

    public async deferReply(ephemeral = false): Promise<InteractionCallbackResponse | undefined> {
        if (!this.isInteraction) return;
        return await this.interaction?.deferReply({ ephemeral, withResponse: true });
    }

    public async followUp(
        data: MessageReplyData
    ): Promise<Message<boolean> | OmitPartialGroupDMChannel<Message<boolean>> | undefined>;
    public async followUp(data: InteractionReplyData): Promise<InteractionCallbackResponse>;
    public async followUp(data: any): Promise<any> {
        if (this.isInteraction && this.interaction && this.isReplied) {
            return await this.interaction.followUp(data);
        }

        if (!this.#msg) return void 0;
        if (this.#msg.partial) this.#msg = await this.#msg.fetch();
        return await this.#msg.reply(data);
    }

    public async deferUpdate(): Promise<InteractionCallbackResponse | undefined> {
        if (!this.isInteraction || !this.interaction?.isMessageComponent()) return;
        return await this.interaction?.deferUpdate({ withResponse: true });
    }

    public async update(data: InteractionEdit): Promise<InteractionResponse<boolean> | Message<boolean> | undefined> {
        if (this.isInteraction && this.interaction && this.isReplied) {
            if (!this.interaction.isMessageComponent()) return;
            return await this.interaction?.update(data);
        }

        if (!this.#msg) return void 0;
        if (this.#msg.partial) this.#msg = await this.#msg.fetch();
        return await this.#msg.edit(data);
    }

    public setArgs(args: any[]): Context {
        this.args = args;
        return this;
    }
}
