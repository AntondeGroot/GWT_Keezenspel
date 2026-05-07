package adg.keezen.util;

import adg.keezen.i18n.AppConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class GameRulesWidget extends Composite {

    private static final AppConstants CONSTANTS = GWT.create(AppConstants.class);

    private FlowPanel overlay;

    public GameRulesWidget() {
        Button button = new Button(CONSTANTS.rulesButton());
        button.setStylePrimaryName("rulesButton");
        button.addClickHandler(e -> showModal());
        initWidget(button);
    }

    private void showModal() {
        if (overlay == null) {
            overlay = buildOverlay();
            RootPanel.get().add(overlay);
            overlay.setVisible(false);
        }
        overlay.setVisible(true);
    }

    private void hideModal() {
        if (overlay != null) {
            overlay.setVisible(false);
        }
    }

    private FlowPanel buildOverlay() {
        FlowPanel o = new FlowPanel();
        o.addStyleName("rules-overlay");
        // Click on the dark backdrop closes the modal
        o.addDomHandler(e -> hideModal(), ClickEvent.getType());

        FlowPanel content = new FlowPanel();
        content.addStyleName("rules-content");
        // Prevent clicks inside the content from bubbling to the backdrop
        content.addDomHandler(e -> e.stopPropagation(), ClickEvent.getType());

        Label title = new Label(CONSTANTS.rulesTitle());
        title.addStyleName("rules-title");
        content.add(title);

        addSection(content,
            CONSTANTS.rulesGettingOnBoard(),
            new String[]{"A", "K"},
            new boolean[]{true, true},
            new String[]{clean(CONSTANTS.hintAce()), clean(CONSTANTS.hintKing())});

        addSection(content,
            CONSTANTS.rulesSpecialCards(),
            new String[]{"4", "7", "J", "Q"},
            new boolean[]{false, false, true, true},
            new String[]{clean(CONSTANTS.hintFour()), clean(CONSTANTS.hintSeven()),
                         clean(CONSTANTS.hintJack()), clean(CONSTANTS.hintQueen())});

        Label closeHint = new Label(CONSTANTS.rulesClickToClose());
        closeHint.addStyleName("rules-close-hint");
        content.add(closeHint);

        o.add(content);
        return o;
    }

    private void addSection(FlowPanel parent, String sectionTitle,
                            String[] cards, boolean[] isRed, String[] texts) {
        FlowPanel section = new FlowPanel();
        section.addStyleName("rules-section");

        Label heading = new Label(sectionTitle);
        heading.addStyleName("rules-section-title");
        section.add(heading);

        for (int i = 0; i < cards.length; i++) {
            FlowPanel row = new FlowPanel();
            row.addStyleName("rules-row");

            HTML chip = new HTML("<div class='rules-card-chip"
                + (isRed[i] ? " rules-card-red" : "") + "'>" + cards[i] + "</div>");
            row.add(chip);

            HTML text = new HTML(texts[i]);
            text.addStyleName("rules-row-text");
            row.add(text);

            section.add(row);
        }

        parent.add(section);
    }

    private static String clean(String s) {
        return s.replace("\n", "<br>");
    }
}
