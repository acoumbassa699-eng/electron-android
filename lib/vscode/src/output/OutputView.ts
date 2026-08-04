// Copyright 2024 Electron Android Project
// Output View

import { SidebarView } from '../sidebar/SidebarView';

export interface OutputChannel {
    id: string;
    name: string;
    logs: string[];
    maxLines: number;
}

export class OutputView extends SidebarView {
    private channels: Map<string, OutputChannel> = new Map();
    private activeChannelId: string | null = null;
    private maxLinesPerChannel = 1000;

    constructor() {
        super({ id: 'output', title: 'Output', icon: 'output' });
        
        // Create default channel
        this.createChannel('default', 'Output');
    }

    createChannel(id: string, name: string): OutputChannel {
        const channel: OutputChannel = {
            id,
            name,
            logs: [],
            maxLines: this.maxLinesPerChannel,
        };
        this.channels.set(id, channel);
        
        if (!this.activeChannelId) {
            this.activeChannelId = id;
        }
        
        return channel;
    }

    getChannel(id: string): OutputChannel | undefined {
        return this.channels.get(id);
    }

    appendLine(channelId: string, line: string) {
        const channel = this.channels.get(channelId);
        if (channel) {
            channel.logs.push(line);
            
            // Trim if exceeds max lines
            if (channel.logs.length > channel.maxLines) {
                channel.logs.splice(0, channel.logs.length - channel.maxLines);
            }
        }
    }

    append(channelId: string, text: string) {
        const lines = text.split('\n');
        for (const line of lines) {
            this.appendLine(channelId, line);
        }
    }

    clear(channelId: string) {
        const channel = this.channels.get(channelId);
        if (channel) {
            channel.logs = [];
        }
    }

    clearAll() {
        for (const channel of this.channels.values()) {
            channel.logs = [];
        }
    }

    setActiveChannel(id: string) {
        if (this.channels.has(id)) {
            this.activeChannelId = id;
        }
    }

    getActiveChannel(): OutputChannel | null {
        return this.activeChannelId ? this.channels.get(this.activeChannelId) || null : null;
    }

    handleAction(action: string, data?: any): void {
        switch (action) {
            case 'append':
                this.append(data?.channelId || 'default', data?.text);
                break;
            case 'appendLine':
                this.appendLine(data?.channelId || 'default', data?.line);
                break;
            case 'clear':
                this.clear(data?.channelId || this.activeChannelId!);
                break;
            case 'clearAll':
                this.clearAll();
                break;
            case 'setActive':
                this.setActiveChannel(data?.channelId);
                break;
            case 'createChannel':
                this.createChannel(data?.id, data?.name);
                break;
        }
    }

    getContent(): any {
        const activeChannel = this.getActiveChannel();
        return {
            channels: Array.from(this.channels.values()).map(ch => ({
                id: ch.id,
                name: ch.name,
                lineCount: ch.logs.length,
            })),
            activeChannelId: this.activeChannelId,
            activeChannelLogs: activeChannel?.logs || [],
        };
    }
}
