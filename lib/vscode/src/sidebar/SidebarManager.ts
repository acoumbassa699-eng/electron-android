// Copyright 2024 Electron Android Project
// Sidebar Manager

import { VSCodeEditor } from '../editor/VSCodeEditor';
import { SidebarView } from './SidebarView';

export class SidebarManager {
    private editor: VSCodeEditor;
    private views: Map<string, SidebarView> = new Map();
    private activeViewId: string | null = null;
    private visible: boolean = true;
    private width: number = 250;
    private position: 'left' | 'right' = 'left';

    constructor(editor: VSCodeEditor) {
        this.editor = editor;
    }

    registerView(id: string, view: SidebarView) {
        this.views.set(id, view);
        view.setManager(this);
        
        // Send initial state
        this.editor.webContents.send('sidebar:viewRegistered', {
            id,
            title: view.getTitle(),
            icon: view.getIcon(),
        });
    }

    unregisterView(id: string) {
        const view = this.views.get(id);
        if (view) {
            view.destroy();
            this.views.delete(id);
        }
    }

    getView(id: string): SidebarView | undefined {
        return this.views.get(id);
    }

    getActiveView(): SidebarView | null {
        return this.activeViewId ? this.views.get(this.activeViewId) || null : null;
    }

    setActiveView(id: string | null) {
        if (id && this.views.has(id)) {
            this.activeViewId = id;
        } else {
            this.activeViewId = null;
        }
        this.render();
    }

    toggleView(id: string) {
        if (this.activeViewId === id && this.visible) {
            this.visible = false;
        } else {
            this.visible = true;
            this.activeViewId = id;
        }
        this.render();
    }

    show() {
        this.visible = true;
        this.render();
    }

    hide() {
        this.visible = false;
        this.render();
    }

    setWidth(width: number) {
        this.width = Math.max(150, Math.min(500, width));
        this.render();
    }

    getWidth(): number {
        return this.width;
    }

    setPosition(position: 'left' | 'right') {
        this.position = position;
        this.render();
    }

    private render() {
        this.editor.webContents.send('sidebar:updated', {
            visible: this.visible,
            width: this.width,
            position: this.position,
            activeViewId: this.activeViewId,
            views: Array.from(this.views.entries()).map(([id, view]) => ({
                id,
                title: view.getTitle(),
                icon: view.getIcon(),
            })),
        });
    }

    // Handle resize from renderer
    onResize(width: number) {
        this.setWidth(width);
    }

    // Handle view action from renderer
    onViewAction(viewId: string, action: string, data?: any) {
        const view = this.views.get(viewId);
        if (view) {
            view.handleAction(action, data);
        }
    }
}
