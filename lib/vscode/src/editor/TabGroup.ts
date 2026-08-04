// Copyright 2024 Electron Android Project
// Tab Group Component

import { VSCodeEditor } from './VSCodeEditor';

export interface Tab {
    id: string;
    name: string;
    path: string;
    modified: boolean;
    active: boolean;
}

export class TabGroup {
    private editor: VSCodeEditor;
    private tabs: Tab[] = [];
    private activeTabId: string | null = null;

    constructor(editor: VSCodeEditor) {
        this.editor = editor;
    }

    addTab(name: string, editorId: string): Tab {
        const tab: Tab = {
            id: editorId,
            name,
            path: '',
            modified: false,
            active: true,
        };

        // Deactivate current active tab
        this.tabs.forEach(t => t.active = false);
        
        this.tabs.push(tab);
        this.activeTabId = editorId;
        
        this.render();
        return tab;
    }

    removeTab(tabId: string) {
        const index = this.tabs.findIndex(t => t.id === tabId);
        if (index === -1) return;

        this.tabs.splice(index, 1);
        
        // If removed tab was active, activate another
        if (this.activeTabId === tabId && this.tabs.length > 0) {
            const newActiveIndex = Math.min(index, this.tabs.length - 1);
            this.activateTab(this.tabs[newActiveIndex].id);
        } else if (this.tabs.length === 0) {
            this.activeTabId = null;
        }

        this.render();
    }

    activateTab(tabId: string) {
        this.tabs.forEach(t => t.active = t.id === tabId);
        this.activeTabId = tabId;
        this.render();
    }

    updateTab(tabId: string, name: string, modified?: boolean) {
        const tab = this.tabs.find(t => t.id === tabId);
        if (tab) {
            if (name !== undefined) tab.name = name;
            if (modified !== undefined) tab.modified = modified;
            this.render();
        }
    }

    getActiveTab(): Tab | null {
        return this.tabs.find(t => t.id === this.activeTabId) || null;
    }

    getTabs(): Tab[] {
        return [...this.tabs];
    }

    private render() {
        // Send tab state to renderer
        this.editor.webContents.send('tabs:updated', {
            tabs: this.tabs,
            activeTabId: this.activeTabId,
        });
    }

    closeTab(tabId: string) {
        const tab = this.tabs.find(t => t.id === tabId);
        if (tab && tab.modified) {
            // TODO: Show confirmation dialog
            this.removeTab(tabId);
        } else {
            this.removeTab(tabId);
        }
    }

    closeAllTabs() {
        this.tabs = [];
        this.activeTabId = null;
        this.render();
    }

    closeOtherTabs(keepTabId: string) {
        this.tabs = this.tabs.filter(t => t.id === keepTabId);
        this.activeTabId = keepTabId;
        this.tabs.forEach(t => t.active = t.id === keepTabId);
        this.render();
    }

    closeTabsToRight(fromTabId: string) {
        const fromIndex = this.tabs.findIndex(t => t.id === fromTabId);
        if (fromIndex === -1) return;

        this.tabs = this.tabs.slice(0, fromIndex + 1);
        if (this.activeTabId && !this.tabs.find(t => t.id === this.activeTabId)) {
            this.activeTabId = this.tabs[this.tabs.length - 1]?.id || null;
            this.tabs.forEach(t => t.active = t.id === this.activeTabId);
        }
        this.render();
    }
}
