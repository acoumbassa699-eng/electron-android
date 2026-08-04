// Copyright 2024 Electron Android Project
// Status Bar Manager

import { VSCodeEditor } from '../editor/VSCodeEditor';

export interface StatusBarItem {
    id: string;
    text: string;
    align: 'left' | 'right';
    color?: string;
    backgroundColor?: string;
    command?: string;
    tooltip?: string;
    priority: number;
}

export class StatusBarManager {
    private editor: VSCodeEditor;
    private items: Map<string, StatusBarItem> = new Map();
    private leftItems: StatusBarItem[] = [];
    private rightItems: StatusBarItem[] = [];

    constructor(editor: VSCodeEditor) {
        this.editor = editor;
        this.initializeDefaultItems();
    }

    private initializeDefaultItems() {
        // Add default status bar items
        this.setItem('branch', 'main', 'left', undefined, 1);
        this.setItem('position', 'Ln 1, Col 1', 'left', undefined, 2);
        this.setItem('language', 'Plain Text', 'left', undefined, 3);
        this.setItem('encoding', 'UTF-8', 'right', undefined, 4);
        this.setItem('eol', 'LF', 'right', undefined, 5);
        this.setItem('spaces', 'Spaces: 4', 'right', undefined, 6);
        this.setItem('notifications', '', 'right', undefined, 100);
    }

    setItem(id: string, text: string, align?: 'left' | 'right', color?: string, priority?: number) {
        const item: StatusBarItem = {
            id,
            text,
            align: align || 'left',
            color,
            priority: priority || 50,
        };

        this.items.set(id, item);
        this.rebuildLists();
        this.render();
    }

    removeItem(id: string) {
        this.items.delete(id);
        this.rebuildLists();
        this.render();
    }

    getItem(id: string): StatusBarItem | undefined {
        return this.items.get(id);
    }

    private rebuildLists() {
        this.leftItems = [];
        this.rightItems = [];

        for (const item of this.items.values()) {
            if (item.align === 'left') {
                this.leftItems.push(item);
            } else {
                this.rightItems.push(item);
            }
        }

        // Sort by priority
        this.leftItems.sort((a, b) => a.priority - b.priority);
        this.rightItems.sort((a, b) => a.priority - b.priority);
    }

    private render() {
        this.editor.webContents.send('statusBar:updated', {
            leftItems: this.leftItems,
            rightItems: this.rightItems,
        });
    }

    // Convenience methods
    setBranch(branch: string) {
        this.setItem('branch', branch, 'left', '#4ec9b0');
    }

    setPosition(line: number, column: number) {
        this.setItem('position', `Ln ${line}, Col ${column}`, 'left');
    }

    setLanguage(language: string) {
        this.setItem('language', language, 'left', '#c586c0');
    }

    setEncoding(encoding: string) {
        this.setItem('encoding', encoding, 'right');
    }

    setEndOfLine(eol: 'LF' | 'CRLF') {
        this.setItem('eol', eol, 'right');
    }

    setIndentation(spaces: number) {
        this.setItem('spaces', `Spaces: ${spaces}`, 'right');
    }

    showNotification(text: string, color?: string) {
        this.setItem('notifications', text, 'right', color || '#f44747', 100);
    }
}
