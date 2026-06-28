let currentBoard = "000000000";
let gameOver = false;
let isFirstMove = true; // Track if we need to show the raw board yet

const boardElement = document.getElementById('board');
const rawBoardElement = document.getElementById('raw-board');
const statusElement = document.getElementById('status');
const serverUrl = 'http://localhost:8080/move';

function initGrid() {
    boardElement.innerHTML = '';
    for (let i = 0; i < 9; i++) {
        const cell = document.createElement('div');
        cell.className = 'cell';
        cell.onclick = () => makeMove(i);
        boardElement.appendChild(cell);
    }
    renderBoard();
}

function renderBoard() {
    // Update raw text only if a move has been made
    if (isFirstMove) {
        rawBoardElement.innerText = "";
    } else {
        rawBoardElement.innerText = '"board": "' + currentBoard + '"';
    }

    for (let i = 0; i < 9; i++) {
        const val = currentBoard.charAt(i);
        const cellElement = boardElement.children[i];
        
        // Display O for Player (1), X for Computer (2)
        cellElement.innerText = val === '1' ? 'O' : val === '2' ? 'X' : '';
        cellElement.className = 'cell'; 
        
        if (val !== '0') {
            cellElement.classList.add('taken');
            cellElement.classList.add(val === '1' ? 'player' : 'computer');
        }
    }
    checkWinnerLocal();
}

function checkWinnerLocal() {
    const lines = [
        [0, 1, 2], [3, 4, 5], [6, 7, 8], // rows
        [0, 3, 6], [1, 4, 7], [2, 5, 8], // cols
        [0, 4, 8], [2, 4, 6]             // diagonals
    ];
    
    for (let line of lines) {
        const [a, b, c] = line;
        if (currentBoard[a] !== '0' && currentBoard[a] === currentBoard[b] && currentBoard[a] === currentBoard[c]) {
            gameOver = true;
            statusElement.innerText = currentBoard[a] === '1' ? "🎉 You Win!" : "💻 Computer Wins!";
            return;
        }
    }
    
    if (!currentBoard.includes('0')) {
        gameOver = true;
        statusElement.innerText = "🤝 It's a draw!";
    }
}

async function makeMove(cellIndex) {
    if (gameOver || currentBoard.charAt(cellIndex) !== '0') return;

    isFirstMove = false; 

    const originalBoardForServer = currentBoard; // SAVE THIS BEFORE CHANGING

    // Optimistically update the UI locally
    let newBoardArr = currentBoard.split('');
    newBoardArr[cellIndex] = '1';
    currentBoard = newBoardArr.join('');
    renderBoard();

    if (gameOver) return;

    statusElement.innerText = "Computer is thinking...";

    try {
        const response = await fetch(serverUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            // SEND THE ORIGINAL UNMODIFIED BOARD
            body: JSON.stringify({ board: originalBoardForServer, cell: cellIndex + 1 }) 
        });

        if (response.ok) {
            const data = await response.json();
            currentBoard = data.board;
            statusElement.innerText = "Your turn! (Select a cell)";
            renderBoard();
        } else {
            const errorData = await response.json();
            alert("Server rejected move: " + (errorData.error || response.statusText));
            resetGame();
        }
    } catch (error) {
        alert("Failed to connect to the Java HTTP Server. Ensure it is running on Port 8080.");
        statusElement.innerText = "Connection Error.";
    }
}

function resetGame() {
    currentBoard = "000000000";
    gameOver = false;
    isFirstMove = true; // Reset text hide flag
    statusElement.innerText = "Your turn! (Select a cell)";
    renderBoard();
}

initGrid();