
declare class NameFormatter {
    name: string;
}

declare class NameInput {
    currentName: string;
    onNameAdded: Function;
    onAddName(): void;
}

declare class NameList {
    names: string[];
}

declare class NamesApp {
    names: Array<string>;
    showNames: boolean;
    constructor();
    onNameAdded(name: any): void;
}
