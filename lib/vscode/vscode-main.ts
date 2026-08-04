// Copyright 2024 Electron Android Project
// VS Code-like Editor Main Module

import { app, BrowserWindow, ipcMain, Menu, Tray, nativeImage } from 'electron';
import * as path from 'path';
import { VSCodeEditor } from './src/editor/VSCodeEditor';
import { SidebarManager } from './src/sidebar/SidebarManager';
import { StatusBarManager } from './src/statusbar/StatusBarManager';
import { ExplorerView } from './src/explorer/ExplorerView';
import { SearchView } from './src/search/SearchView';
import { TerminalView } from './src/terminal/TerminalView';
import { OutputView } from './src/output/OutputView';
import { CommandPalette } from './src/common/commands/CommandPalette';
import { ThemeManager } from './src/platform/theme/ThemeManager';
import { WindowManager } from './src/platform/window/WindowManager';
import { KeyBindings } from './src/common/commands/KeyBindings';
import { ExtensionHost } from './src/common/extensions/ExtensionHost';

// Global references
let mainWindow: VSCodeEditor | null = null;
let sidebarManager: SidebarManager | null = null;
let statusBarManager: StatusBarManager | null = null;
let tray: Tray | null = null;

// Disable hardware acceleration for better performance on low-end devices
app.disableHardwareAcceleration();

// Single instance lock
const gotTheLock = app.requestSingleInstanceLock();

if (!gotTheLock) {
    app.quit();
} else {
    app.on('second-instance', () => {
        if (mainWindow) {
            if (mainWindow.isMinimized()) mainWindow.restore();
            mainWindow.focus();
        }
    });

    // App ready
    app.whenReady().then(() => {
        createWindow();
        createTray();
        registerGlobalShortcuts();
        loadExtensions();
    });

    // All windows closed
    app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') {
            app.quit();
        }
    });

    // Activate (macOS)
    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
}

function createWindow() {
    // Initialize managers
    ThemeManager.getInstance().loadTheme('vs-dark');
    WindowManager.getInstance().initialize();

    // Create main editor window
    mainWindow = new VSCodeEditor({
        width: WindowManager.getInstance().getWidth(),
        height: WindowManager.getInstance().getHeight(),
        minWidth: 400,
        minHeight: 300,
        backgroundColor: '#1e1e1e',
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true,
            preload: path.join(__dirname, 'preload.js'),
            webSecurity: true,
            allowRunningInsecureContent: false,
        },
        titleBarStyle: process.platform === 'darwin' ? 'hiddenInset' : 'default',
        frame: true,
        show: false,
    });

    // Initialize UI components
    initializeUI();

    // Load initial content
    mainWindow.loadFile(path.join(__dirname, 'index.html'));

    // Show window when ready
    mainWindow.once('ready-to-show', () => {
        mainWindow?.show();
        mainWindow?.center();
    });

    // Handle close
    mainWindow.on('close', (event) => {
        if (tray) {
            event.preventDefault();
            mainWindow?.hide();
        }
    });

    mainWindow.on('closed', () => {
        mainWindow = null;
        sidebarManager = null;
        statusBarManager = null;
    });

    // Setup menu
    createApplicationMenu();
}

function initializeUI() {
    if (!mainWindow) return;

    // Initialize sidebar
    sidebarManager = new SidebarManager(mainWindow);
    sidebarManager.registerView('explorer', new ExplorerView());
    sidebarManager.registerView('search', new SearchView());
    sidebarManager.registerView('terminal', new TerminalView());
    sidebarManager.registerView('output', new OutputView());

    // Initialize status bar
    statusBarManager = new StatusBarManager(mainWindow);

    // Initialize command palette
    const commandPalette = new CommandPalette(mainWindow);
    commandPalette.registerCommands(getDefaultCommands());

    // Initialize key bindings
    const keyBindings = new KeyBindings(mainWindow);
    keyBindings.register(getDefaultKeyBindings());

    // IPC handlers
    setupIPCHandlers();
}

