// Copyright 2024 Electron Android Project
// Sidebar View Base Class

import { SidebarManager } from './SidebarManager';

export interface SidebarViewOptions {
    id: string;
    title: string;
    icon: string;
}

export abstract class SidebarView {
    protected id: string;
    protected title: string;
    protected icon: string;
    protected manager: SidebarManager | null = null;
    protected data: any = null;

    constructor(options: SidebarViewOptions) {
        this.id = options.id;
        this.title = options.title;
        this.icon = options.icon;
    }

    getId(): string { return this.id; }
    getTitle(): string { return this.title; }
    getIcon(): string { return this.icon; }

    setManager(manager: SidebarManager) {
        this.manager = manager;
    }

    // Called when the view becomes active
    onActivate() {}

    // Called when the view becomes inactive
    onDeactivate() {}

    // Called when the view receives new data
    setData(data: any) {
        this.data = data;
        this.onDataChanged();
    }

    // Override to handle data changes
    protected onDataChanged() {}

    // Handle actions from renderer
    abstract handleAction(action: string, data?: any): void;

    // Get view content for renderer
    abstract getContent(): any;

    // Clean up
    destroy() {}
}
