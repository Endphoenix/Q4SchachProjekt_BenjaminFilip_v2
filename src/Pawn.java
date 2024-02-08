import javax.swing.*;

// Spielfigur: Pawn / Bauer
public class Pawn extends Piece {

    public Pawn(boolean isWhite, boolean killed, Tile position) {
        super(isWhite, killed, position);
    }

    @Override
    public void calculateNewPos() {
        if (!isMoved()) {
            // Wenn der Bauer noch nicht bewegt wurde, hat er die Option, um zwei Felder zu ziehen
            int direction = isWhite() ? 1 : -1;  // Richtung hängt von der Farbe des Bauern ab
            Tile currentTile = getPosition();
            int newX = currentTile.getX();
            int newY = currentTile.getY() + (2 * direction);
            Tile newTile = Board.tiles[newX][newY];
            int newY1 = currentTile.getY() + direction;
            Tile newTile1 = Board.tiles[newX][newY1];

            // Überprüfen, ob die neuen Positionen innerhalb des Spielbretts liegen
            if (isValidMove(newX, newY) && isValidMove(newX, newY1)) {
                JButton newButton = createFieldButton(newTile);
                Board.tiles[newX][newY].getpTile().add(newButton);
                Board.tiles[newX][newY].getpTile().updateUI();
            }
            if (isValidMove(newX, newY1)) {
                JButton newButton1 = createFieldButton(newTile1);
                Board.tiles[newX][newY1].getpTile().add(newButton1);
                Board.tiles[newX][newY].getpTile().updateUI();
                tryKill(newX, newY1);
            }
        } else {
            // Falls der Bauer bereits bewegt wurde, kann er nur noch ein Feld ziehen
            int direction = isWhite() ? 1 : -1;  // Richtung hängt von der Farbe des Bauern ab
            Tile currentTile = getPosition();
            int newX = currentTile.getX();
            int newY = currentTile.getY() + direction;
            Tile newTile = Board.tiles[newX][newY];

            // Überprüfen, ob die neue Position innerhalb des Spielbretts liegt
            if (isValidMove(newX, newY)) {
                JButton newButton = createFieldButton(newTile);
                Board.tiles[newX][newY].getpTile().add(newButton);
                Board.tiles[newX][newY].getpTile().updateUI();
                tryKill(newX,newY);
            } else {
                tryKill(newX, newY);
            }
        }
    }

    /* Methode zum Überprüfen, ob die gewünschte Position innerhalb des Spielbretts liegt, ob das neue Tile bereits
     besetzt ist.
     */
    private boolean isValidMove(int x, int y) {
        if (Board.tiles[x][y].getOccupyingPiece() != null) {
            return false;
        } else {
            return x < 8 && y < 8;
        }
    }

    private boolean canKill(int x, int y) {
        if (Board.tiles[x][y].getOccupyingPiece() != null){
            if ((Board.tiles[x][y].getOccupyingPiece().isWhite() && isWhite())
                    || (!Board.tiles[x][y].getOccupyingPiece().isWhite() && !isWhite())) {
                return false;
            } else {
                return x < 8 && y < 8;
            }
        } else {
            return false;
        }
    }

    public void tryKill(int x, int y) {

        for (int i = -1; i <= 1; i += 2) {
            int newX = x + i;

            if (newX >= 0 && newX < 8 && canKill(newX, y)) {
                JButton newButton = createFieldButton(Board.tiles[newX][y]);
                newButton.setSelected(true);
                newButton.setIcon(new ImageIcon("src/pics/KillTarget.png"));
                Board.tiles[newX][y].getpTile().add(newButton);
                Board.tiles[newX][y].getpTile().updateUI();
            }
        }
    }



    // Methode zum Umwandeln eines Bauern
    public void promote() {
        boolean color = !isWhite();
            // Der Bauer erreicht die gegnerische Grundreihe (y = 0 für Weiß, y = 7 für Schwarz)
            // Füge hier den Code für die Bauernumwandlung ein
            SwingUtilities.invokeLater(() -> {
                String[] options = {"Queen", "Rook", "Bishop", "Knight"};
                int choice = JOptionPane.showOptionDialog(null, "Choose a piece to promote to:", "Promotion",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                Piece promotedPiece = switch (choice) {
                    case 0 -> new Queen(color, false, getPosition());
                    case 1 -> new Rook(color, false, getPosition());
                    case 2 -> new Bishop(color, false, getPosition());
                    case 3 -> new Knight(color, false, getPosition());
                    default ->
                        // Default to Queen if no valid choice is made
                            new Queen(color, false, getPosition());
                };

                // Ersetze den Bauern durch die gewählte Figur
                Board.getTiles()[getPosition().getX()][getPosition().getY()].setOccupyingPiece(promotedPiece);
                JButton button = promotedPiece.createPieceButton();
                getPosition().getpTile().remove(0);
                getPosition().getpTile().add(button);
                getPosition().getpTile().updateUI();
            });

    }



    @Override
    protected ImageIcon getIconPath () {
        return new ImageIcon(isWhite() ? "src/pics/PawnBlack.png" : "src/pics/PawnWhite.png");
    }
}