function setupIPCHandlers() {
    // Editor operations
    ipcMain.handle('editor:openFile', async (_, filePath: string) => {
        return mainWindow?.openFile(filePath);
    });

    ipcMain.handle('editor:saveFile', async (_, filePath: string, content: string) => {
        return mainWindow?.saveFile(filePath, content);
    });

    ipcMain.handle('editor:getOpenFiles', async () => {
        return mainWindow?.getOpenFiles() || [];
    });

    // Sidebar operations
    ipcMain.handle('sidebar:toggle', async (_, viewId: string) => {
        sidebarManager?.toggleView(viewId);
    });

    ipcMain.handle('sidebar:setActive', async (_, viewId: string) => {
        sidebarManager?.setActiveView(viewId);
    });

    // Status bar
    ipcMain.handle('statusBar:setItem', async (_, id: string, text: string, align?: 'left' | 'right') => {
        statusBarManager?.setItem(id, text, align);
    });

    ipcMain.handle('statusBar:removeItem', async (_, id: string) => {
        statusBarManager?.removeItem(id);
    });

    // Terminal
    ipcMain.handle('terminal:create', async (_, id: string) => {
        return mainWindow?.createTerminal(id);
    });

    ipcMain.handle('terminal:write', async (_, id: string, data: string) => {
        mainWindow?.writeTerminal(id, data);
    });

    ipcMain.handle('terminal:resize', async (_, id: string, cols: number, rows: number) => {
        mainWindow?.resizeTerminal(id, cols, rows);
    });

    // Theme
    ipcMain.handle('theme:set', async (_, themeId: string) => {
        ThemeManager.getInstance().loadTheme(themeId);
    });

    ipcMain.handle('theme:get', async () => {
        return ThemeManager.getInstance().getCurrentTheme();
    });

    // Window
    ipcMain.handle('window:minimize', async () => {
        mainWindow?.minimize();
    });

    ipcMain.handle('window:maximize', async () => {
        if (mainWindow?.isMaximized()) {
            mainWindow.unmaximize();
        } else {
            mainWindow?.maximize();
        }
    });

    ipcMain.handle('window:close', async () => {
        mainWindow?.close();
    });
}

function createApplicationMenu() {
    const template: Electron.MenuItemConstructorOptions[] = [
        {
            label: 'File',
            submenu: [
                { label: 'New File', accelerator: 'CmdOrCtrl+N', click: () => mainWindow?.newFile() },
                { label: 'Open File...', accelerator: 'CmdOrCtrl+O', click: () => mainWindow?.openFileDialog() },
                { label: 'Open Folder...', accelerator: 'CmdOrCtrl+Shift+O', click: () => mainWindow?.openFolderDialog() },
                { type: 'separator' },
                { label: 'Save', accelerator: 'CmdOrCtrl+S', click: () => mainWindow?.saveCurrentFile() },
                { label: 'Save As...', accelerator: 'CmdOrCtrl+Shift+S', click: () => mainWindow?.saveFileAs() },
                { type: 'separator' },
                { label: 'Exit', accelerator: 'Alt+F4', click: () => app.quit() },
            ],
        },
        {
            label: 'Edit',
            submenu: [
                { label: 'Undo', accelerator: 'CmdOrCtrl+Z', role: 'undo' },
                { label: 'Redo', accelerator: 'CmdOrCtrl+Y', role: 'redo' },
                { type: 'separator' },
                { label: 'Cut', accelerator: 'CmdOrCtrl+X', role: 'cut' },
                { label: 'Copy', accelerator: 'CmdOrCtrl+C', role: 'copy' },
                { label: 'Paste', accelerator: 'CmdOrCtrl+V', role: 'paste' },
                { type: 'separator' },
                { label: 'Select All', accelerator: 'CmdOrCtrl+A', role: 'selectAll' },
                { type: 'separator' },
                { label: 'Find', accelerator: 'CmdOrCtrl+F', click: () => mainWindow?.showFind() },
                { label: 'Replace', accelerator: 'CmdOrCtrl+H', click: () => mainWindow?.showReplace() },
            ],
        },
        {
            label: 'View',
            submenu: [
                { label: 'Command Palette', accelerator: 'CmdOrCtrl+Shift+P', click: () => mainWindow?.showCommandPalette() },
                { type: 'separator' },
                { label: 'Explorer', accelerator: 'CmdOrCtrl+B', click: () => sidebarManager?.toggleView('explorer') },
                { label: 'Search', accelerator: 'CmdOrCtrl+Shift+F', click: () => sidebarManager?.toggleView('search') },
                { label: 'Terminal', accelerator: 'CmdOrCtrl+`', click: () => sidebarManager?.toggleView('terminal') },
                { label: 'Output', accelerator: 'CmdOrCtrl+Shift+U', click: () => sidebarManager?.toggleView('output') },
                { type: 'separator' },
                { label: 'Toggle Full Screen', accelerator: 'F11', click: () => mainWindow?.setFullScreen(!mainWindow?.isFullScreen()) },
            ],
        },
        {
            label: 'Terminal',
            submenu: [
                { label: 'New Terminal', accelerator: 'CmdOrCtrl+Shift+`', click: () => mainWindow?.createTerminal('default') },
                { label: 'Kill Terminal', click: () => mainWindow?.killTerminal() },
                { type: 'separator' },
                { label: 'Clear Terminal', click: () => mainWindow?.clearTerminal() },
            ],
        },
        {
            label: 'Help',
            submenu: [
                { label: 'About', click: () => showAboutDialog() },
                { label: 'Documentation', click: () => require('electron').shell.openExternal('https://code.visualstudio.com/docs') },
            ],
        },
    ];

    const menu = Menu.buildFromTemplate(template);
    Menu.setApplicationMenu(menu);
}

