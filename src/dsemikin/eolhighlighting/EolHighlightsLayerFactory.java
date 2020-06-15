package dsemikin.eolhighlighting;

import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

@MimeRegistration(mimeType = "", service = HighlightsLayerFactory.class)
public final class EolHighlightsLayerFactory implements HighlightsLayerFactory  {

    private static final String LAYER_TYPE_ID = EolHighlightsContainerOwner.class.getName();
    private static final ZOrder Z_ORDER = ZOrder.CARET_RACK.forPosition(2000); // todo: Check, that it is appropriate

    @Override
    public HighlightsLayer[] createLayers(Context context) {

        final HighlightsLayer[] highlightsLayers = new HighlightsLayer[] {
            HighlightsLayer.create(
                    LAYER_TYPE_ID,
                    Z_ORDER,
                    isFixedSize(),
                    getHighlightsContainer(context)
            )
        };

        return highlightsLayers;
    }

    private boolean isFixedSize() {
        // todo: Actually this information must ber retrieved from
        // HighlightsContainerProvider. We don't know, if highlights actually
        // modify e.g. font family etc.
        return true;
    }

    private EolHighlightsContainerOwner getEolHighlightsContainerOwner(final Document document) {

        EolHighlightsContainerOwner eolHighlightsContainerProvider =
                (EolHighlightsContainerOwner) document.getProperty(EolHighlightsContainerOwner.class);

        if (eolHighlightsContainerProvider == null) {
            eolHighlightsContainerProvider = new EolHighlightsContainerOwner(document);
            document.putProperty(EolHighlightsContainerOwner.class, eolHighlightsContainerProvider);
        }

        return eolHighlightsContainerProvider;
    }

    private HighlightsContainer getHighlightsContainer(final Context context) {

        final Document document = context.getDocument();
        final EolHighlightsContainerOwner highlightsContainerOwner = getEolHighlightsContainerOwner(document);
        final HighlightsContainer highlightsContainer = highlightsContainerOwner.getHighlightsContainer();

        return highlightsContainer;
    }
}
