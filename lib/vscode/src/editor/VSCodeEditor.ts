// Copyright 2024 Electron Android Project
// VS Code Editor Main Window

import { BrowserWindow, FileFilter } from 'electron';
import * as path from 'path';
import * as fs from 'fs';
import { TabGroup } from './TabGroup';
import { EditorPane } from './EditorPane';
import { Minimap } from './minimap/Minimap';
import { SuggestionsWidget } from './suggestions/SuggestionsWidget';

export interface VSCodeEditorOptions {
    width: number;
    height: number;
    minWidth?: number;
    minHeight?: number;
    backgroundColor?: string;
    webPreferences?: Electron.WebPreferences;
    titleBarStyle?: 'default' | 'hidden' | 'hiddenInset';
    frame?: boolean;
    show?: boolean;
}

interface OpenFile {
    path: string;
    name: string;
    content: string;
    modified: boolean;
}

export class VSCodeEditor extends BrowserWindow {
    private tabs: TabGroup;
    private editorPanes: Map<string, EditorPane> = new Map();
    private activeEditorId: string | null = null;
    private terminals: Map<string, any> = new Map();
    private minimap: Minimap | null = null;
    private suggestionsWidget: SuggestionsWidget | null = null;
    private fileTree: any = null;
    private searchResults: any = null;

    constructor(options: VSCodeEditorOptions) {
        super(options);
        
        this.tabs = new TabGroup(this);
        this.setupEventHandlers();
    }

    private setupEventHandlers() {
        this.on('resize', () => this.onWindowResize());
        this.on('maximize', () => this.sendToRenderer('window:maximized', true));
        this.on('unmaximize', () => this.sendToRenderer('window:maximized', false));
    }

    private sendToRenderer(channel: string, ...args: any[]) {
        if (!this.webContents.isDestroyed()) {
            this.webContents.send(channel, ...args);
        }
    }

    private onWindowResize() {
        this.sendToRenderer('window:resized', {
            width: this.getBounds().width,
            height: this.getBounds().height,
        });
    }

    // File operations
    async openFile(filePath: string): Promise<boolean> {
        try {
            const content = await fs.promises.readFile(filePath, 'utf-8');
            const name = path.basename(filePath);
            
            const file: OpenFile = { path: filePath, name, content, modified: false };
            
            if (this.activeEditorId && this.editorPanes.has(this.activeEditorId)) {
                const editor = this.editorPanes.get(this.activeEditorId)!;
                if (this.editorPanes.size > 1) {
                    // Open in new tab
                    return this.createNewEditor(file);
                }
            }
            
            return this.createNewEditor(file);
        } catch (error) {
            console.error('Failed to open file:', error);
            return false;
        }
    }

    private createNewEditor(file: OpenFile): boolean {
        const editorId = `editor-${Date.now()}`;
        const editor = new EditorPane(this, file.path, file.name, file.content);
        
        this.editorPanes.set(editorId, editor);
        this.activeEditorId = editorId;
        this.tabs.addTab(file.name, editorId);
        
        this.sendToRenderer('editor:opened', {
            id: editorId,
            path: file.path,
            name: file.name,
            content: file.content,
        });
        
        return true;
    }

    async saveFile(filePath: string, content: string): Promise<boolean> {
        try {
            await fs.promises.writeFile(filePath, content, 'utf-8');
            
            // Update editor state
            for (const [id, editor] of this.editorPanes) {
                if (editor.getPath() === filePath) {
                    editor.setModified(false);
                    break;
                }
            }
            
            return true;
        } catch (error) {
            console.error('Failed to save file:', error);
            return false;
        }
    }

    getOpenFiles(): OpenFile[] {
        const files: OpenFile[] = [];
        for (const editor of this.editorPanes.values()) {
            files.push({
                path: editor.getPath(),
                name: editor.getName(),
                content: editor.getContent(),
                modified: editor.isModified(),
            });
        }
        return files;
    }

    // UI operations
    newFile() {
        const untitledFile: OpenFile = {
            path: '',
            name: 'Untitled',
            content: '',
            modified: false,
        };
        this.createNewEditor(untitledFile);
    }

