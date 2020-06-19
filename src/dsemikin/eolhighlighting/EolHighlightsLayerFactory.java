package dsemikin.eolhighlighting;

import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

@MimeRegistration(mimeType = "", service = HighlightsLayerFactory.class)
public final class EolHighlightsLayerFactory implements HighlightsLayerFactory  {

    private static final String LAYER_TYPE_ID = EolHighlightsContainer.class.getName();
    private static final ZOrder Z_ORDER = ZOrder.CARET_RACK.forPosition(2000); // todo: Check, that it is appropriate

    @Override
    public HighlightsLayer[] createLayers(Context context) {

        EolHighlightsContainer highlightsContainer = getHighlightsContainer(context);
        final HighlightsLayer[] highlightsLayers = new HighlightsLayer[] {
            HighlightsLayer.create(
                    LAYER_TYPE_ID,
                    Z_ORDER,
                    highlightsContainer.isFixedSize(),
                    highlightsContainer
            )
        };

        return highlightsLayers;
    }

    private EolHighlightsContainer getEolHighlightsContainer(final Document document) {

        EolHighlightsContainer eolHighlightsContainer =
                (EolHighlightsContainer) document.getProperty(EolHighlightsContainer.class);

        if (eolHighlightsContainer == null) {
            eolHighlightsContainer = new EolHighlightsContainer(document);
            document.putProperty(EolHighlightsContainer.class, eolHighlightsContainer);
        }

        return eolHighlightsContainer;
    }

    private EolHighlightsContainer getHighlightsContainer(final Context context) {

        final Document document = context.getDocument();
        final EolHighlightsContainer highlightsContainer = getEolHighlightsContainer(document);

        return highlightsContainer;
    }
}