function createTray() {
    // Create a simple 16x16 icon
    const icon = nativeImage.createEmpty();
    tray = new Tray(icon);

    const contextMenu = Menu.buildFromTemplate([
        { label: 'Show', click: () => mainWindow?.show() },
        { label: 'Hide', click: () => mainWindow?.hide() },
        { type: 'separator' },
        { label: 'Quit', click: () => { tray?.destroy(); app.quit(); } },
    ]);

    tray.setToolTip('VS Code - Electron');
    tray.setContextMenu(contextMenu);

    tray.on('click', () => {
        if (mainWindow?.isVisible()) {
            mainWindow.hide();
        } else {
            mainWindow?.show();
        }
    });
}

function registerGlobalShortcuts() {
    // Global shortcut to show/hide window
    require('electron').globalShortcut.register('CmdOrCtrl+Shift+E', () => {
        if (mainWindow?.isVisible()) {
            mainWindow.hide();
        } else {
            mainWindow?.show();
        }
    });
}

function loadExtensions() {
    const extensionHost = new ExtensionHost(mainWindow!);
    extensionHost.loadExtensions();
}

function getDefaultCommands() {
    return [
        { id: 'workbench.action.showCommands', label: 'Show All Commands', command: () => mainWindow?.showCommandPalette() },
        { id: 'workbench.action.files.newUntitledFile', label: 'New File', command: () => mainWindow?.newFile() },
        { id: 'workbench.action.files.openFile', label: 'Open File', command: () => mainWindow?.openFileDialog() },
        { id: 'workbench.action.files.save', label: 'Save', command: () => mainWindow?.saveCurrentFile() },
        { id: 'workbench.view.explorer', label: 'Show Explorer', command: () => sidebarManager?.toggleView('explorer') },
        { id: 'workbench.view.search', label: 'Show Search', command: () => sidebarManager?.toggleView('search') },
        { id: 'workbench.view.terminal', label: 'Show Terminal', command: () => sidebarManager?.toggleView('terminal') },
        { id: 'workbench.action.toggleFullScreen', label: 'Toggle Full Screen', command: () => mainWindow?.setFullScreen(!mainWindow?.isFullScreen()) },
    ];
}

function getDefaultKeyBindings() {
    return [
        { key: 'cmdOrCtrl+n', command: 'workbench.action.files.newUntitledFile' },
        { key: 'cmdOrCtrl+o', command: 'workbench.action.files.openFile' },
        { key: 'cmdOrCtrl+s', command: 'workbench.action.files.save' },
        { key: 'cmdOrCtrl+shift+p', command: 'workbench.action.showCommands' },
        { key: 'cmdOrCtrl+b', command: 'workbench.view.explorer' },
        { key: 'cmdOrCtrl+`', command: 'workbench.view.terminal' },
        { key: 'cmdOrCtrl+shift+f', command: 'workbench.view.search' },
        { key: 'f11', command: 'workbench.action.toggleFullScreen' },
    ];
}

function showAboutDialog() {
    const { dialog } = require('electron');
    dialog.showMessageBox(mainWindow!, {
        type: 'info',
        title: 'About VS Code - Electron',
        message: 'VS Code - Electron',
        detail: `Version: 1.0.0
Electron: ${process.versions.electron}
Chrome: ${process.versions.chrome}
Node.js: ${process.versions.node}

A VS Code-like editor built with Electron.`,
    });
}