    async openFileDialog() {
        const { dialog } = require('electron');
        const result = await dialog.showOpenDialog(this, {
            properties: ['openFile'],
            filters: [
                { name: 'All Files', extensions: ['*'] },
                { name: 'JavaScript', extensions: ['js', 'ts', 'jsx', 'tsx'] },
                { name: 'Python', extensions: ['py'] },
                { name: 'HTML', extensions: ['html', 'htm'] },
                { name: 'CSS', extensions: ['css', 'scss', 'less'] },
                { name: 'JSON', extensions: ['json'] },
                { name: 'Markdown', extensions: ['md', 'markdown'] },
            ],
        });

        if (!result.canceled && result.filePaths.length > 0) {
            for (const filePath of result.filePaths) {
                await this.openFile(filePath);
            }
        }
    }

    async openFolderDialog() {
        const { dialog } = require('electron');
        const result = await dialog.showOpenDialog(this, {
            properties: ['openDirectory'],
        });

        if (!result.canceled && result.filePaths.length > 0) {
            this.loadFolder(result.filePaths[0]);
        }
    }

    private async loadFolder(folderPath: string) {
        this.fileTree = await this.buildFileTree(folderPath);
        this.sendToRenderer('explorer:folderLoaded', this.fileTree);
    }

    private async buildFileTree(dirPath: string, depth = 0): Promise<any> {
        if (depth > 5) return null; // Limit depth
        
        const entries = await fs.promises.readdir(dirPath, { withFileTypes: true });
        const items = [];

        for (const entry of entries) {
            if (entry.name.startsWith('.')) continue; // Skip hidden files
            
            const fullPath = path.join(dirPath, entry.name);
            
            if (entry.isDirectory()) {
                const children = await this.buildFileTree(fullPath, depth + 1);
                if (children && children.children.length > 0) {
                    items.push({
                        name: entry.name,
                        path: fullPath,
                        type: 'directory',
                        children: children.children,
                    });
                }
            } else {
                items.push({
                    name: entry.name,
                    path: fullPath,
                    type: 'file',
                });
            }
        }

        return { path: dirPath, children: items };
    }

    saveCurrentFile() {
        if (this.activeEditorId) {
            const editor = this.editorPanes.get(this.activeEditorId);
            if (editor) {
                if (editor.getPath()) {
                    this.saveFile(editor.getPath(), editor.getContent());
                } else {
                    this.saveFileAs();
                }
            }
        }
    }

    async saveFileAs() {
        if (!this.activeEditorId) return;
        
        const editor = this.editorPanes.get(this.activeEditorId);
        if (!editor) return;

        const { dialog } = require('electron');
        const result = await dialog.showSaveDialog(this, {
            defaultPath: editor.getName(),
        });

        if (!result.canceled && result.filePath) {
            await this.saveFile(result.filePath, editor.getContent());
            editor.setPath(result.filePath);
            editor.setName(path.basename(result.filePath));
            this.tabs.updateTab(this.activeEditorId, editor.getName());
        }
    }

    // Find/Replace
    showFind() {
        this.sendToRenderer('find:show');
    }

    showReplace() {
        this.sendToRenderer('find:showReplace');
    }

    showCommandPalette() {
        this.sendToRenderer('commandPalette:show');
    }

    // Terminal operations
    createTerminal(id: string) {
        const terminal = {
            id,
            cols: 80,
            rows: 24,
            pid: null,
        };
        this.terminals.set(id, terminal);
        this.sendToRenderer('terminal:created', { id, cols: terminal.cols, rows: terminal.rows });
        return id;
    }

    writeTerminal(id: string, data: string) {
        const terminal = this.terminals.get(id);
        if (terminal) {
            this.sendToRenderer('terminal:data', { id, data });
        }
    }

    resizeTerminal(id: string, cols: number, rows: number) {
        const terminal = this.terminals.get(id);
        if (terminal) {
            terminal.cols = cols;
            terminal.rows = rows;
        }
    }

    killTerminal() {
        if (this.activeEditorId && this.terminals.has(this.activeEditorId)) {
            this.terminals.delete(this.activeEditorId);
            this.sendToRenderer('terminal:closed', { id: this.activeEditorId });
        }
    }

    clearTerminal() {
        this.sendToRenderer('terminal:cleared');
    }
}
