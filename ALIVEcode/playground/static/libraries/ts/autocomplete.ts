//#region Types
/**
 * represent all the valid command names
 */
type commandNames =
    "showSettingsMenu" | "goToNextError" | "goToPreviousError" | "selectall" | "centerselection" | "gotoline" | "fold" | "unfold" | "toggleFoldWidget"
    | "toggleParentFoldWidget" | "foldall" | "foldAllComments" | "foldOther" | "unfoldall" | "findnext" | "findprevious" | "selectOrFindNext"
    | "selectOrFindPrevious" | "find" | "overwrite" | "selecttostart" | "gotostart" | "selectup" | "golineup" | "selecttoend" | "gotoend"
    | "selectdown" | "golinedown" | "selectwordleft" | "gotowordleft" | "selecttolinestart" | "gotolinestart" | "selectleft" | "gotoleft" | "selectwordright"
    | "gotowordright" | "selecttolineend" | "gotolineend" | "selectright" | "gotoright" | "selectpagedown" | "pagedown" | "gotopagedown"
    | "selectpageup" | "pageup" | "gotopageup" | "scrollup" | "scrolldown" | "selectlinestart" | "selectlineend" | "togglerecording"
    | "replaymacro" | "jumptomatching" | "selecttomatching" | "expandToMatching" | "passKeysToBrowser" | "copy" | "cut" | "paste" | "removeline"
    | "duplicateSelection" | "sortlines" | "togglecomment" | "toggleBlockComment" | "modifyNumberUp" | "modifyNumberDown" | "replace" | "undo" | "redo"
    | "copylinesup" | "movelinesup" | "copylinesdown" | "movelinesdown" | "del" | "backspace" | "cut_or_delete" | "removetolinestart" | "removetolineend"
    | "removetolinestarthard" | "removetolineendhard" | "removewordleft" | "removewordright" | "outdent" | "indent" | "blockoutdent" | "blockindent"
    | "insertstring" | "inserttext" | "splitline" | "transposeletters" | "touppercase" | "tolowercase" | "autoindent" | "expandtoline" | "joinlines"
    | "invertSelection" | "addLineAfter" | "addLineBefore" | "openCommandPallete" | "modeSelect" | "foldToLevel1" | "foldToLevel2" | "foldToLevel3"
    | "foldToLevel4" | "foldToLevel5" | "foldToLevel6" | "foldToLevel7" | "foldToLevel8" | "addCursorAbove" | "addCursorBelow" | "addCursorAboveSkipCurrent"
    | "addCursorBelowSkipCurrent" | "selectMoreBefore" | "selectMoreAfter" | "selectNextBefore" | "selectNextAfter" | "toggleSplitSelectionIntoLines"
    | "splitSelectionIntoLines" | "alignCursors" | "findAll" | "startAutocomplete" | "expandSnippet"

/**
 * represent the possible arguments that a command can take
 */
type command_args = number /* line */ | string /* str */ | { times: number, text?: string } /* args */

/**
 * represent a valid command
 */
type command = {
    command: commandNames
    args?: command_args
}


type SymbolPair = {
    open: string
    close: string
}

/**
 * represent the position of the cursor
 */
type Position = { row: number, column: number }

//#endregion types


type settingsType = {
    onMultiLineComment: "complete" | "format"
    onDocComment: "complete" | "format"
    completeSymbol: "smart" | "always" | "never"
    completeBloc: "smart" | "always" | "never"

    symbolPairs: [
        { open: '"', close: '"' },
        { open: "'", close: "'" },
        { open: '(', close: ')' },
        { open: '{', close: '}' },
        { open: '[', close: ']' },
    ]

    blocPairs: [
        // si
        { open: "si", close: "fin si" },

        // fonctions
        { open: "fonction", close: "fin fonction" },

        //getter
        { open: "get", close: "fin get" },

        //setter
        { open: "set", close: "fin set" },

        // structures
        { open: "structure", close: "fin structure" },

        // boucles
        { open: "faire", close: "tant que" },
        { open: "tant que", close: "fin tant que" },
        { open: "pour", close: "fin pour" },
        { open: "repeter", close: "fin repeter" },
    ]
}


let editor: any;

const checkComment = ":"
const patterns: {
    commentaires: SymbolPair[]
    symbolPairs: SymbolPair[]
    blocks: SymbolPair[]
} = {

    commentaires: [
        { open: "(:", close: ":)" },
        { open: "(-:", close: ":-)" },
    ],

    /**
     *? blocks: {
     **   name: [start, end]
     *? }
     */
    symbolPairs: [
        { open: '"', close: '"' },
        { open: "'", close: "'" },
        { open: '(', close: ')' },
        { open: '{', close: '}' },
        { open: '[', close: ']' },
    ],

    blocks: [
        // si
        { open: "si", close: "fin si" },

        // fonctions
        { open: "fonction", close: "fin fonction" },

        //getter
        { open: "get", close: "fin get" },

        //setter
        { open: "set", close: "fin set" },

        // structures
        { open: "structure", close: "fin structure" },

        // boucles
        { open: "faire", close: "tant que" },
        { open: "tant que", close: "fin tant que" },
        { open: "pour", close: "fin pour" },
        { open: "repeter", close: "fin repeter" },
    ]
}



