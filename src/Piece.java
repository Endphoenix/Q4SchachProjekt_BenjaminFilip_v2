import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public abstract class Piece {
    private boolean white;
    private boolean killed;
    public Tile position;
    private boolean moved = false;
    public static Piece[] tempPieces;
    public Piece(boolean white, boolean killed, Tile position) {
        this.white = white;
        this.killed = killed;
        this.position = position;
        this.tempPieces = new Piece[0];  // Füge diese Zeile hinzu, um das Array zu initialisieren.
    }

    public Tile getPosition() {
        return this.position;
    }

    public void setPosition(Tile position) {
        this.position = position;
    }

    public boolean isWhite() {
        return this.white;
    }

    public abstract void calculateNewPos();


    // Move method
    public void move(int newX, int newY){
        Board.txtA.append(Board.vCounter + ". " + (isWhite() ? "Weiß, " : "Schwarz, ") + getClassName() + ": "
                + (char)(97+position.getX()) + (8-position.getY()) + " -> " + (char)(97+newX) + (8-newY) + "\n");
        Board.vCounter++;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++){
                if (Board.tiles[i][j].getOccupyingPiece() instanceof Pawn){
                    Pawn pawn = (Pawn) Board.tiles[i][j].getOccupyingPiece();
                    if (pawn.isWhite() == Board.tiles[position.getX()][position.getY()].getOccupyingPiece().isWhite()){
                        pawn.setEnPassant(false);
                    }
                }
            }
        }

        if (!Board.tiles[newX][newY].getButton().isDefaultCapable() &&
                Board.tiles[position.getX()][position.getY()].getOccupyingPiece() instanceof Pawn) {
            Pawn pawn = (Pawn) Board.tiles[position.getX()][position.getY()].getOccupyingPiece();
            pawn.setEnPassant(true);
        } else if (Board.tiles[newX][newY].getButton().isDefaultCapable() &&
                Board.tiles[position.getX()][position.getY()].getOccupyingPiece() instanceof Pawn) {
            Pawn pawn = (Pawn) Board.tiles[position.getX()][position.getY()].getOccupyingPiece();
            pawn.setEnPassant(false);
        }

        Board.tiles[position.getX()][position.getY()].getpTile().remove(0);
        Board.tiles[position.getX()][position.getY()].getpTile().updateUI();
        Tile newTile = Board.tiles[newX][newY];
        getPosition().setOccupied(false);
        if (Board.tiles[newTile.getX()][newTile.getY()].getOccupyingPiece() != null &&
                Board.tiles[newTile.getX()][newTile.getY()].getOccupyingPiece().getClass().equals(King.class)){
            if (Board.tiles[newTile.getX()][newTile.getY()].getOccupyingPiece().isWhite()){
                Board.setStatus(GameStatus.BLACKWIN);
            } else {
                Board.setStatus(GameStatus.WHITEWIN);
            }
            Board.lStatus.setText(Board.status.toString());
        }
        newTile.setOccupied(true);
        newTile.setOccupyingPiece(this);
        JButton pwButton = createPieceButton();
        Board.tiles[newTile.getX()][newTile.getY()].getpTile().remove(0);
        Board.tiles[newTile.getX()][newTile.getY()].getpTile().add(pwButton);
        position.setOccupied(false);
        position.setOccupyingPiece(null);
        setPosition(newTile);


        // Überprüfe, ob der Bauer die gegnerische Grundreihe erreicht hat
        checkPromotion();

        moved = true;
    }

    public static void pullRook(Tile destTile){
        if (7 - destTile.getX() < 3) {
            Rook rook = (Rook) Board.tiles[7][destTile.getY()].getOccupyingPiece();
            rook.move(destTile.getX(), destTile.getY());
        } else if (0 < destTile.getX()) {
            Rook rook = (Rook) Board.tiles[0][destTile.getY()].getOccupyingPiece();
            rook.move(destTile.getX(), destTile.getY());
        }
    }

    private static void checkCastleR(Piece piece) {
        if (piece instanceof King) {
            King king = (King) piece;
            // Find the rook position for castling
            Tile rookTile = Board.tiles[7][piece.getPosition().getY()];
            Tile destTile = Board.tiles[5][piece.getPosition().getY()];
            king.castle(rookTile);
        }
    }
    private static void checkCastleL(Piece piece) {
        if (piece instanceof King) {
            King king = (King) piece;
            //Find the rook position for castling
            Tile rookTile = Board.tiles[0][piece.getPosition().getY()];
            Tile destTile = Board.tiles[3][piece.getPosition().getY()];
            king.castle(rookTile);

        }
    }
    // Methode zur Überprüfung, ob der Bauer die gegnerische Grundreihe erreicht hat
    private void checkPromotion() {
        if (this instanceof Pawn) {
            int yPosition = getPosition().getY();
            if ((yPosition == 0 && isWhite()) || (yPosition == 7 && !isWhite())) {
                // Der Bauer hat die gegnerische Grundreihe erreicht
                ((Pawn) this).promote();
            }
        }
    }
    // Button creation method
    public JButton createPieceButton() {
        JButton button = new JButton();
        button.addActionListener(new PieceActionListener(this));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setIcon(getIconPath());
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setToolTipText(getClassName());
        return button;
    }

    public JButton createFieldButton(Tile position) {
        JButton button = new JButton();
        button.addActionListener(new FieldActionListener(position, this));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setIcon(new ImageIcon("src/pics/Target.png"));
        button.setSelected(true);
        button.setToolTipText("Bewege die Figur hierher.");
        return button;
    }

    public JButton createKillButton(Tile position) {
        JButton button = new JButton();
        button.addActionListener(new FieldActionListener(position, this));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setIcon(new ImageIcon("src/pics/KillTarget.png"));
        button.setSelected(true);
        button.setToolTipText("Schlage diese Figur.");
        return button;
    }

    public JButton createCastleButton(Tile position) {
        JButton button = new JButton();
        button.addActionListener(new FieldActionListener(position, this));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setIcon(new ImageIcon("src/pics/Castling.png"));
        button.setSelected(true);
        button.setToolTipText("Rochade ausführen.");
        return button;
    }

    public JButton createEnPassantButton(Tile position) {
        JButton button = new JButton();
        button.addActionListener(new FieldActionListener(position, this));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setSelected(true);
        button.setToolTipText("EnPassant ausführen.");
        button.setFocusPainted(false);
        return button;
    }

    // Abstract method to get the icon path
    protected abstract ImageIcon getIconPath();
    protected abstract ImageIcon getKillIconPath(boolean white);

    // ActionListener for field clicks
    public static class PieceActionListener implements ActionListener {
        private final Piece piece;

        public PieceActionListener(Piece piece) {
            this.piece = piece;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            String clickSfx = "src/sfx/click.wav";
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File(clickSfx));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.setFramePosition(0);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                throw new RuntimeException(ex);
            }
            resetTempPieces(piece.getPosition());
            removeFieldButtons();
            if (piece instanceof Pawn) {
                Pawn pawn = (Pawn) piece;
                pawn.checkEnPassant(piece.getPosition().getX(), piece.getPosition().getY());
            }
            piece.calculateNewPos();
            // Check for castling move
            checkCastleR(piece);
            checkCastleL(piece);
        }
    }
    public static class FieldActionListener implements ActionListener {
        private final Tile newTile;
        private final Piece piece;

        public FieldActionListener(Tile newTile, Piece piece) {
            this.newTile = newTile;
            this.piece = piece;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String moveSfx = "src/sfx/move.wav";
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File(moveSfx));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.setFramePosition(0);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                throw new RuntimeException(ex);
            }

            if (piece.getPosition().isOccupied()) {
                if (piece instanceof King && ((King) piece).isCastled() && !piece.isMoved()) {
                    if (newTile.getX() > piece.getPosition().getX()) {
                        Tile destTile = Board.tiles[newTile.getX() - 1][newTile.getY()];
                        pullRook(destTile);
                    } else if (newTile.getX() < piece.getPosition().getX()) {
                        Tile destTile = Board.tiles[newTile.getX() + 1][newTile.getY()];
                        pullRook(destTile);
                    }

                }

                if (!newTile.getButton().isFocusPainted()) {
                    int y = newTile.getY() + (piece.isWhite() ? 1 : -1);
                    Board.tiles[newTile.getX()][y].getpTile().remove(0);
                    Board.tiles[newTile.getX()][y].setOccupyingPiece(null);
                    Board.tiles[newTile.getX()][y].getpTile().updateUI();
                }

                piece.move(newTile.getX(), newTile.getY());
                resetTempPieces(piece.getPosition());
                removeFieldButtons();

            }
            if (Board.status.equals(GameStatus.WHITEMOVE)) {
                Board.changeButtonsEnabled(true);
                Board.setStatus(GameStatus.BLACKMOVE);
            } else if (Board.status.equals(GameStatus.BLACKMOVE)) {
                Board.changeButtonsEnabled(false);
                Board.setStatus(GameStatus.WHITEMOVE);
            } else if (Board.status.equals(GameStatus.WHITEWIN)) {
                Board.disableAllButtons();
                NotifySound();
                JOptionPane.showMessageDialog(null, "Weiß hat das Spiel gewonnen!", "Spielausgang",
                        JOptionPane.INFORMATION_MESSAGE, new ImageIcon("src/pics/WhiteWin.png"));
            } else if (Board.status.equals(GameStatus.BLACKWIN)) {
                Board.disableAllButtons();
                NotifySound();
                JOptionPane.showMessageDialog(null, "Schwarz hat das Spiel gewonnen!", "Spielausgang",
                        JOptionPane.INFORMATION_MESSAGE, new ImageIcon("src/pics/BlackWin.png"));
            }
            Board.lStatus.setText(Board.status.toString());
        }

        public static void NotifySound() {
            String notifySfx = "src/sfx/notify.wav";
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File(notifySfx));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.setFramePosition(0);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void removeFieldButtons(){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++){
                for (int k = 0; k < Board.tiles[i][j].getpTile().getComponents().length; k++) {
                    Component c = Board.tiles[i][j].getpTile().getComponent(k);
                    if (c instanceof JButton){
                        if(((JButton) c).isSelected()){
                            Board.tiles[i][j].getpTile().remove(k);
                            Board.tiles[i][j].getpTile().updateUI();

                        }

                    }
                }
            }
        }
    }
    public static void resetTempPieces(Tile destination) {
        for (Piece tempPiece : tempPieces) {
            if (tempPiece != null) {

                Tile originalTile = tempPiece.getPosition();

                if (!(originalTile.getpTile() == destination.getpTile())) {

                    originalTile.setOccupyingPiece(tempPiece);

                    JButton button = tempPiece.createPieceButton();
                    originalTile.getpTile().remove(0);
                    originalTile.getpTile().add(button);
                    originalTile.getpTile().updateUI();
                }
            }
        }

        // Nach dem Zurücksetzen leere das tempPieces-Array
        tempPieces = new Piece[0];
    }

    public abstract String getClassName(); // Für ToolTips

    public boolean isMoved() {
        return moved;
    }

    protected static boolean isValidMove(int x, int y) {
        if (x < 8 && x > -1 && y < 8 && y > -1) {
            if (Board.tiles[x][y].getOccupyingPiece() != null) {
                return false;
            } else return true;
        } else return false;
    }
}
