// Copyright 2024 Electron Android Project
// Search View

import { SidebarView } from '../sidebar/SidebarView';

export interface SearchResult {
    path: string;
    line: number;
    column: number;
    preview: string;
    match: string;
}

export interface SearchMatch {
    file: string;
    results: SearchResult[];
}

export class SearchView extends SidebarView {
    private query: string = '';
    private results: SearchMatch[] = [];
    private isSearching: boolean = false;
    private includePattern: string = '';
    private excludePattern: string = '';
    private caseSensitive: boolean = false;
    private wholeWord: boolean = false;
    private useRegex: boolean = false;

    constructor() {
        super({ id: 'search', title: 'Search', icon: 'search' });
    }

    setQuery(query: string) {
        this.query = query;
    }

    setOptions(options: {
        include?: string;
        exclude?: string;
        caseSensitive?: boolean;
        wholeWord?: boolean;
        useRegex?: boolean;
    }) {
        if (options.include !== undefined) this.includePattern = options.include;
        if (options.exclude !== undefined) this.excludePattern = options.exclude;
        if (options.caseSensitive !== undefined) this.caseSensitive = options.caseSensitive;
        if (options.wholeWord !== undefined) this.wholeWord = options.wholeWord;
        if (options.useRegex !== undefined) this.useRegex = options.useRegex;
    }

    handleAction(action: string, data?: any): void {
        switch (action) {
            case 'search':
                this.performSearch(data?.query);
                break;
            case 'openResult':
                this.openResult(data?.result);
                break;
            case 'clear':
                this.clearResults();
                break;
            case 'collapseAll':
                this.collapseAll();
                break;
        }
    }

    async performSearch(query?: string) {
        if (query) this.query = query;
        if (!this.query) return;

        this.isSearching = true;
        this.updateView();

        // TODO: Implement actual file search using fs
        // For now, simulate search results
        this.results = [];
        this.isSearching = false;
        this.updateView();
    }

    private openResult(result: SearchResult) {
        // Open file at the search result location
        // This would send IPC message to main window
    }

    private clearResults() {
        this.results = [];
        this.updateView();
    }

    private collapseAll() {
        // Collapse all search result groups
        this.updateView();
    }

    private updateView() {
        if (this.manager) {
            // Send updated content to renderer
        }
    }

    getContent(): any {
        return {
            query: this.query,
            results: this.results,
            isSearching: this.isSearching,
            options: {
                include: this.includePattern,
                exclude: this.excludePattern,
                caseSensitive: this.caseSensitive,
                wholeWord: this.wholeWord,
                useRegex: this.useRegex,
            },
        };
    }
}
