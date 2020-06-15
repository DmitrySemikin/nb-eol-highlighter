package dsemikin.eolhighlighting;

import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

@MimeRegistration(mimeType = "", service = HighlightsLayerFactory.class)
public final class EolHighlightsLayerFactory implements HighlightsLayerFactory  {

    private static final String LAYER_TYPE_ID = EolHighlightsContainerOwner.class.getName();
    private static final ZOrder Z_ORDER = ZOrder.CARET_RACK.forPosition(2000); // todo: Check, that it is appropriate

    @Override
    public HighlightsLayer[] createLayers(Context context) {

        EolHighlightsContainerOwner highlightsContainerOwner = getHighlightsContainerOwner(context);
        final HighlightsLayer[] highlightsLayers = new HighlightsLayer[] {
            HighlightsLayer.create(
                    LAYER_TYPE_ID,
                    Z_ORDER,
                    highlightsContainerOwner.isFixedSize(),
                    highlightsContainerOwner.getHighlightsContainer()
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

        EolHighlightsContainerOwner eolHighlightsContainerOwner =
                (EolHighlightsContainerOwner) document.getProperty(EolHighlightsContainerOwner.class);

        if (eolHighlightsContainerOwner == null) {
            eolHighlightsContainerOwner = new EolHighlightsContainerOwner(document);
            document.putProperty(EolHighlightsContainerOwner.class, eolHighlightsContainerOwner);
        }

        return eolHighlightsContainerOwner;
    }

    private EolHighlightsContainerOwner getHighlightsContainerOwner(final Context context) {

        final Document document = context.getDocument();
        final EolHighlightsContainerOwner highlightsContainerOwner = getEolHighlightsContainerOwner(document);

        return highlightsContainerOwner;
    }
}
