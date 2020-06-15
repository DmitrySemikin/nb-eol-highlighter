package dsemikin.eolhighlighting;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.Optional;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/*package*/ final class EolHighlightsContainerProvider {

    private static final String EOL = "\n";

    // todo: we need to get colors from the options
    private static final AttributeSet DEFAULT_COLORS =
            AttributesUtilities.createImmutable(
                    StyleConstants.Foreground,
                    new Color(236, 235, 163)
            );

    private final WeakReference<Document> weakDocument; // todo: do we really need it to be weak?
    private final OffsetsBag offsetBag;

    private final RequestProcessor requestProcessor;
    private static final int HIGHLIGHTS_UPDATE_DELAY = 100;
    private Optional<RequestProcessor.Task> highlightsUpdateTask = Optional.empty();
    private final EolHighlightsDocumentListener documentListener; // we need strong reference to it. See WeakListeners docs.

    /*package*/ EolHighlightsContainerProvider(final Document document) {
        weakDocument = new WeakReference<>(document);
        offsetBag = new OffsetsBag(document);
        requestProcessor = new RequestProcessor(EolHighlightsContainerProvider.class);
        documentListener = new EolHighlightsDocumentListener();

        document.addDocumentListener(WeakListeners.document(documentListener, document));

        updateHighlights();
    }

    /*package*/ HighlightsContainer getHighlightsContainer() {
        return offsetBag;
    }

    private void scheduleEolHighlightsUpdate() {
        if (highlightsUpdateTask.isEmpty()) {
            // todo: should we do something about multithreading?
            highlightsUpdateTask = Optional.of(
                    requestProcessor.create(new EolHighlightsUpdateTask())
            );
        }
        highlightsUpdateTask.get().schedule(HIGHLIGHTS_UPDATE_DELAY);
    }

    private void updateHighlights() {
        // todo: Probably not the most performant implementation. But for now - proof of concept.
        // todo: Probably would be nice to factor logic for calculation of highlights into separate class
        Document document = weakDocument.get();
        if (document != null) {
            try {
                offsetBag.clear();
                final String text = document.getText(0, document.getLength());
                for (int index = text.indexOf(EOL); index >= 0; index = text.indexOf(EOL, index + 1)) {
                    offsetBag.addHighlight(index, index + 1, DEFAULT_COLORS);                }
            } catch (BadLocationException ex) {
                // we don't expect it to ever happen...
                // todo: what about multithreading?
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private class EolHighlightsUpdateTask implements Runnable {
        @Override
        public void run() {
            EolHighlightsContainerProvider.this.updateHighlights();
        }
    }

    private class EolHighlightsDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            handleDocumentChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            handleDocumentChange();
        }

        @Override
        public void changedUpdate(DocumentEvent unused) {
            // nothing to do.
        }

        private void handleDocumentChange() {
            EolHighlightsContainerProvider.this.scheduleEolHighlightsUpdate();
        }

    }
}
