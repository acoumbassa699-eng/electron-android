// Copyright 2024 Electron Android Project
// Terminal View

import { SidebarView } from '../sidebar/SidebarView';

export interface TerminalSession {
    id: string;
    name: string;
    pid?: number;
    cols: number;
    rows: number;
    cwd?: string;
    env?: Record<string, string>;
}

export class TerminalView extends SidebarView {
    private terminals: Map<string, TerminalSession> = new Map();
    private activeTerminalId: string | null = null;
    private outputBuffer: Map<string, string[]> = new Map();

    constructor() {
        super({ id: 'terminal', title: 'Terminal', icon: 'terminal' });
    }

    createTerminal(options?: Partial<TerminalSession>): string {
        const id = options?.id || `terminal-${Date.now()}`;
        const session: TerminalSession = {
            id,
            name: options?.name || 'Terminal',
            cols: options?.cols || 80,
            rows: options?.rows || 24,
            cwd: options?.cwd || process.cwd(),
            env: options?.env,
        };
        
        this.terminals.set(id, session);
        this.outputBuffer.set(id, []);
        this.activeTerminalId = id;
        
        return id;
    }

    getTerminal(id: string): TerminalSession | undefined {
        return this.terminals.get(id);
    }

    getActiveTerminal(): TerminalSession | null {
        return this.activeTerminalId ? this.terminals.get(this.activeTerminalId) || null : null;
    }

    setActiveTerminal(id: string) {
        if (this.terminals.has(id)) {
            this.activeTerminalId = id;
        }
    }

    writeToTerminal(id: string, data: string) {
        const buffer = this.outputBuffer.get(id);
        if (buffer) {
            buffer.push(data);
            // Keep buffer size manageable
            if (buffer.length > 1000) {
                buffer.splice(0, buffer.length - 1000);
            }
        }
    }

    getTerminalOutput(id: string): string[] {
        return this.outputBuffer.get(id) || [];
    }

    clearTerminal(id: string) {
        this.outputBuffer.set(id, []);
    }

    resizeTerminal(id: string, cols: number, rows: number) {
        const terminal = this.terminals.get(id);
        if (terminal) {
            terminal.cols = cols;
            terminal.rows = rows;
        }
    }

    closeTerminal(id: string) {
        this.terminals.delete(id);
        this.outputBuffer.delete(id);
        
        if (this.activeTerminalId === id) {
            // Activate another terminal or set to null
            const remaining = Array.from(this.terminals.keys());
            this.activeTerminalId = remaining.length > 0 ? remaining[0] : null;
        }
    }

    handleAction(action: string, data?: any): void {
        switch (action) {
            case 'create':
                this.createTerminal(data?.options);
                break;
            case 'write':
                this.writeToTerminal(data?.id, data?.data);
                break;
            case 'resize':
                this.resizeTerminal(data?.id, data?.cols, data?.rows);
                break;
            case 'clear':
                this.clearTerminal(data?.id || this.activeTerminalId!);
                break;
            case 'close':
                this.closeTerminal(data?.id);
                break;
            case 'setActive':
                this.setActiveTerminal(data?.id);
                break;
            case 'rename':
                const term = this.terminals.get(data?.id);
                if (term) term.name = data?.name;
                break;
        }
    }

    getContent(): any {
        return {
            terminals: Array.from(this.terminals.values()),
            activeTerminalId: this.activeTerminalId,
        };
    }
}
