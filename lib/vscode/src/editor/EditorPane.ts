// Copyright 2024 Electron Android Project
// Editor Pane Component

import { VSCodeEditor } from './VSCodeEditor';

export interface CursorPosition {
    line: number;
    column: number;
}

export interface Selection {
    start: CursorPosition;
    end: CursorPosition;
}

export class EditorPane {
    private editor: VSCodeEditor;
    private path: string;
    private name: string;
    private content: string;
    private language: string;
    private modified: boolean;
    private cursorPosition: CursorPosition = { line: 1, column: 1 };
    private selection: Selection | null = null;
    private scrollPosition = { x: 0, y: 0 };
    private undoStack: string[] = [];
    private redoStack: string[] = [];
    private maxUndoSteps = 100;

    constructor(editor: VSCodeEditor, path: string, name: string, content: string) {
        this.editor = editor;
        this.path = path;
        this.name = name;
        this.content = content;
        this.language = this.detectLanguage(name);
        this.modified = false;
    }

    private detectLanguage(filename: string): string {
        const ext = filename.split('.').pop()?.toLowerCase() || '';
        const languageMap: Record<string, string> = {
            'js': 'javascript',
            'ts': 'typescript',
            'jsx': 'javascriptreact',
            'tsx': 'typescriptreact',
            'py': 'python',
            'html': 'html',
            'htm': 'html',
            'css': 'css',
            'scss': 'scss',
            'less': 'less',
            'json': 'json',
            'md': 'markdown',
            'yaml': 'yaml',
            'yml': 'yaml',
            'xml': 'xml',
            'sh': 'shell',
            'bash': 'shell',
            'sql': 'sql',
            'go': 'go',
            'rs': 'rust',
            'java': 'java',
            'c': 'c',
            'cpp': 'cpp',
            'h': 'cpp',
            'hpp': 'cpp',
            'rb': 'ruby',
            'php': 'php',
            'swift': 'swift',
            'kt': 'kotlin',
            'dart': 'dart',
        };
        return languageMap[ext] || 'plaintext';
    }

    // Getters
    getPath(): string { return this.path; }
    getName(): string { return this.name; }
    getContent(): string { return this.content; }
    getLanguage(): string { return this.language; }
    isModified(): boolean { return this.modified; }
    getCursorPosition(): CursorPosition { return { ...this.cursorPosition }; }
    getSelection(): Selection | null { return this.selection ? { ...this.selection } : null; }

    // Setters
    setPath(path: string) { this.path = path; }
    setName(name: string) { this.name = name; }
    setModified(modified: boolean) {
        this.modified = modified;
        this.sendUpdate();
    }

    setContent(content: string, recordUndo = true) {
        if (recordUndo && this.content !== content) {
            this.undoStack.push(this.content);
            if (this.undoStack.length > this.maxUndoSteps) {
                this.undoStack.shift();
            }
            this.redoStack = [];
            this.modified = true;
        }
        this.content = content;
        this.sendUpdate();
    }

    setCursorPosition(line: number, column: number) {
        this.cursorPosition = { line, column };
        this.sendCursorUpdate();
    }

    setSelection(start: CursorPosition, end: CursorPosition) {
        this.selection = { start, end };
        this.sendCursorUpdate();
    }

    clearSelection() {
        this.selection = null;
        this.sendCursorUpdate();
    }

    setScrollPosition(x: number, y: number) {
        this.scrollPosition = { x, y };
    }

    // Undo/Redo
    undo() {
        if (this.undoStack.length > 0) {
            this.redoStack.push(this.content);
            this.content = this.undoStack.pop() || '';
            this.sendUpdate();
        }
    }

    redo() {
        if (this.redoStack.length > 0) {
            this.undoStack.push(this.content);
            this.content = this.redoStack.pop() || '';
            this.sendUpdate();
        }
    }

    // Text operations
    insertText(text: string, position?: CursorPosition) {
        if (position) {
            const lines = this.content.split('\n');
            const lineIndex = Math.min(position.line - 1, lines.length - 1);
            const line = lines[lineIndex];
            const columnIndex = Math.min(position.column - 1, line.length);
            lines[lineIndex] = line.substring(0, columnIndex) + text + line.substring(columnIndex);
            this.setContent(lines.join('\n'));
        } else {
            this.setContent(this.content + text);
        }
    }

    deleteText(start: CursorPosition, end: CursorPosition) {
        const lines = this.content.split('\n');
        const startLineIndex = start.line - 1;
        const endLineIndex = end.line - 1;
        
        const startLine = lines[startLineIndex] || '';
        const endLine = lines[endLineIndex] || '';
        
        const newStartLine = startLine.substring(0, start.column - 1);
        const newEndLine = endLine.substring(end.column - 1);
        
        lines[startLineIndex] = newStartLine + newEndLine;
        lines.splice(startLineIndex + 1, endLineIndex - startLineIndex);
        
        this.setContent(lines.join('\n'));
    }

    // Find
    find(pattern: string, options?: { regex?: boolean; wholeWord?: boolean; caseSensitive?: boolean }): CursorPosition[] {
        const matches: CursorPosition[] = [];
        const lines = this.content.split('\n');
        
        let flags = 'g';
        if (!options?.caseSensitive) flags += 'i';
        
        const regex = options?.regex 
            ? new RegExp(pattern, flags)
            : new RegExp(this.escapeRegex(pattern), flags);
        
        lines.forEach((line, lineIndex) => {
            let match;
            while ((match = regex.exec(line)) !== null) {
                matches.push({
                    line: lineIndex + 1,
                    column: match.index + 1,
                });
            }
        });
        
        return matches;
    }

    private escapeRegex(string: string): string {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    // Replace
    replace(pattern: string, replacement: string, options?: { regex?: boolean; wholeWord?: boolean; caseSensitive?: boolean; replaceAll?: boolean }) {
        let flags = 'g';
        if (!options?.caseSensitive) flags += 'i';
        
        const regex = options?.regex 
            ? new RegExp(pattern, flags)
            : new RegExp(this.escapeRegex(pattern), flags);
        
        if (options?.replaceAll) {
            this.setContent(this.content.replace(regex, replacement));
        } else {
            this.setContent(this.content.replace(regex, replacement));
        }
    }

    // Get line count and info
    getLineCount(): number {
        return this.content.split('\n').length;
    }

    getLine(lineNumber: number): string {
        const lines = this.content.split('\n');
        return lines[lineNumber - 1] || '';
    }

    getLineLength(lineNumber: number): number {
        return this.getLine(lineNumber).length;
    }

    // Send updates to renderer
    private sendUpdate() {
        this.editor.webContents.send('editor:contentChanged', {
            path: this.path,
            name: this.name,
            content: this.content,
            modified: this.modified,
            language: this.language,
        });
    }

    private sendCursorUpdate() {
        this.editor.webContents.send('editor:cursorChanged', {
            position: this.cursorPosition,
            selection: this.selection,
        });
    }
}
