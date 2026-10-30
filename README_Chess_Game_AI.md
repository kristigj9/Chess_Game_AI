# Chess Game AI

**A Java chess application featuring a playable 2D board, an immersive JavaFX 3D interface, and an AI opponent powered by Minimax with alpha-beta pruning.**

[![Java](https://img.shields.io/badge/Java-Desktop-orange)](https://www.java.com/)
[![JavaFX](https://img.shields.io/badge/JavaFX-3D_UI-blue)](https://openjfx.io/)
![AI](https://img.shields.io/badge/AI-Minimax%20%2B%20Alpha--Beta-green)

## Overview

Chess Game AI is a desktop chess project built in Java. It separates the chess rules and board representation from its graphical interfaces, offering both a classic Swing-based 2D experience and a JavaFX-based 3D scene. Players can challenge a computer opponent that evaluates candidate moves using a depth-limited Minimax search with alpha-beta pruning.

## Features

- **Two interfaces:** a traditional 2D chessboard built with Java Swing and a 3D experience built with JavaFX.
- **AI opponent:** Minimax search enhanced with alpha-beta pruning and a board evaluation component.
- **Selectable AI search depth in 3D:** depth 2 or depth 3.
- **Chess move validation:** piece-specific movement and legal-move checking.
- **Game-state detection:** check, checkmate, and stalemate handling.
- **2D game controls:** restart and undo, plus move history and captured-piece information.
- **3D interaction:** perspective camera controls, scene navigation, and animated piece movement.
- **3D assets:** OBJ/MTL models and image resources included in the repository.

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java |
| 2D UI | Java Swing / AWT |
| 3D UI | JavaFX |
| AI | Minimax, alpha-beta pruning |
| Design | Object-oriented programming, separate model/logic/UI packages |
| IDE | IntelliJ IDEA |

## Project Structure

```text
Chess_Game_AI/
├── src/
│   ├── app/
│   │   └── Main.java                 # Swing application entry point
│   ├── gui/
│   │   ├── BoardPanel.java           # Interactive 2D board
│   │   ├── GameFrame.java            # Main 2D window
│   │   └── SidePanel.java            # Move history and captures
│   ├── gui3d/
│   │   ├── ChessGame3D.java          # JavaFX application entry point
│   │   ├── Game3DView.java           # 3D scene and interactions
│   │   └── Piece3DFactory.java       # 3D chess-piece creation
│   ├── logic/
│   │   ├── AIPlayer.java
│   │   ├── MinimaxAI.java
│   │   ├── BoardEvaluator.java
│   │   ├── MoveValidator.java
│   │   ├── CheckDetector.java
│   │   ├── GameStateChecker.java
│   │   └── ...                       # Move records and snapshots
│   ├── model/
│   │   ├── Board.java
│   │   ├── Position.java
│   │   ├── PlayerColor.java
│   │   └── pieces/                   # King, Queen, Rook, Bishop, Knight, Pawn
│   └── resources/                    # Images, OBJ/MTL models, textures
└── .gitignore
```

## Getting Started

### Prerequisites

- A Java Development Kit compatible with your JavaFX installation (Java 21 is a suitable starting point for the included JavaFX 21 SDK configuration).
- IntelliJ IDEA or another Java IDE.
- **For the 3D interface:** JavaFX SDK, including the `javafx.controls` and `javafx.graphics` modules.

> The repository contains an IntelliJ library configuration referencing a local Windows JavaFX SDK. You may need to update the SDK path for your computer. There is no Maven or Gradle build file in the provided project archive.

### Clone

```bash
git clone https://github.com/kristigj9/Chess_Game_AI.git
cd Chess_Game_AI
```

### Run the 2D version

1. Open the project in IntelliJ IDEA.
2. Configure the project JDK and mark `src` as a Sources Root if needed.
3. Run **`app.Main`**.

### Run the 3D version

1. Install and configure a compatible JavaFX SDK in IntelliJ IDEA.
2. Add JavaFX to the project's module/classpath configuration.
3. If JavaFX is not configured through the IDE, use VM options similar to:

```text
--module-path "PATH_TO_JAVAFX_SDK/lib" --add-modules javafx.controls,javafx.graphics
```

4. Run **`gui3d.ChessGame3D`**.

Ensure the project's image and model resources are accessible at runtime. Depending on your IDE configuration, resource paths may need adjustment.

## How the AI Works

The AI evaluates legal candidate moves using a **depth-limited Minimax search**. It models the opponent's responses and uses **alpha-beta pruning** to avoid exploring branches that cannot improve the current best result. The `BoardEvaluator` class provides positional scoring, while `MoveValidator`, `CheckDetector`, and `GameStateChecker` support legal moves and terminal game states.

In the 3D interface, the user can select search depth **2** or **3**. Higher search depth generally considers more future moves but may require more computation.

## Screenshots

Screenshots can be added here after capturing the running application:

```markdown
![2D chess interface](docs/screenshots/chess-2d.png)
![3D chess interface](docs/screenshots/chess-3d.png)
```

## Future Improvements

- Add automated unit tests for chess rules and AI decisions.
- Package the application with a reproducible Maven or Gradle build.
- Expand AI difficulty settings and optimize search performance.
- Add a documented executable release for easier installation.

## Author

**Kristi Gjosasi**  
GitHub: [@kristigj9](https://github.com/kristigj9)

---

*Built as a Java software development project exploring chess rules, object-oriented design, desktop interfaces, 3D graphics, and game-tree search.*
