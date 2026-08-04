// Copyright 2024 Electron Android Project
// Explorer View

import { SidebarView } from '../sidebar/SidebarView';

export interface FileNode {
    name: string;
    path: string;
    type: 'file' | 'directory';
    children?: FileNode[];
    expanded?: boolean;
}

export class ExplorerView extends SidebarView {
    private rootFolder: string = '';
    private fileTree: FileNode | null = null;
    private selectedPath: string | null = null;

    constructor() {
        super({ id: 'explorer', title: 'Explorer', icon: 'files' });
    }

    setRootFolder(path: string) {
        this.rootFolder = path;
    }

    setFileTree(tree: FileNode) {
        this.fileTree = tree;
        this.setData(tree);
    }

    handleAction(action: string, data?: any): void {
        switch (action) {
            case 'refresh':
                // TODO: Refresh file tree
                break;
            case 'newFile':
                this.createNewFile(data?.path);
                break;
            case 'newFolder':
                this.createNewFolder(data?.path);
                break;
            case 'rename':
                this.renameItem(data?.oldPath, data?.newPath);
                break;
            case 'delete':
                this.deleteItem(data?.path);
                break;
            case 'open':
                this.openFile(data?.path);
                break;
            case 'toggleFolder':
                this.toggleFolder(data?.path);
                break;
        }
    }

    private createNewFile(parentPath?: string) {
        // TODO: Implement file creation
    }

    private createNewFolder(parentPath?: string) {
        // TODO: Implement folder creation
    }

    private renameItem(oldPath: string, newPath: string) {
        // TODO: Implement rename
    }

    private deleteItem(path: string) {
        // TODO: Implement delete
    }

    private openFile(path: string) {
        this.selectedPath = path;
        // Send to main window to open file
        if (this.manager) {
            const { VSCodeEditor } = require('../editor/VSCodeEditor');
            // This would be handled via IPC
        }
    }

    private toggleFolder(path: string) {
        if (this.fileTree) {
            this.toggleNode(this.fileTree, path);
        }
    }

    private toggleNode(node: FileNode, path: string): boolean {
        if (node.path === path && node.type === 'directory') {
            node.expanded = !node.expanded;
            return true;
        }
        if (node.children) {
            for (const child of node.children) {
                if (this.toggleNode(child, path)) return true;
            }
        }
        return false;
    }

    getContent(): any {
        return {
            rootFolder: this.rootFolder,
            fileTree: this.fileTree,
            selectedPath: this.selectedPath,
        };
    }
}
