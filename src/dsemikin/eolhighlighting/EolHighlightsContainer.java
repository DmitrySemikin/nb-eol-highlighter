package dsemikin.eolhighlighting;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/*package*/ class EolHighlightsContainer extends AbstractHighlightsContainer {

    private static final String EOL = "\n";

    // todo: we need to get colors from the options
    private static final AttributeSet DEFAULT_COLORS =
            AttributesUtilities.createImmutable(
                    StyleConstants.Foreground,
                    new Color(236, 235, 163)
            );

    private final WeakReference<Document> weakDocument; // todo: do we really need it to be weak?
    private final List<Integer> highlightsStartOffsets;
    private final EolHighlightsDocumentListener documentListener; // we need strong reference to it. See WeakListeners docs.

    EolHighlightsContainer(final Document document) {
        weakDocument = new WeakReference<>(document);
        documentListener = new EolHighlightsDocumentListener();
        document.addDocumentListener(WeakListeners.document(documentListener, document));

        highlightsStartOffsets = calculateHighlights(document);
    }

    @Override
    public HighlightsSequence getHighlights(int i, int i1) {
        EolHighlightsSequence highlightsSequence = new EolHighlightsSequence(highlightsStartOffsets, DEFAULT_COLORS);
        this.addHighlightsChangeListener(highlightsSequence);
        return highlightsSequence;
    }

    /*package*/ boolean isFixedSize() {
        // TODO: implement based on the returned attribute set.
        return true;
    }

    private static List<Integer> calculateHighlights(final Document document) {
        final List<Integer> eolOffsets = new LinkedList<>();
        try {
            final String text = document.getText(0, document.getLength());
            for (
                int eolOffset = text.indexOf(EOL);
                0 <= eolOffset;
                eolOffset = text.indexOf(EOL, eolOffset + 1)
            ) {
                eolOffsets.add(eolOffset);
            }
        } catch (BadLocationException ex) {
            // we don't expect it to ever happen...
            // todo: what about multithreading?
            Exceptions.printStackTrace(ex);
        }
        return eolOffsets;
    }

    private void updateHighlightsOnInsertion(final int insertedBlockStartOffset, final int insertedBlockEndOffset) {

        // find, where to insert the highlights from new block
        int insertIndex = -1;
        for (int currentIndex = 0; currentIndex < highlightsStartOffsets.size(); currentIndex++) {
            if (highlightsStartOffsets.get(currentIndex) >= insertedBlockStartOffset) {
                insertIndex = currentIndex;
                break;
            }
        }
        if (insertIndex == -1) {
            insertIndex = highlightsStartOffsets.size(); // after the last element.
        }

        // insert highlights from new block
        Document document = weakDocument.get();
        if (document != null) {
            try {
                final String text = document.getText(0, document.getLength());
                for (
                    int eolOffset = text.indexOf(EOL, insertedBlockStartOffset);
                    0 <= eolOffset && eolOffset < insertedBlockEndOffset;
                    eolOffset = text.indexOf(EOL, eolOffset + 1)
                ) {
                    highlightsStartOffsets.add(insertIndex, eolOffset);
                    insertIndex++;
                }
            } catch (BadLocationException ex) {
                // we don't expect it to ever happen...
                // todo: what about multithreading?
                Exceptions.printStackTrace(ex);
            }
        }

        // update highlights after the new block
        for (int updateIndex = insertIndex; updateIndex < highlightsStartOffsets.size(); updateIndex++) {
            highlightsStartOffsets.set(updateIndex, highlightsStartOffsets.get(updateIndex) + insertedBlockEndOffset - insertedBlockStartOffset);
        }

        // notify about the update
        fireHighlightsChange(insertedBlockStartOffset, insertedBlockEndOffset);
    }

    private void updateHighlightsOnRemoval(final int removedBlockStartOffset, final int removedBlockEndOffset) {

        // we create copy and iterate backwards to avoid problems when deleting elements.
        List<Integer> copyOfStartOffsets = new ArrayList(highlightsStartOffsets);
        for (int index = copyOfStartOffsets.size() - 1; index >= 0 ; index--) {
            Integer currentHighlightStartOffset = highlightsStartOffsets.get(index);
            if (removedBlockEndOffset <= currentHighlightStartOffset) {
                // update start offsets of highlights behind deleted block
                highlightsStartOffsets.set(index, currentHighlightStartOffset - (removedBlockEndOffset - removedBlockStartOffset));
            }
            if (removedBlockStartOffset <= currentHighlightStartOffset && currentHighlightStartOffset < removedBlockEndOffset) {
                // remove start offsets of highlights within the deleted block
                highlightsStartOffsets.remove(index);
            }
        }

        fireHighlightsChange(removedBlockStartOffset, removedBlockStartOffset);
    }

    private class EolHighlightsDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            EolHighlightsContainer.this.updateHighlightsOnInsertion(e.getOffset(), e.getOffset() + e.getLength());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            EolHighlightsContainer.this.updateHighlightsOnRemoval(e.getOffset(), e.getOffset() + e.getLength());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            EolHighlightsContainer.this.updateHighlightsOnRemoval(e.getOffset(), e.getOffset() + e.getLength());
            EolHighlightsContainer.this.updateHighlightsOnInsertion(e.getOffset(), e.getOffset() + e.getLength());
        }
    }
}