function closeSymbolPair(symbol: SymbolPair, keyString: string, goBackTimes: number = 1): command {
    const pos = getPos();
    const line = getLine(pos.row);

    if (line[pos.column] === symbol.close) {
        return { command: "gotoright", args: { times: 1 } }
    }
    // doesn't add the pair if the symbole is placed before a word or an opening symbol
    else if (
        // does nothing if the symbol is added before or after a letter or an opening symbol
        line[pos.column]?.match(/\p{L}+/u)
        || line[pos.column]?.match(`|${patterns.symbolPairs.map(pair => "\\" + pair.open).join("|")}`)
        || (line[pos.column - 1]?.match(/\p{L}+/u) && symbol.close === symbol.open)

        // does nothing if the symbole typed is the closing symbol and the closing symbol is not the same as the opening symbol
        || (keyString === symbol.close && symbol.close !== symbol.open)

        // return before adding the pair if the symbol is the closing symbol
        || (symbol.close === symbol.open && line[pos.column - 1] === symbol.open)
    ) {
        return;
    }

    execCommands({ command: "insertstring", args: `${symbol.open}${symbol.close}` });
    return { command: "gotoleft", args: { times: goBackTimes } };
}



function closeBlock(): command {
    const pos = getPos();
    const currentLine = getLine(pos.row);
    if (!currentLine) return;

    const blockPairs = Object.values(patterns.blocks);

    const blockPair = blockPairs.find(blockPair => lineStartWith(currentLine, blockPair.open));
    if (!blockPair) return;

    const tabSize = editor.session.$tabSize;

    const spaces = indentation(currentLine);

    //* check if the block has already been closed
    for (const line of getLines(pos.row + 1) || []) {
        // the line checked must have the same indentation to be a potential closing statement
        if (line.length - line.trimLeft().length === spaces) {

            // if a new block of the same type is open before the current block is close, the current block must be closed
            if (lineStartWith(line, blockPair.open)) {
                break;
            }
            // if a closing statement of the good type is found, there is no need to add another one
            else if (lineStartWith(line, blockPair.close)) {
                return { command: "insertstring", args: "\n" + " ".repeat(spaces + tabSize) }
            }
        }
    }

    execCommands(
        { command: "insertstring", args: `\n\n${" ".repeat(spaces)}${blockPair.close}` },
        { command: "golineup", args: { times: 1 } }
    )
    return { command: "insertstring", args: " ".repeat(spaces + tabSize) }
}



function autocomplete(data: any, hashId: number, keyString: string, keyCode: number, e: unknown): command {
    const pos = getPos();
    const line = getLine(pos.row);
    if (hashId == -1) {
        const symbolPair: SymbolPair = Object.values(patterns.symbolPairs).find(symbol => symbol.open === keyString || symbol.close === keyString);
        if (symbolPair != undefined) {
            return closeSymbolPair(symbolPair, keyString);
        }
        // les commentaires fermant automatique
        else if (keyString === checkComment) {
            const [multiligne, documentation] = patterns.commentaires;
            const lastSymbol = ")"
            if (line.substring(pos.column - (multiligne.open.length - 1), pos.column) + ":" === multiligne.open) {
                let closing = multiligne.close;
                // if the line already has the last symbol of the comment at the end, don't add it
                if (line[pos.column] === lastSymbol) closing = closing.substring(0, closing.length - 1)

                execCommands(
                    { command: "insertstring", args: `:\n ${" ".repeat(indentation(line))}\n${closing}` },
                    { command: "golineup", args: { times: 1 } }
                )
                return { command: "gotolineend" }
            }
            else if (line.substring(pos.column - (documentation.open.length - 1), pos.column) + ":" === documentation.open) {
                let closing = documentation.close;
                // if the line already has the last symbol of the comment at the end, don't add it
                if (line[pos.column] === lastSymbol) closing = closing.substring(0, closing.length - 1)

                execCommands(
                    { command: "insertstring", args: `:\n ${" ".repeat(indentation(line))}- \n${closing}` },
                    { command: "golineup", args: { times: 1 } }
                )
                return { command: "gotolineend" }
            }
        }

        // if the \n is pressed at the end of the line
        if (keyString === "\n" && line.trim() && pos.column >= line.trimRight().length) {
            if (lineStartWith(line, "-")) {
                return { command: "insertstring", args: `\n${" ".repeat(indentation(line))}- ` }
            }
            return closeBlock();
        }
    }
}


//#region utils

function execCommands(...commands: command[]) {
    for (const command of commands) {
        editor.commands.exec(command.command, editor, command.args);
    }
}

function insertIndentedString(line:string, text: string): command {
    return { command: "insertstring", args: `${" ".repeat(indentation(line))}${text}` }
}

/**
 * @returns the current position of the cursor as an object of form  
 * `{ row: number, column: number }`
 */
function getPos(): Position {
    return editor.getCursorPosition();
}

/**
 * get the line at the specified row
 * @param row the index of the line
 * @returns the line at the specified row or `undefined` if there is none
 */
function getLine(row: number): string {
    return editor.session.getLine(row);
}

/**
 * get the lines from one row to another row
 * @param start the first row included in the array
 * @param end the last row included in the array (default is the last row of the document)
 * @returns the array of lines between the start row and the end row
 */
function getLines(start: number, end: number = null): string[] {
    return editor.session.getLines(start, end ?? editor.session.getLength());
}


/**
 * check if the line startswith a specify string and ignores the indentation at the start of the line
 * @param line the line to be tested
 * @param start the string 
 * @returns if the line starts with start
 */
function lineStartWith(line: string, start: string): boolean {
    return line.trimLeft().startsWith(start);
}

/**
 * 
 * @param line 
 * @returns the number of spaces that make the indentation of the line
 */
function indentation(line: string): number {
    const tabSize = editor.session.$tabSize;
    return line.substring(0, line.indexOf(line.trimLeft()[0])).replace("\t", " ".repeat(tabSize)).length;
}

//#endregion