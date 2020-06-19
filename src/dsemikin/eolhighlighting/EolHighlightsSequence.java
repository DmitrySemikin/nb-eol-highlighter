package dsemikin.eolhighlighting;

import java.util.ConcurrentModificationException;
import java.util.List;
import javax.swing.text.AttributeSet;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/*package*/ class EolHighlightsSequence implements HighlightsSequence, HighlightsChangeListener {

    private boolean highlightsChanged = false;
    private int currentStartOffsetIdx = -1; // first moveNext() should return first highlight
    private final List<Integer> startOffsets;
    private final AttributeSet attributes;

    /*package*/ EolHighlightsSequence(
            final List<Integer> startOffsets,
            final AttributeSet attributes
    ) {
        this.startOffsets = startOffsets;
        this.attributes = attributes;
    }

    @Override
    public boolean moveNext() {
        if (highlightsChanged) {
            throw new ConcurrentModificationException("Calling EolHighlightsSequence.moveNext() after highlights were changed.");
        }
        currentStartOffsetIdx++;
        return currentStartOffsetIdx < startOffsets.size();
    }

    @Override
    public int getStartOffset() {
        if (highlightsChanged) {
            throw new ConcurrentModificationException("Calling EolHighlightsSequence.getStartOffset() after highlights were changed.");
        }
        return startOffsets.get(currentStartOffsetIdx);
    }

    @Override
    public int getEndOffset() {
        if (highlightsChanged) {
            throw new ConcurrentModificationException("Calling EolHighlightsSequence.getEndOffset() after highlights were changed.");
        }
        return startOffsets.get(currentStartOffsetIdx) + 1; // length of EOL is alwqays 1
    }

    @Override
    public AttributeSet getAttributes() {
        if (highlightsChanged) {
            throw new ConcurrentModificationException("Calling EolHighlightsSequence.getAttributes() after highlights were changed.");
        }
        return attributes;
    }

    @Override
    public void highlightChanged(HighlightsChangeEvent unused) {
        highlightsChanged = true;
    }
}
