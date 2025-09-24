package me.samsuik.sakura.tps.graph;

import me.samsuik.sakura.tps.ServerTickInformation;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public final class DetailedTPSGraph extends TPSGraph {
    public DetailedTPSGraph(
        final int width,
        final int height,
        final double scale,
        final List<ServerTickInformation> tickInformation
    ) {
        super(width, height, scale, tickInformation);
    }

    @Override
    public BuiltComponentCanvas plot() {
        final ComponentCanvas canvas = new ComponentCanvas(this.width, this.height);
        canvas.fill(GraphComponents.BACKGROUND);

        this.basicOutline(canvas);
        this.prettifyOutline(canvas);
        this.addColourAndHoverInformation(canvas);

        canvas.flip();
        return canvas.build();
    }

    private void basicOutline(final ComponentCanvas canvas) {
        for (int x = 0; x < this.width; ++x) {
            final int row = this.rowFromColumn(x);
            final int nextRow = this.rowFromColumn(x + 1);
            final int minRow = Math.min(row, nextRow);
            final int maxRow = Math.max(row, nextRow);

            if (maxRow - minRow >= 2) {
                canvas.set(x, minRow, GraphComponents.TOP_DOTTED_LINE);
                canvas.set(x, maxRow, GraphComponents.BOTTOM_DOTTED_LINE);

                for (int y = minRow + 1; y < maxRow; ++y) {
                    canvas.set(x, y, GraphComponents.VERTICAL_LINE);
                }
            } else {
                canvas.set(x, row, GraphComponents.HORIZONTAL_LINE);
            }
        }
    }

    private void prettifyOutline(final ComponentCanvas canvas) {
        for (int x = 0; x < this.width; ++x) {
            final int row = this.rowFromColumn(x);
            final int nextRow = this.rowFromColumn(x + 1);
            final int prevRow = this.rowFromColumn(x - 1);
            final int minRow = Math.min(row, nextRow);
            final int maxRow = Math.max(row, nextRow);

            if (maxRow - minRow >= 2) {
                this.prettifyVerticalOutline(canvas, x, row, nextRow, prevRow, minRow, maxRow);
            } else {
                this.prettifySlopes(canvas, x, row, nextRow, prevRow);
            }
        }
    }

    private void prettifyVerticalOutline(
        final ComponentCanvas canvas,
        final int x,
        final int row,
        final int nextRow,
        final int prevRow,
        final int minRow,
        final int maxRow
    ) {
        if (minRow == nextRow) {
            canvas.set(x, minRow, GraphComponents.CONE_BOTTOM_LEFT);
        } else if (prevRow <= minRow) {
            canvas.set(x, minRow, GraphComponents.CONE_BOTTOM_RIGHT);
        }
        if (prevRow == row + 1 && nextRow < row) {
            canvas.set(x, maxRow, GraphComponents.CONE_TOP_RIGHT);
        }
        if (maxRow == row && Math.abs(nextRow - maxRow) > 1 && Math.abs(prevRow - maxRow) > 1 && prevRow < maxRow) {
            canvas.set(x - 1, maxRow, GraphComponents.CONE_TOP_LEFT);
            canvas.set(x, maxRow, GraphComponents.CONE_TOP_RIGHT);
        }
        if (minRow == row && Math.abs(nextRow - minRow) > 1 && Math.abs(prevRow - minRow) > 1 && prevRow > minRow) {
            canvas.set(x - 1, minRow, GraphComponents.CONE_BOTTOM_LEFT);
            canvas.set(x, minRow, GraphComponents.CONE_BOTTOM_RIGHT);
        }
    }

    private void prettifySlopes(
        final ComponentCanvas canvas,
        final int x,
        final int row,
        final int nextRow,
        final int prevRow
    ) {
        final int slopeDirection = nextRow - prevRow;
        final int slopeChange = Math.abs(slopeDirection);

        if (slopeChange >= 2 && Math.max(nextRow, prevRow) == row + 1) {
            canvas.set(x, row, slopeDirection < 0 ? GraphComponents.TL_TO_BR : GraphComponents.BL_TO_TR);
        } else if (Math.abs(row - nextRow) == 1 || slopeDirection == 0) {
            if (row < nextRow) {
                canvas.set(x, row, GraphComponents.TOP_DOTTED_LINE);
            } else if (row > nextRow) {
                canvas.set(x, row, GraphComponents.BOTTOM_DOTTED_LINE);
            }
        } else if (Math.abs(row - prevRow) == 1) {
            if (prevRow > row) {
                canvas.set(x, row, GraphComponents.TOP_DOTTED_LINE);
            } else if (prevRow < row) {
                canvas.set(x, row, GraphComponents.BOTTOM_DOTTED_LINE);
            }
        }
    }
}
